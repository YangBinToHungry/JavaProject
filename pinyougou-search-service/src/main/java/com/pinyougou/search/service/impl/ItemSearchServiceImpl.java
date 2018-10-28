package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout=3000)
public class ItemSearchServiceImpl implements ItemSearchService{
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public Map<String, Object> search(Map searchMap) {
        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        Map<String,Object> map= new HashMap<>();
        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //3.查询品牌和规格列表
        String categoryName=(String)searchMap.get("category");
        if(!"".equals(categoryName)){
            map.putAll(searchBrandAndSpecList(categoryName));
        }else{
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0).toString()));
            }
        }
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap){
        ArrayList<String> list = new ArrayList<>();
        SimpleQuery query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult  = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for(GroupEntry<TbItem> entry:content ){
            //结果存值
            list.add(entry.getGroupValue());
        }
        return list;
    }

    /**
     * 根据关键字搜索列表
     * @param
     * @return
     */
    private Map searchList(Map searchMap){
        Map<String,Object>map=new HashMap<>();
        //设置高亮的域
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);
        //添加查询条件
        //1.1 关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //1.2 按商品分类过滤
        if(!"".equals(searchMap.get("category")) ){
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3 按品牌过滤
        if(!"".equals(searchMap.get("brand"))  ){
            FilterQuery filterQuery=new SimpleFilterQuery();
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4 按规格过滤
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
            for(String key :specMap.keySet()){
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.5 按价格筛选.....
        if(!"".equals(searchMap.get("price")) && searchMap.get("price")!=null){
            //item_price:[0 TO 20]
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria pricecriteria = new Criteria("item_price");
            String[] split = searchMap.get("price").toString().split("-");
            //如果有* 语法是不支持的
            if(!split[1].equals("*")) {
                pricecriteria.between(split[0], split[1], true, true);
            }else{
                pricecriteria.greaterThanEqual(split[0]);
            }
            filterQuery.addCriteria(pricecriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.6 分页查询
        //提取页码
        Integer pageNo= (Integer) searchMap.get("pageNo");
        if(pageNo==null){
            //默认第一页
            pageNo=1;
        }
        //每页记录数
        Integer pageSize=(Integer) searchMap.get("pageSize");
        if(pageSize==null){
            //默认20
            pageSize=10;
        }
        //从第几条记录查询
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        //1.7排序
        //ASC  DESC
        String sortValue = (String) searchMap.get("sort");
        //排序字段
        String sortField = (String) searchMap.get("sortField");
        if(sortValue!=null&&!sortValue.equals("")) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //循环高亮入口集合
        for(HighlightEntry<TbItem> h:page.getHighlighted()){
            //获取原实体类
            TbItem item = h.getEntity();
            if(h.getHighlights().size()>0&&h.getHighlights().get(0).getSnipplets().size()>0){
                //设置高亮结果
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        //返回总页数
        map.put("totalPages", page.getTotalPages());
        //返回总记录数
        map.put("total", page.getTotalElements());
        return map;
    }

    /**
     * 查询品牌和规格列表
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        //获取模板ID
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(typeId!=null){
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            //返回值添加品牌列表
            map.put("brandList", brandList);
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }



}

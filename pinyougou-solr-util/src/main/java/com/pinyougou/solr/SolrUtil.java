package com.pinyougou.solr;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;

import java.util.List;
import java.util.Map;

public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入商品数据到索引库中
     */
    public void importItemData() {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //已审核
        criteria.andStatusEqualTo("1");
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        System.out.println("===商品列表===");
        for(TbItem item:tbItems){
            Map specMap= JSON.parseObject(item.getSpec());
            //给带注解的字段赋值
            item.setSpecMap(specMap);
            System.out.println(item.getTitle());
        }
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
        System.out.println("===结束===");
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-*.xml");
        context.getBean(SolrUtil.class).importItemData();
    }
}

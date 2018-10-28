package com.pinyougou.page.service.impl;

/*import com.alibaba.dubbo.config.annotation.Service;*/

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl  implements ItemPageService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfig;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 从数据库查询数据
     * @param goodsId
     * @return
     */
    @Override
    public boolean genItemHtml(Long goodsId){
        try {
            Configuration configuration = freeMarkerConfig.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            Map dataModel=new HashMap<>();

            //1.加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);

            //2.加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);

            //3.商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);

            //4.SKU列表
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            //状态为有效
            criteria.andStatusEqualTo("1");
            //指定SPU ID
            criteria.andGoodsIdEqualTo(goodsId);
            //按照状态降序，保证第一个为默认
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);

            Writer out=new FileWriter("F:\\item\\"+goodsId+".html");
            template.process(dataModel, out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                new File("F:\\item\\" + goodsId + ".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}


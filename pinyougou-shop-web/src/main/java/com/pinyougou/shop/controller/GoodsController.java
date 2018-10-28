package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.group.Goods;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * 返回全部列表
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll(){
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult  findPage(int page,int rows){
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods){
        try {
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.getGoods().setSellerId(sellerId);
            goodsService.add(goods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods){
        try {
            //检验是否是当前商家的ID
            Goods goods1 = goodsService.findOne(goods.getGoods().getId());
            String sellerId1 = goods1.getGoods().getSellerId();
            //获取当前登录的商家ID
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            if(!sellerId1.equals(sellerId)||!sellerId.equals(sellerId1)){
                return new Result(false,"非法操作");
            }
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id){
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long [] ids){
        try {
            goodsService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
        //获取商家ID
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        //添加查询条件
        goods.setSellerId(sellerId);
        return goodsService.findPage(goods, page, rows);
    }

}

package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.group.Goods;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		TbGoods tbGoods = goods.getGoods();
		//设置未审核状态
		tbGoods.setAuditStatus("0");
		//设置为未删除
		tbGoods.setIsDelete(false);
		goodsMapper.insert(tbGoods);
		//设置ID
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		goodsDesc.setGoodsId(tbGoods.getId());
		//插入商品扩展数据
		goodsDescMapper.insert(goodsDesc);
		if("1".equals(tbGoods.getIsEnableSpec())){
			//添加商品的sku的列表
			for(TbItem item:goods.getItemList()){
				//标题
				String title = tbGoods.getGoodsName();
				Map<String,Object> specMap = JSON.parseObject(item.getSpec());
				for(String key:specMap.keySet()){
					title+=""+specMap.get(key);
				}
				item.setTitle(title);
				//商品spu编号
				item.setGoodsId(tbGoods.getId());
				//商家编号
				item.setSellerId(tbGoods.getSellerId());
				//商品分类编号
				item.setCategoryid(tbGoods.getCategory3Id());
				//创建日期
				item.setCreateTime(new Date());
				//修改日期
				item.setUpdateTime(new Date());
				//品牌名称
				TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
				item.setBrand(brand.getName());
				//分类名称
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
				item.setCategory(itemCat.getName());
				//商家名称
				TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
				item.setSeller(seller.getNickName());
				//图片地址
				List<Map> imageList = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
				if(imageList.size()>0){
					item.setImage(imageList.get(0).get("url").toString());
				}
				itemMapper.insert(item);
			}
		}else {
			//添加单品
			TbItem item = new TbItem();
			//商品KPU+规格描述串作为SKU名称
			item.setTitle(tbGoods.getGoodsName());
			//价格
			item.setPrice( tbGoods.getPrice() );
			//状态
			item.setStatus("1");
			//是否默认
			item.setIsDefault("1");
			//库存数量
			item.setNum(99999);
			item.setSpec("{}");

			//商品spu编号
			item.setGoodsId(tbGoods.getId());
			//商家编号
			item.setSellerId(tbGoods.getSellerId());
			//商品分类编号
			item.setCategoryid(tbGoods.getCategory3Id());
			//创建日期
			item.setCreateTime(new Date());
			//修改日期
			item.setUpdateTime(new Date());
			//品牌名称
			TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
			item.setBrand(brand.getName());
			//分类名称
			TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
			item.setCategory(itemCat.getName());
			//商家名称
			TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
			item.setSeller(seller.getNickName());
			//图片地址
			List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
			if(imageList.size()>0){
				item.setImage(imageList.get(0).get("url").toString());
			}
			itemMapper.insert(item);
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//获取对象
		TbGoods tbGoods = goods.getGoods();
		TbGoodsDesc goodsDesc = goods.getGoodsDesc();
		//设置未申请状态:如果是经过修改的商品，需要重新设置状态,另外新启用方法来修改状态值。
		tbGoods.setAuditStatus("0");
		//保存商品表
		goodsMapper.updateByPrimaryKey(tbGoods);
		//保存商品扩展表
		goodsDescMapper.updateByPrimaryKey(goodsDesc);
		//删除原有的sku列表数据
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(tbGoods.getId());
		itemMapper.deleteByExample(example);
		//添加新的sku列表数据
		if("1".equals(tbGoods.getIsEnableSpec())){
			//添加商品的sku的列表
			for(TbItem item:goods.getItemList()){
				//标题
				String title = tbGoods.getGoodsName();
				Map<String,Object> specMap = JSON.parseObject(item.getSpec());
				for(String key:specMap.keySet()){
					title+=""+specMap.get(key);
				}
				item.setTitle(title);
				//商品spu编号
				item.setGoodsId(tbGoods.getId());
				//商家编号
				item.setSellerId(tbGoods.getSellerId());
				//商品分类编号
				item.setCategoryid(tbGoods.getCategory3Id());
				//创建日期
				item.setCreateTime(new Date());
				//修改日期
				item.setUpdateTime(new Date());
				//品牌名称
				TbBrand brand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
				item.setBrand(brand.getName());
				//分类名称
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
				item.setCategory(itemCat.getName());
				//商家名称
				TbSeller seller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
				item.setSeller(seller.getNickName());
				//图片地址
				List<Map> imageList = JSON.parseArray(goodsDesc.getItemImages(), Map.class);
				if(imageList.size()>0){
					item.setImage(imageList.get(0).get("url").toString());
				}
				itemMapper.insert(item);
			}
		}else {
			//添加单品
			TbItem item = new TbItem();
			//商品KPU+规格描述串作为SKU名称
			item.setTitle(tbGoods.getGoodsName());
			//价格
			item.setPrice( tbGoods.getPrice() );
			//状态
			item.setStatus("1");
			//是否默认
			item.setIsDefault("1");
			//库存数量
			item.setNum(99999);
			item.setSpec("{}");

			//商品spu编号
			item.setGoodsId(tbGoods.getId());
			//商家编号
			item.setSellerId(tbGoods.getSellerId());
			//商品分类编号
			item.setCategoryid(tbGoods.getCategory3Id());
			//创建日期
			item.setCreateTime(new Date());
			//修改日期
			item.setUpdateTime(new Date());
			//品牌名称
			TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
			item.setBrand(brand.getName());
			//分类名称
			TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
			item.setCategory(itemCat.getName());
			//商家名称
			TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
			item.setSeller(seller.getNickName());
			//图片地址
			List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
			if(imageList.size()>0){
				item.setImage(imageList.get(0).get("url").toString());
			}
			itemMapper.insert(item);
		}

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		//商品
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//商品细节
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		//商品列表
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete(true);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteEqualTo(false);
		if(goods!=null){

			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for(Long id:ids){
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}

	}

	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		criteria.andStatusEqualTo(status);
		return itemMapper.selectByExample(example);
	}

	/**
	 * 插入SKU列表数据
	 * @param goods
	 */
	private void saveItemList(Goods goods){

	}

}

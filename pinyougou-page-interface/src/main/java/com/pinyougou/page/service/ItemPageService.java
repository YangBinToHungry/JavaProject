package com.pinyougou.page.service;

/**
 * 商品详细页接口
 * @author Administrator
 *
 */
public interface ItemPageService {
	/**
	 * 生成商品详细页
	 * @param goodsId
	 */
	public boolean genItemHtml(Long goodsId);

	/**
	 * 删除商品详细页
	 * @param
	 * @return
	 */
	public boolean deleteItemHtml(Long[] goodsIds);
}
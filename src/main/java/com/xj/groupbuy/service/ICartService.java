package com.xj.groupbuy.service;

import com.xj.groupbuy.common.vo.CommonVO;
import com.xj.groupbuy.entity.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhangxiaojian
 * @since 2021-03-07
 */
public interface ICartService extends IService<Cart> {

    CommonVO addGoodsToCart(Integer goodsId);

    Cart getUserCart(String userId);
}

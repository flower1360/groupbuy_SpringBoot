package com.xj.groupbuy.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhangxiaojian
 * @since 2021-04-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID",type = IdType.AUTO)
    private Integer id;

    @TableField("PARENT_ORDER_ID")
    private Integer parentOrderId;

    @TableField("GOODS_ID")
    private Integer goodsId;

    @TableField("GOODS_NUM")
    private Integer goodsNum;

    @TableField("GOODS_PRICE")
    private BigDecimal goodsPrice;

    @TableField("GOODS_UNIT")
    private String goodsUnit;

    @TableField("SPEC_KEY")
    private String specKey;

    @TableField("SPEC_KEY_NAME")
    private String specKeyName;


}

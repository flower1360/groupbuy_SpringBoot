package com.xj.groupbuy.background.category.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhangxiaojian
 * @since 2021-03-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("CATEGORY")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("CAT_ID")
    private String catId;

    @TableField("CAT_NAME")
    private String catName;

    @TableField("PARENT_CAT_ID")
    private String parentCatId;

    @TableField("SORT")
    private int sort;

}

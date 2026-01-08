package com.kmbeast.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 跳单举报实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "jump_order_report")
public class JumpOrderReport {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 预约单ID
     */
    private Integer orderId;

    /**
     * 被举报用户ID
     */
    private Integer userId;

    /**
     * 举报中介ID
     */
    private Integer agentId;

    /**
     * 房屋ID
     */
    private Integer houseId;

    /**
     * 举报说明
     */
    private String content;

    /**
     * 证据图片路径，逗号分隔
     */
    private String evidence;

    /**
     * 状态：0-待审核，1-审核属实(已扣分)，2-审核驳回
     */
    private Integer status;

    /**
     * 举报时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 审核时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    /**
     * 管理员审核意见
     */
    private String adminComment;
}

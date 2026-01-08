package com.kmbeast.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 问题反馈实体类，对应数据库的feedback表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "feedback")
public class Feedback {

    /**
     * 工单号（主键）
     */
    @TableId
    private String workOrderId;

    /**
     * 反馈用户ID，外键关联用户表
     */
    private Integer userId;

    /**
     * 关联房屋ID（可选，当反馈与特定房屋相关时）
     */
    private Integer houseId;

    /**
     * 关联订单ID（可选，当反馈与特定订单相关时）
     */
    private Integer orderId;

    /**
     * 反馈类型（BUG：功能异常，SUGGESTION：功能建议，COMPLAINT：投诉，OTHER：其他）
     */
    private String feedbackType;

    /**
     * 反馈标题
     */
    private String title;

    /**
     * 反馈内容详情
     */
    private String content;

    /**
     * 联系方式（电话/邮箱）
     */
    private String contactInfo;

    /**
     * 处理状态（PENDING：待处理，PROCESSING：处理中，RESOLVED：已解决，CLOSED：已关闭）
     */
    private String status;

    /**
     * 优先级（HIGH：高，MEDIUM：中，LOW：低）
     */
    private String priority;

    /**
     * 分配给的客服/处理人员ID
     */
    private Integer assignedTo;

    /**
     * 管理员回复内容
     */
    private String adminReply;

    /**
     * 回复时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 用户名（关联查询字段，不映射到数据库）
     */
    @TableField(exist = false)
    private String username;

    /**
     * 房屋标题（关联查询字段，不映射到数据库）
     */
    @TableField(exist = false)
    private String houseTitle;

    /**
     * 处理人员用户名（关联查询字段，不映射到数据库）
     */
    @TableField(exist = false)
    private String assignedToName;
}
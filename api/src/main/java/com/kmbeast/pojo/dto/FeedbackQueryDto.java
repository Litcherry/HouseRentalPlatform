package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题反馈查询条件类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FeedbackQueryDto extends QueryDto {

    /**
     * 工单号
     */
    private String workOrderId;

    /**
     * 反馈用户ID
     */
    private Integer userId;

    /**
     * 关联房屋ID
     */
    private Integer houseId;

    /**
     * 关联订单ID
     */
    private Integer orderId;

    /**
     * 反馈类型（BUG：功能异常，SUGGESTION：功能建议，COMPLAINT：投诉，OTHER：其他）
     */
    private String feedbackType;

    /**
     * 反馈标题（模糊查询）
     */
    private String title;

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
     * 创建时间开始
     */
    private LocalDateTime createTimeStart;

    /**
     * 创建时间结束
     */
    private LocalDateTime createTimeEnd;

    /**
     * 房屋ID集合
     */
    private List<Integer> houseIds;
}
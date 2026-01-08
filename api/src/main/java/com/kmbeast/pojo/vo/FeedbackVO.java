package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.Feedback;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问题反馈VO类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FeedbackVO extends Feedback {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 房源标题
     */
    private String houseTitle;

    /**
     * 处理人员用户名
     */
    private String assignedToName;

    /**
     * 反馈类型描述
     */
    private String feedbackTypeName;

    /**
     * 状态描述
     */
    private String statusName;

    /**
     * 优先级描述
     */
    private String priorityName;
}
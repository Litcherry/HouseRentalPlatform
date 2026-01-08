package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.FeedbackQueryDto;
import com.kmbeast.pojo.entity.Feedback;
import com.kmbeast.pojo.vo.FeedbackVO;

import java.util.List;

/**
 * 问题反馈业务逻辑接口
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 分页查询问题反馈列表
     *
     * @param feedbackQueryDto 查询参数
     * @return Result<List<FeedbackVO>> 问题反馈列表
     */
    Result<List<FeedbackVO>> list(FeedbackQueryDto feedbackQueryDto);

    /**
     * 新增问题反馈
     *
     * @param feedback 问题反馈实体
     * @return Result<String> 操作结果
     */
    Result<String> saveEntity(Feedback feedback);

    /**
     * 更新问题反馈
     *
     * @param feedback 问题反馈实体
     * @return Result<String> 操作结果
     */
    Result<String> updateEntity(Feedback feedback);

    /**
     * 删除问题反馈
     *
     * @param workOrderId 工单号
     * @return Result<String> 操作结果
     */
    Result<String> deleteEntity(String workOrderId);

    /**
     * 处理问题反馈（管理员回复）
     *
     * @param workOrderId 工单号
     * @param adminReply 管理员回复内容
     * @param status 处理状态
     * @return Result<String> 操作结果
     */
    Result<String> handleFeedback(String workOrderId, String adminReply, String status);

    /**
     * 分配问题反馈给处理人员
     *
     * @param workOrderId 工单号
     * @param assignedTo 处理人员ID
     * @return Result<String> 操作结果
     */
    Result<String> assignFeedback(String workOrderId, Integer assignedTo);

    /**
     * 查询用户的问题反馈
     *
     * @param userId 用户ID
     * @return Result<List<FeedbackVO>> 用户的问题反馈列表
     */
    Result<List<FeedbackVO>> listByUserId(Integer userId);

    /**
     * 查询分配给处理人员的问题反馈
     *
     * @param assignedTo 处理人员ID
     * @return Result<List<FeedbackVO>> 分配给该人员的问题反馈列表
     */
    Result<List<FeedbackVO>> listByAssignedTo(Integer assignedTo);
}
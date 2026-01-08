package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.mapper.FeedbackMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.FeedbackQueryDto;
import com.kmbeast.pojo.entity.Feedback;
import com.kmbeast.pojo.vo.FeedbackVO;
import com.kmbeast.service.FeedbackService;
import com.kmbeast.utils.AssertUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题反馈业务逻辑接口实现类 - 修复版本
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Resource
    private FeedbackMapper feedbackMapper;

    @Override
    public Result<List<FeedbackVO>> list(FeedbackQueryDto feedbackQueryDto) {
        // 先查询总数
        Integer count = feedbackMapper.listCount(feedbackQueryDto);

        // 如果没有数据，返回空列表
        if (count == null || count == 0) {
            return ApiResult.success();
        }

        // 设置安全的分页参数
        if (feedbackQueryDto.getCurrent() != null && feedbackQueryDto.getSize() != null) {
            // 确保分页参数合法
            Integer current = feedbackQueryDto.getCurrent();
            Integer size = feedbackQueryDto.getSize();

            // 防止负值和零值
            current = current == null || current < 1 ? 1 : current;
            size = size == null || size < 1 ? 10 : size;

            // 计算offset，确保不为负数
            Integer offset = (current - 1) * size;
            offset = offset < 0 ? 0 : offset;

            feedbackQueryDto.setOffset(offset);
            feedbackQueryDto.setCurrent(current);
            feedbackQueryDto.setSize(size);
        } else {
            // 设置默认分页参数
            feedbackQueryDto.setOffset(0);
            feedbackQueryDto.setCurrent(1);
            feedbackQueryDto.setSize(10);
        }

        // 查询数据（Mapper中已经做了JOIN查询，包含了用户和房屋信息）
        List<FeedbackVO> feedbackVOList = feedbackMapper.list(feedbackQueryDto);

        // 设置枚举值的描述
        if (feedbackVOList != null) {
            feedbackVOList.forEach(this::setFeedbackDescriptions);
        }

        return ApiResult.success(feedbackVOList, count);
    }

    @Override
    public Result<String> saveEntity(Feedback feedback) {
        // 参数校验
        AssertUtils.notNull(feedback, "反馈信息不能为空");
        AssertUtils.hasText(feedback.getTitle(), "反馈标题不能为空");
        AssertUtils.hasText(feedback.getContent(), "反馈内容不能为空");
        AssertUtils.hasText(feedback.getFeedbackType(), "反馈类型不能为空");

        // 生成工单号
        String workOrderId = feedbackMapper.generateWorkOrderId();
        feedback.setWorkOrderId(workOrderId);

        // 设置默认值
        if (!StringUtils.hasText(feedback.getStatus())) {
            feedback.setStatus("PENDING");
        }
        if (!StringUtils.hasText(feedback.getPriority())) {
            feedback.setPriority("MEDIUM");
        }
        feedback.setCreateTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());

        boolean saved = this.save(feedback);
        if (!saved) {
            return ApiResult.error("反馈提交失败");
        }
        return ApiResult.success("反馈提交成功", workOrderId);
    }

    @Override
    public Result<String> updateEntity(Feedback feedback) {
        // 参数校验
        AssertUtils.notNull(feedback, "反馈信息不能为空");
        AssertUtils.hasText(feedback.getWorkOrderId(), "工单号不能为空");

        Feedback existingFeedback = this.getById(feedback.getWorkOrderId());
        AssertUtils.notNull(existingFeedback, "反馈信息不存在");

        // 更新时间
        feedback.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(feedback);
        if (!updated) {
            return ApiResult.error("更新失败");
        }
        return ApiResult.success("更新成功");
    }

    @Override
    public Result<String> deleteEntity(String workOrderId) {
        AssertUtils.hasText(workOrderId, "工单号不能为空");

        Feedback feedback = this.getById(workOrderId);
        AssertUtils.notNull(feedback, "反馈信息不存在");

        boolean deleted = this.removeById(workOrderId);
        if (!deleted) {
            return ApiResult.error("删除失败");
        }
        return ApiResult.success("删除成功");
    }

    @Override
    public Result<String> handleFeedback(String workOrderId, String adminReply, String status) {
        AssertUtils.hasText(workOrderId, "工单号不能为空");
        AssertUtils.hasText(adminReply, "回复内容不能为空");
        AssertUtils.hasText(status, "处理状态不能为空");

        Feedback feedback = this.getById(workOrderId);
        AssertUtils.notNull(feedback, "反馈信息不存在");

        feedback.setAdminReply(adminReply);
        feedback.setStatus(status);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(feedback);
        if (!updated) {
            return ApiResult.error("处理失败");
        }
        return ApiResult.success("处理成功");
    }

    @Override
    public Result<String> assignFeedback(String workOrderId, Integer assignedTo) {
        AssertUtils.hasText(workOrderId, "工单号不能为空");
        AssertUtils.notNull(assignedTo, "处理人员ID不能为空");

        Feedback feedback = this.getById(workOrderId);
        AssertUtils.notNull(feedback, "反馈信息不存在");

        feedback.setAssignedTo(assignedTo);
        feedback.setStatus("PROCESSING");
        feedback.setUpdateTime(LocalDateTime.now());

        boolean updated = this.updateById(feedback);
        if (!updated) {
            return ApiResult.error("分配失败");
        }
        return ApiResult.success("分配成功");
    }

    @Override
    public Result<List<FeedbackVO>> listByUserId(Integer userId) {
        AssertUtils.notNull(userId, "用户ID不能为空");

        List<FeedbackVO> feedbackList = feedbackMapper.listByUserId(userId);
        // 设置枚举值的描述
        if (feedbackList != null) {
            feedbackList.forEach(this::setFeedbackDescriptions);
        }
        return ApiResult.success(feedbackList);
    }

    @Override
    public Result<List<FeedbackVO>> listByAssignedTo(Integer assignedTo) {
        AssertUtils.notNull(assignedTo, "处理人员ID不能为空");

        List<FeedbackVO> feedbackList = feedbackMapper.listByAssignedTo(assignedTo);
        // 设置枚举值的描述
        if (feedbackList != null) {
            feedbackList.forEach(this::setFeedbackDescriptions);
        }
        return ApiResult.success(feedbackList);
    }

    /**
     * 设置反馈枚举值的描述
     */
    private void setFeedbackDescriptions(FeedbackVO feedback) {
        if (feedback == null) {
            return;
        }

        // 设置枚举描述
        feedback.setFeedbackTypeName(getFeedbackTypeName(feedback.getFeedbackType()));
        feedback.setStatusName(getStatusName(feedback.getStatus()));
        feedback.setPriorityName(getPriorityName(feedback.getPriority()));
    }

    /**
     * 获取反馈类型描述
     */
    private String getFeedbackTypeName(String feedbackType) {
        if (!StringUtils.hasText(feedbackType)) {
            return "";
        }
        switch (feedbackType.toUpperCase()) {
            case "BUG":
                return "功能异常";
            case "SUGGESTION":
                return "功能建议";
            case "COMPLAINT":
                return "投诉";
            case "OTHER":
                return "其他";
            default:
                return feedbackType;
        }
    }

    /**
     * 获取状态描述
     */
    private String getStatusName(String status) {
        if (!StringUtils.hasText(status)) {
            return "";
        }
        switch (status.toUpperCase()) {
            case "PENDING":
                return "待处理";
            case "PROCESSING":
                return "处理中";
            case "RESOLVED":
                return "已解决";
            case "CLOSED":
                return "已关闭";
            default:
                return status;
        }
    }

    /**
     * 获取优先级描述
     */
    private String getPriorityName(String priority) {
        if (!StringUtils.hasText(priority)) {
            return "";
        }
        switch (priority.toUpperCase()) {
            case "HIGH":
                return "高";
            case "MEDIUM":
                return "中";
            case "LOW":
                return "低";
            default:
                return priority;
        }
    }
}
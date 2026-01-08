package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.FeedbackQueryDto;
import com.kmbeast.pojo.entity.Feedback;
import com.kmbeast.pojo.vo.FeedbackVO;
import com.kmbeast.pojo.vo.SelectedVO;
import com.kmbeast.service.FeedbackService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 问题反馈控制器
 */
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    /**
     * 新增问题反馈
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public Result<String> save(@RequestBody Feedback feedback) {
        // 设置当前用户ID
        feedback.setUserId(LocalThreadHolder.getUserId());
        return feedbackService.saveEntity(feedback);
    }

    /**
     * 更新问题反馈
     */
    @PutMapping(value = "/update")
    @ResponseBody
    public Result<String> update(@RequestBody Feedback feedback) {
        return feedbackService.updateEntity(feedback);
    }

    /**
     * 删除问题反馈
     */
    @DeleteMapping(value = "/{workOrderId}")
    @ResponseBody
    public Result<String> delete(@PathVariable String workOrderId) {
        return feedbackService.deleteEntity(workOrderId);
    }

    /**
     * 处理问题反馈（管理员回复）
     */
    @PutMapping(value = "/handle/{workOrderId}")
    @ResponseBody
    public Result<String> handle(@PathVariable String workOrderId, @RequestBody Feedback feedback) {
        return feedbackService.handleFeedback(workOrderId, feedback.getAdminReply(), feedback.getStatus());
    }

    /**
     * 分配问题反馈给处理人员
     */
    @PutMapping(value = "/assign/{workOrderId}")
    @ResponseBody
    public Result<String> assign(@PathVariable String workOrderId, @RequestBody Feedback feedback) {
        return feedbackService.assignFeedback(workOrderId, feedback.getAssignedTo());
    }

    /**
     * 查询问题反馈列表（管理员）
     */
    @Pager
    @PostMapping(value = "/list")
    @ResponseBody
    public Result<List<FeedbackVO>> list(@RequestBody FeedbackQueryDto feedbackQueryDto) {
        return feedbackService.list(feedbackQueryDto);
    }

    /**
     * 查询当前用户的问题反馈
     */
    @Pager
    @PostMapping(value = "/listUser")
    @ResponseBody
    public Result<List<FeedbackVO>> listUser(@RequestBody FeedbackQueryDto feedbackQueryDto) {
        feedbackQueryDto.setUserId(LocalThreadHolder.getUserId());
        return feedbackService.list(feedbackQueryDto);
    }

    /**
     * 查询分配给当前处理人员的问题反馈
     */
    @Pager
    @PostMapping(value = "/listAssigned")
    @ResponseBody
    public Result<List<FeedbackVO>> listAssigned(@RequestBody FeedbackQueryDto feedbackQueryDto) {
        feedbackQueryDto.setAssignedTo(LocalThreadHolder.getUserId());
        return feedbackService.list(feedbackQueryDto);
    }

    /**
     * 根据用户ID查询问题反馈
     */
    @GetMapping(value = "/user/{userId}")
    @ResponseBody
    public Result<List<FeedbackVO>> getByUserId(@PathVariable Integer userId) {
        return feedbackService.listByUserId(userId);
    }

    /**
     * 根据处理人员ID查询问题反馈
     */
    @GetMapping(value = "/assigned/{assignedTo}")
    @ResponseBody
    public Result<List<FeedbackVO>> getByAssignedTo(@PathVariable Integer assignedTo) {
        return feedbackService.listByAssignedTo(assignedTo);
    }

    /**
     * 获取反馈类型选项
     */
    @GetMapping(value = "/feedbackTypes")
    @ResponseBody
    public Result<List<SelectedVO>> getFeedbackTypes() {
        List<SelectedVO> selectedVOS = new ArrayList<>();
        selectedVOS.add(new SelectedVO("BUG", "功能异常"));
        selectedVOS.add(new SelectedVO("SUGGESTION", "功能建议"));
        selectedVOS.add(new SelectedVO("COMPLAINT", "投诉"));
        selectedVOS.add(new SelectedVO("OTHER", "其他"));
        return ApiResult.success(selectedVOS);
    }

    /**
     * 获取处理状态选项
     */
    @GetMapping(value = "/statuses")
    @ResponseBody
    public Result<List<SelectedVO>> getStatuses() {
        List<SelectedVO> selectedVOS = new ArrayList<>();
        selectedVOS.add(new SelectedVO("PENDING", "待处理"));
        selectedVOS.add(new SelectedVO("PROCESSING", "处理中"));
        selectedVOS.add(new SelectedVO("RESOLVED", "已解决"));
        selectedVOS.add(new SelectedVO("CLOSED", "已关闭"));
        return ApiResult.success(selectedVOS);
    }

    /**
     * 获取优先级选项
     */
    @GetMapping(value = "/priorities")
    @ResponseBody
    public Result<List<SelectedVO>> getPriorities() {
        List<SelectedVO> selectedVOS = new ArrayList<>();
        selectedVOS.add(new SelectedVO("HIGH", "高"));
        selectedVOS.add(new SelectedVO("MEDIUM", "中"));
        selectedVOS.add(new SelectedVO("LOW", "低"));
        return ApiResult.success(selectedVOS);
    }
}
package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.FeedbackQueryDto;
import com.kmbeast.pojo.entity.Feedback;
import com.kmbeast.pojo.vo.FeedbackVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 问题反馈持久化接口
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {

    /**
     * 分页查询问题反馈列表
     *
     * @param feedbackQueryDto 查询参数
     * @return List<FeedbackVO> 问题反馈列表
     */
    List<FeedbackVO> list(FeedbackQueryDto feedbackQueryDto);

    /**
     * 统计问题反馈总数
     *
     * @param feedbackQueryDto 查询参数
     * @return Integer 总数
     */
    Integer listCount(FeedbackQueryDto feedbackQueryDto);

    /**
     * 根据用户ID查询用户的问题反馈
     *
     * @param userId 用户ID
     * @return List<FeedbackVO> 用户的问题反馈列表
     */
    List<FeedbackVO> listByUserId(@Param("userId") Integer userId);

    /**
     * 根据处理人员ID查询分配给该人员的问题反馈
     *
     * @param assignedTo 处理人员ID
     * @return List<FeedbackVO> 分配给该人员的问题反馈列表
     */
    List<FeedbackVO> listByAssignedTo(@Param("assignedTo") Integer assignedTo);

    /**
     * 生成工单号
     *
     * @return String 新的工单号
     */
    String generateWorkOrderId();
}
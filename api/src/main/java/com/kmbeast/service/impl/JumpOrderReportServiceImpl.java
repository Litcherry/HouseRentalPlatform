package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.JumpOrderReportMapper;
import com.kmbeast.mapper.UserMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.JumpOrderReportQueryDto;
import com.kmbeast.pojo.entity.JumpOrderReport;
import com.kmbeast.pojo.entity.User;
import com.kmbeast.pojo.vo.JumpOrderReportVO;
import com.kmbeast.service.JumpOrderReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JumpOrderReportServiceImpl extends ServiceImpl<JumpOrderReportMapper, JumpOrderReport> implements JumpOrderReportService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private JumpOrderReportMapper jumpOrderReportMapper;

    @Override
    public Result<String> submitReport(JumpOrderReport jumpOrderReport) {
        // 设置举报中介为当前登录用户
        jumpOrderReport.setAgentId(LocalThreadHolder.getUserId());
        jumpOrderReport.setCreateTime(LocalDateTime.now());
        jumpOrderReport.setStatus(0); // 待审核
        this.save(jumpOrderReport);
        return ApiResult.success("举报提交成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> reviewReport(JumpOrderReport jumpOrderReport) {
        JumpOrderReport existingReport = this.getById(jumpOrderReport.getId());
        if (existingReport == null) {
            return ApiResult.error("举报记录不存在");
        }
        
        // 更新审核信息
        existingReport.setStatus(jumpOrderReport.getStatus());
        existingReport.setAdminComment(jumpOrderReport.getAdminComment());
        existingReport.setReviewTime(LocalDateTime.now());
        
        this.updateById(existingReport);

        // 如果审核属实(status == 1)，扣除用户信誉分
        if (jumpOrderReport.getStatus() == 1) {
            User user = userMapper.getUserById(existingReport.getUserId());
            if (user != null) {
                int newScore = Math.max(0, user.getCreditScore() - 20);
                user.setCreditScore(newScore);
                userMapper.update(user);
            }
        }

        return ApiResult.success("审核完成");
    }

    @Override
    public Result<List<JumpOrderReportVO>> query(JumpOrderReportQueryDto queryDto) {
        List<JumpOrderReportVO> list = jumpOrderReportMapper.query(queryDto);
        int total = jumpOrderReportMapper.queryCount(queryDto);
        return ApiResult.success(list, total);
    }
}

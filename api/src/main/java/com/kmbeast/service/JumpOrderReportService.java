package com.kmbeast.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.JumpOrderReportQueryDto;
import com.kmbeast.pojo.entity.JumpOrderReport;
import com.kmbeast.pojo.vo.JumpOrderReportVO;

import java.util.List;

public interface JumpOrderReportService extends IService<JumpOrderReport> {
    
    /**
     * 提交举报
     */
    Result<String> submitReport(JumpOrderReport jumpOrderReport);

    /**
     * 审核举报
     */
    Result<String> reviewReport(JumpOrderReport jumpOrderReport);

    /**
     * 查询举报列表
     */
    Result<List<JumpOrderReportVO>> query(JumpOrderReportQueryDto queryDto);
}

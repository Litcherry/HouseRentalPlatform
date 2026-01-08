package com.kmbeast.controller;

import com.kmbeast.aop.Pager;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.JumpOrderReportQueryDto;
import com.kmbeast.pojo.entity.JumpOrderReport;
import com.kmbeast.pojo.vo.JumpOrderReportVO;
import com.kmbeast.service.JumpOrderReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/jump-order-report")
public class JumpOrderReportController {

    @Resource
    private JumpOrderReportService jumpOrderReportService;

    /**
     * 提交举报
     */
    @PostMapping(value = "/save")
    public Result<String> save(@RequestBody JumpOrderReport jumpOrderReport) {
        return jumpOrderReportService.submitReport(jumpOrderReport);
    }

    /**
     * 审核举报
     */
    @PutMapping(value = "/review")
    public Result<String> review(@RequestBody JumpOrderReport jumpOrderReport) {
        return jumpOrderReportService.reviewReport(jumpOrderReport);
    }

    /**
     * 查询列表
     */
    @Pager
    @PostMapping(value = "/list")
    public Result<List<JumpOrderReportVO>> list(@RequestBody JumpOrderReportQueryDto queryDto) {
        return jumpOrderReportService.query(queryDto);
    }
    
    /**
     * 查询当前登录用户的举报列表(中介用)
     */
    @Pager
    @PostMapping(value = "/listAgent")
    public Result<List<JumpOrderReportVO>> listAgent(@RequestBody JumpOrderReportQueryDto queryDto) {
        queryDto.setAgentId(LocalThreadHolder.getUserId());
        return jumpOrderReportService.query(queryDto);
    }
}

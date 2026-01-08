 package com.kmbeast.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kmbeast.pojo.dto.JumpOrderReportQueryDto;
import com.kmbeast.pojo.entity.JumpOrderReport;
import com.kmbeast.pojo.vo.JumpOrderReportVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface JumpOrderReportMapper extends BaseMapper<JumpOrderReport> {

    List<JumpOrderReportVO> query(JumpOrderReportQueryDto queryDto);

    int queryCount(JumpOrderReportQueryDto queryDto);
}

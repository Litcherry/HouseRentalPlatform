package com.kmbeast.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JumpOrderReportQueryDto extends QueryDto {
    
    private Integer userId;
    
    private Integer agentId;
    
    private Integer status;
}

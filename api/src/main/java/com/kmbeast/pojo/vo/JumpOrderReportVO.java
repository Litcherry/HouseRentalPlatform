package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.JumpOrderReport;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class JumpOrderReportVO extends JumpOrderReport {
    
    /**
     * 被举报用户名
     */
    private String username;
    
    /**
     * 举报中介名
     */
    private String agentName;
    
    /**
     * 房屋名称
     */
    private String houseName;
}

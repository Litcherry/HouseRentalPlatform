package com.kmbeast.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选择器VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectedVO {
    private Object value;
    private String label;
}

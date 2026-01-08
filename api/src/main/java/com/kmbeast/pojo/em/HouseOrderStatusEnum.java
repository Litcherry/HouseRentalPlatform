package com.kmbeast.pojo.em;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房屋预约看房状态枚举
 */
@Getter
@AllArgsConstructor
public enum HouseOrderStatusEnum {

    APPLYING(0, "申请中"),
    WAIT_LOOK(1, "已接单/待看房"),
    FINISHED(2, "已完成"),
    CANCELLED(3, "已取消"),
    NO_SHOW(4, "已爽约"),
    REJECTED(5, "已拒绝");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 描述
     */
    private final String detail;

    /**
     * 通过状态获取状态描述
     *
     * @param type 状态
     * @return String
     */
    public static String getDetail(Integer type) {
        for (HouseOrderStatusEnum value : HouseOrderStatusEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getDetail();
            }
        }
        return "其他";
    }

}

package com.kmbeast.pojo.vo;

import com.kmbeast.pojo.entity.House;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 房屋列表VO类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HouseVO extends House {
    /**
     * 房源所属省份ID
     */
    private Integer topAreaId;
    /**
     * 房东名
     */
    private String landlordName;
    /**
     * 房东头像
     */
    private String landlordAvatar;
    /**
     * 中介名
     */
    private String agentName;
    /**
     * 中介头像
     */
    private String agentAvatar;
    /**
     * 中介评分
     */
    private Double agentScore;
    /**
     * 所属小区
     */
    private String communityName;
    /**
     * 所属市区
     */
    private String cityAreaName;
    /**
     * 朝向名
     */
    private String directionName;
    /**
     * 装修状态，比如毛坯房
     */
    private String fitmentStatusName;
    /**
     * 押金方式，比如押一付一
     */
    private String depositMethodName;
    /**
     * 房屋类型名
     */
    private String typeName;
    /**
     * 租赁类型
     */
    private String rentalTypeName;
    /**
     * 户型类型
     */
    private String sizedName;
    /**
     * 全景图片URL，逗号分隔（用于360度全景看房）
     */
    private String panoramaJson;
    /**
     * 3D模型文件URL（GLTF/GLB格式，用于真3D漫游看房）
     */
    private String model3dUrl;
}

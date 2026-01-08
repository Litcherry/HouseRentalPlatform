package com.kmbeast.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kmbeast.context.LocalThreadHolder;
import com.kmbeast.mapper.HouseMapper;
import com.kmbeast.mapper.HouseOrderInfoMapper;
import com.kmbeast.mapper.HouseOrderStatusMapper;
import com.kmbeast.mapper.LandlordMapper;
import com.kmbeast.mapper.UserMapper;
import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import com.kmbeast.pojo.dto.HouseOrderInfoQueryDto;
import com.kmbeast.pojo.dto.LandlordQueryDto;
import com.kmbeast.pojo.em.DateTimeSplitEnum;
import com.kmbeast.pojo.em.HouseOrderStatusEnum;
import com.kmbeast.pojo.em.RoleEnum;
import com.kmbeast.pojo.entity.House;
import com.kmbeast.pojo.entity.HouseOrderInfo;
import com.kmbeast.pojo.entity.HouseOrderStatus;
import com.kmbeast.pojo.entity.User;
import com.kmbeast.pojo.vo.HouseOrderInfoVO;
import com.kmbeast.pojo.vo.LandlordVO;
import com.kmbeast.service.HouseOrderInfoService;
import com.kmbeast.utils.AssertUtils;
import com.kmbeast.utils.DateFormatUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 预约看房业务逻辑接口实现类
 */
@Service
public class HouseOrderInfoServiceImpl extends ServiceImpl<HouseOrderInfoMapper, HouseOrderInfo> implements HouseOrderInfoService {

    @Resource
    private HouseOrderStatusMapper houseOrderStatusMapper;
    @Resource
    private LandlordMapper landlordMapper;
    @Resource
    private HouseMapper houseMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 填充状态描述
     */
    private void fillOrderStatusName(List<HouseOrderInfoVO> houseOrderInfoVOS) {
        if (houseOrderInfoVOS != null) {
            for (HouseOrderInfoVO vo : houseOrderInfoVOS) {
                vo.setOrderStatusName(HouseOrderStatusEnum.getDetail(vo.getOrderStatus()));
            }
        }
    }

    /**
     * 查询预约看房列表
     *
     * @param houseOrderInfoQueryDto 查询条件
     * @return Result<List < HouseOrderInfoVO>>
     */
    @Override
    public Result<List<HouseOrderInfoVO>> list(HouseOrderInfoQueryDto houseOrderInfoQueryDto) {
        List<HouseOrderInfoVO> houseNewsListVOList = this.baseMapper.list(houseOrderInfoQueryDto);
        fillOrderStatusName(houseNewsListVOList);
        Integer count = this.baseMapper.listCount(houseOrderInfoQueryDto);
        return ApiResult.success(houseNewsListVOList, count);
    }

    /**
     * 预约看房新增
     *
     * @param houseOrderInfo 实体
     * @return Result<String>
     */
    @Override
    public Result<String> saveEntity(HouseOrderInfo houseOrderInfo) {
        // 参数校验
        judge(houseOrderInfo);
        // 校验信誉分
        checkCreditScore();
        // 用户不能预约自己名下发布的房源信息
        validHouseAuth(houseOrderInfo);
        // 如果对应的时间段超过了当前时间，不能预约
        validDateSplit(houseOrderInfo);
        // 如果这个时间段已经被别人预约，并且预约状态不处于已完成或者已取消状态，不能预约
        validStatus(houseOrderInfo);
        // 设置初始默认值
        defaultCreate(houseOrderInfo);
        // 预约记录入库
        save(houseOrderInfo);
        return ApiResult.success();
    }

    /**
     * 校验信誉分
     */
    private void checkCreditScore() {
        User user = userMapper.getUserById(LocalThreadHolder.getUserId());
        if (user != null) {
            int score = user.getCreditScore() == null ? 100 : user.getCreditScore();
            AssertUtils.isTrue(score >= 60, "您的信誉分低于60分，已被限制预约，请联系管理员申诉");
        }
    }

    /**
     * 检验：用户不能预约自己名下发布的房源信息
     * 因为房东信息基于用户去申请，意味着双重的身份，既能作为用户去预约别人发布的房源的看房申请，也能自己去发布房源
     * 当他使用用户身份去预约看房时，不能去预约自己名下维护的房源信息
     *
     * @param houseOrderInfo 预约看房信息实体
     */
    private void validHouseAuth(HouseOrderInfo houseOrderInfo) {
        // 1. 获取此时用户传进来的房屋ID
        Integer houseId = houseOrderInfo.getHouseId();
        Integer userId = LocalThreadHolder.getUserId();

        // 中介校验
        if (Objects.equals(LocalThreadHolder.getRoleId(), RoleEnum.AGENT.getRole())) {
            House house = houseMapper.selectById(houseId);
            if (house != null && Objects.equals(house.getAgentId(), userId)) {
                AssertUtils.isTrue(false, "不能预约自己管理的房源");
            }
        }

        // 2. 看看申请此预约的用户是不是认证了房东信息
        LandlordQueryDto landlordQueryDto = new LandlordQueryDto();
        landlordQueryDto.setUserId(userId); // 设置上此时申请的用户ID
        List<LandlordVO> landlordVOS = landlordMapper.list(landlordQueryDto);
        if (landlordVOS.isEmpty()) { // 证明他自己不是房东，纯粹的用户身份
            return;
        }
        LandlordVO landlordVO = landlordVOS.get(0);
        // 获取此人的房东ID
        Integer landlordId = landlordVO.getId();
        // 3. 凭着房东ID查询该房东身份下面的所有的房屋信息
        House house = houseMapper.selectById(houseId);
        if (house != null && Objects.equals(house.getLandlordId(), landlordId)) {
            AssertUtils.isTrue(false, "大兄dei，请勿预约自己名下房源信息");
        }
    }

    /**
     * 设置初始默认值 - 创建时间、操作者、初始状态申请中
     */
    private void defaultCreate(HouseOrderInfo houseOrderInfo) {
        houseOrderInfo.setCreateTime(LocalDateTime.now()); // 设置时间
        houseOrderInfo.setUserId(LocalThreadHolder.getUserId()); // 设置操作者用户ID
        houseOrderInfo.setOrderStatus(HouseOrderStatusEnum.APPLYING.getType());// 初始状态是申请中
    }

    /**
     * 判断当前预约时间段是否可用
     */
    private void validDateSplit(HouseOrderInfo houseOrderInfo) {
        LocalDate localDate = DateFormatUtil.parseChineseDate(houseOrderInfo.getOrderDate());
        boolean timeSlotValid = DateTimeSplitEnum.isTimeSlotValid(houseOrderInfo.getOrderTimeSplit(), localDate);
        AssertUtils.isTrue(timeSlotValid, "当前时间段不能预约");
    }

    /**
     * 判断当前预约时间段时候存在竞争关系
     */
    private void validStatus(HouseOrderInfo houseOrderInfo) {
        HouseOrderInfoQueryDto houseOrderInfoQueryDto = new HouseOrderInfoQueryDto();
        houseOrderInfoQueryDto.setHouseId(houseOrderInfo.getHouseId());
        houseOrderInfoQueryDto.setOrderDate(houseOrderInfo.getOrderDate());
        houseOrderInfoQueryDto.setOrderTimeSplit(houseOrderInfo.getOrderTimeSplit());
        List<HouseOrderInfoVO> houseOrderInfoVOS = this.baseMapper.list(houseOrderInfoQueryDto);
        for (HouseOrderInfoVO houseOrderInfoVO : houseOrderInfoVOS) {
            AssertUtils.isFalse(
                    Objects.equals(HouseOrderStatusEnum.APPLYING.getType(), houseOrderInfoVO.getOrderStatus()) ||
                            Objects.equals(HouseOrderStatusEnum.WAIT_LOOK.getType(), houseOrderInfoVO.getOrderStatus()),
                    "当前时间段存在其他预约信息，请稍候再试");
        }
    }

    @Override
    public Result<String> updateEntity(HouseOrderInfo houseOrderInfo) {
        judge(houseOrderInfo);
        // 状态流转处理
        statusFlow(houseOrderInfo);
        updateById(houseOrderInfo);
        return ApiResult.success();
    }

    /**
     * 状态流转过程
     *
     * @param houseOrderInfo 预约看房实体
     */
    private void statusFlow(HouseOrderInfo houseOrderInfo) {
        // 最终：记录状态流转
        HouseOrderInfo orderInfo = getById(houseOrderInfo.getId());
        AssertUtils.notNull(orderInfo, "订单查询异常");
        AssertUtils.notNull(orderInfo.getOrderStatus(), "订单原始状态缺失");
        Integer originStatus = orderInfo.getOrderStatus(); // 原始状态
        Integer newStatus = houseOrderInfo.getOrderStatus();

        // 如果传进来的状态，跟数据库存储的原始状态不一样，证明状态发生了流转
        if (!Objects.equals(originStatus, newStatus)) {
            // 状态流转业务逻辑
            // 1. 待看房 -> 生成核销码
            if (Objects.equals(newStatus, HouseOrderStatusEnum.WAIT_LOOK.getType())) {
                String code = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
                houseOrderInfo.setWriteOffCode(code);
            }
            // 2. 已完成 -> 核销码校验
            if (Objects.equals(newStatus, HouseOrderStatusEnum.FINISHED.getType())) {
                String inputCode = houseOrderInfo.getWriteOffCode();
                String correctCode = orderInfo.getWriteOffCode();
                AssertUtils.hasText(inputCode, "请输入核销码");
                AssertUtils.isTrue(inputCode.equalsIgnoreCase(correctCode), "核销码错误，请检查");
                
                // 守约加分：如果用户完成订单且无违规，信誉分 +1（上限 100）
                User u = userMapper.getUserById(orderInfo.getUserId());
                if (u != null) {
                    int currentScore = (u.getCreditScore() == null) ? 100 : u.getCreditScore();
                    if (currentScore < 100) {
                        User updateUser = new User();
                        updateUser.setId(u.getId());
                        updateUser.setCreditScore(currentScore + 1);
                        userMapper.update(updateUser);
                    }
                }
            }
            // 3. 取消 -> 检查是否违规（2小时内）
            if (Objects.equals(newStatus, HouseOrderStatusEnum.CANCELLED.getType())) {
                String orderDateStr = orderInfo.getOrderDate();
                String timeSplit = orderInfo.getOrderTimeSplit();
                if (timeSplit != null && timeSplit.contains("-")) {
                    try {
                        LocalDate orderDate = DateFormatUtil.parseChineseDate(orderDateStr);
                        String startTimeStr = timeSplit.split("-")[0];
                        String[] parts = startTimeStr.split(":");
                        int hour = Integer.parseInt(parts[0]);
                        int minute = Integer.parseInt(parts[1]);
                        LocalDateTime appointmentTime = LocalDateTime.of(orderDate, LocalTime.of(hour, minute));

                        if (LocalDateTime.now().plusHours(2).isAfter(appointmentTime)) {
                             newStatus = HouseOrderStatusEnum.NO_SHOW.getType();
                             houseOrderInfo.setOrderStatus(newStatus);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // 4. 已爽约 -> 扣除信用分
            if (Objects.equals(newStatus, HouseOrderStatusEnum.NO_SHOW.getType())) {
                User u = userMapper.getUserById(orderInfo.getUserId());
                if (u != null) {
                    int currentScore = (u.getCreditScore() == null) ? 100 : u.getCreditScore();
                    u.setCreditScore(Math.max(0, currentScore - 5));
                    userMapper.update(u);
                }
            }

            HouseOrderStatus houseOrderStatus = new HouseOrderStatus();
            houseOrderStatus.setOriginStatus(originStatus);
            houseOrderStatus.setNewId(newStatus);
            houseOrderStatus.setCreateTime(LocalDateTime.now()); // 流转时间
            houseOrderStatus.setHouseOrderInfoId(houseOrderInfo.getId());
            houseOrderStatusMapper.insert(houseOrderStatus); // 状态流转入库
        }
    }

    private void judge(HouseOrderInfo houseOrderInfo) {
        AssertUtils.notNull(houseOrderInfo, "实体数据不能为空");
        AssertUtils.notNull(houseOrderInfo.getHouseId(), "房源ID不能为空");
        AssertUtils.hasText(houseOrderInfo.getOrderDate(), "必须选择日期");
        AssertUtils.hasText(houseOrderInfo.getOrderTimeSplit(), "时间段不能为空");
    }

    /**
     * 查询房东名下维护的房屋所产生的预约看房信息
     *
     * @param houseOrderInfoQueryDto 查询参数
     * @return Result<List < HouseOrderInfoVO>> 响应结果
     */
    @Override
    public Result<List<HouseOrderInfoVO>> listLandlord(HouseOrderInfoQueryDto houseOrderInfoQueryDto) {
        // 1. 通过用户ID去查找用户自己已经认证的房东信息，取出他的房东ID
        LandlordVO landlord = getLandlord();
        AssertUtils.isTrue(landlord.getIsAudit(), "房东认证信息待审核，请稍后再试");
        // 2. 凭着查出来的房东ID，去房屋信息表查询该房东自己名下维护的所有房屋ID，取列表
        // 升级逻辑：过滤掉分配给中介的房源（即 agent_id 不为空的）
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id");
        queryWrapper.eq("landlord_id", landlord.getId());
        queryWrapper.isNull("agent_id");
        List<Object> houseIdObjs = houseMapper.selectObjs(queryWrapper);
        List<Integer> houseIds = houseIdObjs.stream()
                .map(obj -> (Integer) obj)
                .collect(Collectors.toList());

        if (houseIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        // 3. 查询预约看房订单的时候，带上房屋ID列表
        houseOrderInfoQueryDto.setHouseIds(houseIds);
        List<HouseOrderInfoVO> houseOrderInfoVOS = this.baseMapper.list(houseOrderInfoQueryDto);
        fillOrderStatusName(houseOrderInfoVOS);
        // 脱敏处理：房东端不返回核销码，应由租客出示
        if (houseOrderInfoVOS != null) {
            houseOrderInfoVOS.forEach(vo -> vo.setWriteOffCode(null));
        }
        Integer count = this.baseMapper.listCount(houseOrderInfoQueryDto);
        return ApiResult.success(houseOrderInfoVOS, count);
    }

    /**
     * 查询中介管理的房屋所产生的预约看房信息
     *
     * @param houseOrderInfoQueryDto 查询参数
     * @return Result<List < HouseOrderInfoVO>> 响应结果
     */
    @Override
    public Result<List<HouseOrderInfoVO>> listAgent(HouseOrderInfoQueryDto houseOrderInfoQueryDto) {
        Integer userId = LocalThreadHolder.getUserId();
        // 查询分配给该中介的房源
        QueryWrapper<House> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id");
        queryWrapper.eq("agent_id", userId);
        List<Object> houseIdObjs = houseMapper.selectObjs(queryWrapper);
        List<Integer> houseIds = houseIdObjs.stream()
                .map(obj -> (Integer) obj)
                .collect(Collectors.toList());

        if (houseIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        // 查询预约看房订单的时候，带上房屋ID列表
        houseOrderInfoQueryDto.setHouseIds(houseIds);
        List<HouseOrderInfoVO> houseOrderInfoVOS = this.baseMapper.list(houseOrderInfoQueryDto);
        fillOrderStatusName(houseOrderInfoVOS);
        // 脱敏处理：中介端不返回核销码
        if (houseOrderInfoVOS != null) {
            houseOrderInfoVOS.forEach(vo -> vo.setWriteOffCode(null));
        }
        Integer count = this.baseMapper.listCount(houseOrderInfoQueryDto);
        return ApiResult.success(houseOrderInfoVOS, count);
    }


    /**
     * 取得房东ID
     *
     * @return Integer
     */
    private LandlordVO getLandlord() {
        LandlordQueryDto landlordQueryDto = new LandlordQueryDto();
        landlordQueryDto.setUserId(LocalThreadHolder.getUserId()); // 设置上当前新增房屋信息的房东信息ID
        List<LandlordVO> landlordVOS = landlordMapper.list(landlordQueryDto);
        AssertUtils.isFalse(landlordVOS.isEmpty(), "房东信息异常，非法操作");
        return landlordVOS.get(0); // 用户自己申请的房东信息
    }
}

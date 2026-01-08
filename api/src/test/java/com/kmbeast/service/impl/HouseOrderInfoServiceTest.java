package com.kmbeast.service.impl;

import com.kmbeast.exception.BusinessException;
import com.kmbeast.mapper.HouseOrderInfoMapper;
import com.kmbeast.mapper.HouseOrderStatusMapper;
import com.kmbeast.pojo.em.HouseOrderStatusEnum;
import com.kmbeast.pojo.entity.HouseOrderInfo;
import com.kmbeast.pojo.entity.HouseOrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HouseOrderInfoServiceTest {

    @InjectMocks
    private HouseOrderInfoServiceImpl houseOrderInfoService;

    @Mock
    private HouseOrderInfoMapper houseOrderInfoMapper;

    @Mock
    private HouseOrderStatusMapper houseOrderStatusMapper;

    @BeforeEach
    public void setUp() {
        // Manually inject the mapper into the parent ServiceImpl class
        ReflectionTestUtils.setField(houseOrderInfoService, "baseMapper", houseOrderInfoMapper);
    }

    @Test
    public void testGenerateWriteOffCode() {
        // 模拟数据
        HouseOrderInfo updateInfo = new HouseOrderInfo();
        updateInfo.setId(1);
        updateInfo.setOrderStatus(HouseOrderStatusEnum.WAIT_LOOK.getType()); // 目标状态：待看房
        updateInfo.setHouseId(100);
        updateInfo.setOrderDate("12月20日");
        updateInfo.setOrderTimeSplit("9:00-10:00");

        HouseOrderInfo oldInfo = new HouseOrderInfo();
        oldInfo.setId(1);
        oldInfo.setOrderStatus(HouseOrderStatusEnum.APPLYING.getType()); // 原始状态：申请中

        // 模拟 baseMapper.selectById (getById调用)
        when(houseOrderInfoMapper.selectById(1)).thenReturn(oldInfo);
        
        // 模拟 updateById (不执行实际DB操作)
        when(houseOrderInfoMapper.updateById(any())).thenReturn(1);

        // 执行
        houseOrderInfoService.updateEntity(updateInfo);

        // 验证
        // 1. 验证是否生成了核销码
        Assertions.assertNotNull(updateInfo.getWriteOffCode(), "核销码应自动生成");
        Assertions.assertEquals(8, updateInfo.getWriteOffCode().length(), "核销码长度应为8位");
        
        // 2. 验证状态流转是否记录
        verify(houseOrderStatusMapper, times(1)).insert(any(HouseOrderStatus.class));
    }

    @Test
    public void testVerifyWriteOffCode_Success() {
        String code = "ABC12345";

        // 模拟数据
        HouseOrderInfo updateInfo = new HouseOrderInfo();
        updateInfo.setId(1);
        updateInfo.setOrderStatus(HouseOrderStatusEnum.FINISHED.getType()); // 目标状态：已完成
        updateInfo.setWriteOffCode(code); // 输入正确核销码
        updateInfo.setHouseId(100);
        updateInfo.setOrderDate("12月20日");
        updateInfo.setOrderTimeSplit("9:00-10:00");

        HouseOrderInfo oldInfo = new HouseOrderInfo();
        oldInfo.setId(1);
        oldInfo.setOrderStatus(HouseOrderStatusEnum.WAIT_LOOK.getType());
        oldInfo.setWriteOffCode(code); // 数据库中存的正确核销码

        when(houseOrderInfoMapper.selectById(1)).thenReturn(oldInfo);
        when(houseOrderInfoMapper.updateById(any())).thenReturn(1);

        // 执行
        houseOrderInfoService.updateEntity(updateInfo);

        // 验证没有抛出异常，且更新执行了
        verify(houseOrderInfoMapper, times(1)).updateById(any());
    }

    @Test
    public void testVerifyWriteOffCode_Fail() {
        // 模拟数据
        HouseOrderInfo updateInfo = new HouseOrderInfo();
        updateInfo.setId(1);
        updateInfo.setOrderStatus(HouseOrderStatusEnum.FINISHED.getType());
        updateInfo.setWriteOffCode("WRONG_CODE"); // 错误核销码
        updateInfo.setHouseId(100);
        updateInfo.setOrderDate("12月20日");
        updateInfo.setOrderTimeSplit("9:00-10:00");

        HouseOrderInfo oldInfo = new HouseOrderInfo();
        oldInfo.setId(1);
        oldInfo.setOrderStatus(HouseOrderStatusEnum.WAIT_LOOK.getType());
        oldInfo.setWriteOffCode("RIGHT_CODE");

        when(houseOrderInfoMapper.selectById(1)).thenReturn(oldInfo);

        // 执行并验证异常
        Assertions.assertThrows(BusinessException.class, () -> {
            houseOrderInfoService.updateEntity(updateInfo);
        });
    }
}

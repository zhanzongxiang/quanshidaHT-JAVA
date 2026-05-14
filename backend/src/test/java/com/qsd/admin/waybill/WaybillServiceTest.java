package com.qsd.admin.waybill;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.waybill.dto.WaybillEventPayload;
import com.qsd.admin.waybill.dto.WaybillLegPayload;
import com.qsd.admin.waybill.dto.WaybillSaveRequest;
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.mapper.WaybillLegMapper;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import com.qsd.admin.waybill.mapper.WaybillTrackEventMapper;
import com.qsd.admin.waybill.service.WaybillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaybillServiceTest {

    @Mock
    private WaybillOrderMapper waybillOrderMapper;

    @Mock
    private WaybillLegMapper waybillLegMapper;

    @Mock
    private WaybillTrackEventMapper waybillTrackEventMapper;

    private WaybillService waybillService;

    @BeforeEach
    void setUp() {
        waybillService = new WaybillService(waybillOrderMapper, waybillLegMapper, waybillTrackEventMapper);
    }

    @Test
    void shouldRejectDuplicateMainTrackingNoOnCreate() {
        WaybillOrder existing = new WaybillOrder();
        existing.setId(8L);

        when(waybillOrderMapper.selectActiveByMainTrackingNo("WB202605120001")).thenReturn(existing);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> waybillService.create(buildRequest("WB202605120001"))
        );

        assertEquals("主运单号已存在", ex.getMessage());
        verify(waybillOrderMapper, never()).insert(org.mockito.ArgumentMatchers.any(WaybillOrder.class));
    }

    @Test
    void shouldRejectLegWhenArrivalTimeIsEarlierThanDepartureTime() {
        when(waybillOrderMapper.selectActiveByMainTrackingNo("WB202605120002")).thenReturn(null);

        WaybillSaveRequest request = new WaybillSaveRequest(
            "WB202605120002",
            "",
            "测试客户",
            "",
            "",
            "新加坡",
            "",
            "direct",
            "created",
            "",
            "",
            1,
            null,
            "",
            List.of(new WaybillLegPayload(
                1,
                "customer_to_warehouse",
                "",
                "LEG001",
                "",
                "",
                "pending",
                false,
                "2026-05-12 12:00:00",
                "2026-05-12 11:00:00",
                ""
            )),
            List.of(new WaybillEventPayload(
                1L,
                1,
                "2026-05-12 12:30:00",
                "created",
                "已创建",
                "",
                true
            ))
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> waybillService.create(request));
        assertEquals("第 1 条分段到达时间不能早于发出时间", ex.getMessage());
    }

    @Test
    void shouldRejectEventWhenLegReferenceIsOutOfRange() {
        when(waybillOrderMapper.selectActiveByMainTrackingNo("WB202605120003")).thenReturn(null);

        WaybillSaveRequest request = new WaybillSaveRequest(
            "WB202605120003",
            "",
            "测试客户",
            "",
            "",
            "日本",
            "",
            "direct",
            "created",
            "",
            "",
            1,
            null,
            "",
            List.of(new WaybillLegPayload(
                1,
                "customer_to_warehouse",
                "",
                "LEG002",
                "",
                "",
                "pending",
                false,
                "",
                "",
                ""
            )),
            List.of(new WaybillEventPayload(
                2L,
                1,
                "2026-05-12 12:30:00",
                "created",
                "已创建",
                "",
                true
            ))
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> waybillService.create(request));
        assertEquals("第 1 条事件关联的分段序号无效", ex.getMessage());
    }

    private WaybillSaveRequest buildRequest(String mainTrackingNo) {
        return new WaybillSaveRequest(
            mainTrackingNo,
            "",
            "测试客户",
            "",
            "",
            "马来西亚",
            "",
            "direct",
            "created",
            "",
            "",
            1,
            null,
            "",
            List.of(new WaybillLegPayload(
                1,
                "customer_to_warehouse",
                "",
                "LEG0001",
                "",
                "",
                "pending",
                false,
                "",
                "",
                ""
            )),
            List.of(new WaybillEventPayload(
                1L,
                1,
                "2026-05-12 12:30:00",
                "created",
                "已创建",
                "",
                true
            ))
        );
    }
}

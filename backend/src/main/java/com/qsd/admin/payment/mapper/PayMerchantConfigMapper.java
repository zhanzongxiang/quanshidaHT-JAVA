package com.qsd.admin.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PayMerchantConfigMapper extends BaseMapper<PayMerchantConfig> {

    @Select("""
        select id, merchant_name, merchant_code, mch_id, app_id, notify_url, api_v3_key,
               app_secret,
               private_key_path, merchant_serial_no, platform_certificate_path, enabled, active,
               remark, deleted, created_at, updated_at
        from pay_merchant_config
        where deleted = 0
        order by active desc, enabled desc, id asc
        """)
    List<PayMerchantConfig> selectAllActiveRows();

    @Select("""
        select id, merchant_name, merchant_code, mch_id, app_id, notify_url, api_v3_key,
               app_secret,
               private_key_path, merchant_serial_no, platform_certificate_path, enabled, active,
               remark, deleted, created_at, updated_at
        from pay_merchant_config
        where id = #{id}
          and deleted = 0
        limit 1
        """)
    PayMerchantConfig selectActiveById(Long id);

    @Select("""
        select id, merchant_name, merchant_code, mch_id, app_id, notify_url, api_v3_key,
               app_secret,
               private_key_path, merchant_serial_no, platform_certificate_path, enabled, active,
               remark, deleted, created_at, updated_at
        from pay_merchant_config
        where active = 1
          and enabled = 1
          and deleted = 0
        order by id asc
        limit 1
        """)
    PayMerchantConfig selectCurrentActive();

    @Select("""
        select id, merchant_name, merchant_code, mch_id, app_id, notify_url, api_v3_key,
               app_secret,
               private_key_path, merchant_serial_no, platform_certificate_path, enabled, active,
               remark, deleted, created_at, updated_at
        from pay_merchant_config
        where mch_id = #{mchId}
          and deleted = 0
        limit 1
        """)
    PayMerchantConfig selectByMchId(@Param("mchId") String mchId);

    @Update("""
        update pay_merchant_config
        set active = 0,
            updated_at = current_timestamp
        where deleted = 0
          and active = 1
        """)
    int clearActiveFlag();
}

package com.qsd.admin.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.content.entity.SiteContentPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SiteContentPageMapper extends BaseMapper<SiteContentPage> {

    @Select("""
        select id, page_code, status, form_json, published_at, created_at, updated_at
        from site_content_page
        where page_code = #{pageCode}
        limit 1
        """)
    SiteContentPage selectByPageCode(String pageCode);

    @Select("""
        select id, page_code, status, form_json, published_at, created_at, updated_at
        from site_content_page
        where page_code = #{pageCode}
          and status = 'published'
        limit 1
        """)
    SiteContentPage selectPublishedByPageCode(String pageCode);

    @Select("""
        select count(1)
        from site_content_page
        where page_code like 'service-line:%'
        """)
    int countServiceLinePages();
}

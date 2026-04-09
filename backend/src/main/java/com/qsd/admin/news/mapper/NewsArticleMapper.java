package com.qsd.admin.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.news.entity.NewsArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NewsArticleMapper extends BaseMapper<NewsArticle> {

    @Select("""
        select id, title, summary, cover_image_url, content, author, status, published_at, created_at, updated_at, deleted
        from news_article
        where deleted = 0
        order by coalesce(published_at, updated_at) desc, id desc
        """)
    List<NewsArticle> selectActiveList();

    @Select("""
        select id, title, summary, cover_image_url, content, author, status, published_at, created_at, updated_at, deleted
        from news_article
        where id = #{id} and deleted = 0
        limit 1
        """)
    NewsArticle selectActiveById(Long id);
}

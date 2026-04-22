package com.qsd.admin.news.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qsd.admin.news.entity.NewsArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
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

    @Select("""
        select id, title, summary, cover_image_url, content, author, status, published_at, created_at, updated_at, deleted
        from news_article
        where deleted = 0
          and status = 'published'
        order by published_at desc, id desc
        """)
    List<NewsArticle> selectPublishedList();

    @Select("""
        <script>
        select id, title, summary, cover_image_url, author, published_at
        from news_article
        where deleted = 0
          and status = 'published'
          <if test="publishedFrom != null">
            and published_at <![CDATA[>=]]> #{publishedFrom}
          </if>
          <if test="publishedTo != null">
            and published_at <![CDATA[<]]> #{publishedTo}
          </if>
        order by published_at desc, id desc
        limit #{limit} offset #{offset}
        </script>
        """)
    List<NewsArticle> selectPublishedPageSummaries(
        @Param("publishedFrom") LocalDateTime publishedFrom,
        @Param("publishedTo") LocalDateTime publishedTo,
        @Param("offset") int offset,
        @Param("limit") int limit
    );

    @Select("""
        <script>
        select count(1)
        from news_article
        where deleted = 0
          and status = 'published'
          <if test="publishedFrom != null">
            and published_at <![CDATA[>=]]> #{publishedFrom}
          </if>
          <if test="publishedTo != null">
            and published_at <![CDATA[<]]> #{publishedTo}
          </if>
        </script>
        """)
    long countPublished(
        @Param("publishedFrom") LocalDateTime publishedFrom,
        @Param("publishedTo") LocalDateTime publishedTo
    );

    @Select("""
        select distinct year(published_at)
        from news_article
        where deleted = 0
          and status = 'published'
          and published_at is not null
        order by year(published_at) desc
        """)
    List<Integer> selectPublishedYears();

    @Select("""
        select id, title, summary, cover_image_url, content, author, status, published_at, created_at, updated_at, deleted
        from news_article
        where id = #{id}
          and deleted = 0
          and status = 'published'
        limit 1
        """)
    NewsArticle selectPublishedById(Long id);

    @Select("""
        select count(1)
        from news_article
        where deleted = 0
        """)
    long countActive();
}

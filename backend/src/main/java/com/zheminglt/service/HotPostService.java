package com.zheminglt.service;

import com.zheminglt.vo.PostVO;
import com.zheminglt.vo.ResponseVO;

import java.util.List;

/**
 * 热门帖子服务接口
 */
public interface HotPostService {

    /**
     * 获取今日热门帖子
     */
    ResponseVO<List<PostVO>> getHotPostsToday(int page, int size);

    /**
     * 获取本周热门帖子
     */
    ResponseVO<List<PostVO>> getHotPostsWeek(int page, int size);

    /**
     * 获取本月热门帖子
     */
    ResponseVO<List<PostVO>> getHotPostsMonth(int page, int size);

    /**
     * 获取总榜热门帖子
     */
    ResponseVO<List<PostVO>> getHotPostsAll(int page, int size);

    /**
     * 更新帖子热度分数
     * @param postId 帖子ID
     * @param viewCount 浏览量
     * @param likeCount 点赞数
     * @param commentCount 评论数
     */
    void updatePostHeat(Long postId, Long viewCount, Long likeCount, Long commentCount);

    /**
     * 计算帖子热度分数
     * 算法: 浏览量*1 + 点赞数*3 + 评论数*5
     */
    double calculateHeat(Long viewCount, Long likeCount, Long commentCount);
}

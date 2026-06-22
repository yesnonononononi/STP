package com.summit.stp.post.application.service;

import com.summit.stp.post.application.command.CreatePostCommand;
import com.summit.stp.post.application.command.QueryPostListByCursorCommand;
import com.summit.stp.post.application.command.UpdatePostCommand;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.shared.result.Result;

import java.util.List;

public interface PostAppService {
    /**
     * 获取帖子详情
     * @param id 帖子id
     * @return
     */
    PostVO getPostById(Long id);
    /**
     * 创建帖子
     * @param command
     * @return
     */
    Result<Void> createPost(CreatePostCommand command);
    /**
     * 修改帖子
     * @param command
     */
    void updatePost(UpdatePostCommand command);
    /**
     * 删除帖子
     * @param id
     */
    void deletePost(Long id);
    /**
     * 重发帖子
     * @param id
     */
    void republishPost(Long id);
    /**
     * 判断帖子是否被删除
     * @param postId
     * @return
     */
    public boolean postIsActive(Long postId);
    /**
     * 获取帖子列表
     * @param command
     * @return
     */
    List<PostVO> getPostPage(QueryPostListByCursorCommand command) ;

    /**
     * 点赞帖子(已点赞即取消)
     */
    void likePost(Long postId);



    /**
     * 收藏帖子(已收藏即取消)
     */
    void collectPost(Long postId);

    /**
     * 判断当前登录用户是否已点赞指定帖子
     */
    boolean isLiked(Long postId);

    /**
     * 判断当前登录用户是否已收藏指定帖子
     */
    boolean isCollected(Long postId);


    List<PostVO> getMyCollectPostList(Long targetUserId, String cursor);
    List<PostVO> getMyLikePostList(Long targetUserId, String cursor);

    /**
     * 置顶/取消置顶帖子
     */
    void topPost(Long id, Integer isTop);

    /**
     * 增加帖子浏览数
     */
    void viewPost(Long id);

    /**
     * 帖子可见性
     */
    void visibleSelf(Long id, Integer visible);
}

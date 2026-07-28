package com.summit.stp.post.application.service;

import com.summit.stp.post.application.command.CreatePostImageCommand;
import com.summit.stp.post.application.command.UpdatePostImageCommand;
import com.summit.stp.post.application.vo.PostImageVO;

import java.util.List;

public interface PostImageAppService {
    PostImageVO getPostImageById(Long id);
    List<PostImageVO> getImagesByPostId(Long postId);
    void createPostImage(CreatePostImageCommand command);
    void updatePostImage(UpdatePostImageCommand command);
    void deletePostImage(Long id);
    void deleteImagesByPostId(Long postId);
}

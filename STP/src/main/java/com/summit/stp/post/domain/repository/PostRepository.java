package com.summit.stp.post.domain.repository;


import com.summit.stp.post.domain.model.Post;

public interface PostRepository {
    Post findById(Long id);
    Post save(Post post);
    void update(Post post);
}

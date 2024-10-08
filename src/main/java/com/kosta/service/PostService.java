package com.kosta.service;

import java.util.List;

import com.kosta.domain.request.PostRequest;
import com.kosta.entity.Post;

public interface PostService {

    Post createPost(PostRequest postRequest);
    List<Post> getAllPosts();
}

package com.kosta.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Service;

import com.kosta.domain.request.PostRequest;
import com.kosta.entity.Post;
import com.kosta.repository.PostRepository;
import com.kosta.service.PostService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService{
    private final PostRepository postRepository;

    @Override
    public Post createPost(PostRequest postRequest) {
        String title = postRequest.getTitle();
        String content = postRequest.getContent();
        Post post = Post.builder().title(title).content(content).build();
        return postRepository.save(post);
    }

    @Override
    @Cacheable(cacheNames = "posts")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
}

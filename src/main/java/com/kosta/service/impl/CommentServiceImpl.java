package com.kosta.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kosta.domain.response.CommentResponse;
import com.kosta.repository.CommentRepository;
import com.kosta.service.CommentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;

    @Override
    public List<CommentResponse> getAllComments() {
        return commentRepository.findAllOrderByLikeCountDesc();
    }
}

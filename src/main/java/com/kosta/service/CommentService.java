package com.kosta.service;

import java.util.List;

import com.kosta.domain.response.CommentResponse;

public interface CommentService {

    List<CommentResponse> getAllComments();
   
}

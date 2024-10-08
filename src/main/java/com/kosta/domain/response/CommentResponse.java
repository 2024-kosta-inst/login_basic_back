package com.kosta.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private String writerName;
    private Long likeCount;
}
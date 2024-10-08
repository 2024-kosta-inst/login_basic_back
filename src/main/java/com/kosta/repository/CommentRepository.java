package com.kosta.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kosta.domain.response.CommentResponse;
import com.kosta.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>{
	@Query("SELECT new com.kosta.domain.response.CommentResponse(c.id, c.content, c.user.name, COUNT(cl.id)) "
           + "FROM Comment c LEFT JOIN CommentLike cl ON c.id = cl.comment.id "
           + "GROUP BY c.id ORDER BY COUNT(cl.id) DESC")
    List<CommentResponse> findAllOrderByLikeCountDesc();
}

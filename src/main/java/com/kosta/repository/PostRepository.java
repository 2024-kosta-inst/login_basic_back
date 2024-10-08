package com.kosta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kosta.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}

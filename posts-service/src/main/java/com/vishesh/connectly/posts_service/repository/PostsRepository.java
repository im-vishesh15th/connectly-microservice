package com.vishesh.connectly.posts_service.repository;

import com.vishesh.connectly.posts_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostsRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
}

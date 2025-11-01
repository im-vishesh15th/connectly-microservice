package com.vishesh.connectly.posts_service.event;

import lombok.Data;

@Data
public class PostCreatedEvent {
    Long creatorId;
    String content;
    Long postId;
}

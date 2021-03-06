package ru.skillbox.socnetwork.model.entity;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostComment {
    private Integer id;
    private LocalDateTime time;
    private Integer postId;
    private Integer parentId;
    private Integer authorId;
    private String commentText;
    private Boolean isBlocked = false;
}

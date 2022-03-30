package ru.skillbox.socnetwork.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.skillbox.socnetwork.model.entity.PostComment;

import java.sql.ResultSet;
import java.sql.SQLException;
public class PostCommentMapper implements RowMapper<PostComment> {
    @Override
    public PostComment mapRow(ResultSet rs, int rowNum) throws SQLException {
        PostComment mapper = new PostComment();
        mapper.setId(rs.getInt("id"));
        mapper.setTime(rs.getTimestamp("time").toLocalDateTime());
        mapper.setAuthorId(rs.getInt("author_id"));
        mapper.setPostId(rs.getInt("post_id"));
        mapper.setParentId(rs.getInt("parent_id"));
        mapper.setCommentText(rs.getString("comment_text"));
        mapper.setBlocked(rs.getBoolean("is_blocked"));
        return mapper;
    }
}
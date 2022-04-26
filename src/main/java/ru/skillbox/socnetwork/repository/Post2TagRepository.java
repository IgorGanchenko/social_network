package ru.skillbox.socnetwork.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.skillbox.socnetwork.model.entity.Post2Tag;
import ru.skillbox.socnetwork.model.entity.Tag;
import ru.skillbox.socnetwork.model.mapper.Post2TagMapper;
import ru.skillbox.socnetwork.model.mapper.TagMapper;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class Post2TagRepository {
    private final JdbcTemplate jdbc;

    public void addTag2Post(int postId, int tagId) {
        String sql = "insert into post2tag (post_id, tag_id) values (?, ?)";
        jdbc.update(sql, postId, tagId);
    }

    public List<Post2Tag> getPostTags(int postId) {
        String sql = "SELECT * FROM post2tag WHERE post_id = ?";
        return jdbc.query(sql, new Post2TagMapper(), postId);
    }

    public void deletePostTags(int postId) {
        String sql = "delete from post2tag where post_id = ?";
        jdbc.update(sql, postId);
    }
}

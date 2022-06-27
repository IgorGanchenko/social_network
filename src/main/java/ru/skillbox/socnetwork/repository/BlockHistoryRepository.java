package ru.skillbox.socnetwork.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.skillbox.socnetwork.logging.DebugLogs;

@RequiredArgsConstructor
@Repository
@DebugLogs
public class BlockHistoryRepository {
    private final JdbcTemplate jdbc;
}

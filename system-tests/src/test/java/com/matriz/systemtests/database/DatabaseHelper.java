package com.matriz.systemtests.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.UUID;

import com.matriz.systemtests.utils.ConfigHelper;

/** Direct access to the system-test database (matriz_st): reset between tests and persistence assertions. */
public class DatabaseHelper {

    /** Application tables, children first. */
    private static final String TRUNCATE_ALL =
            "TRUNCATE task_comment, task_assignee, task, project_member, project, app_user";

    /** Columns of a task row relevant to the assertions. */
    public record TaskRow(UUID id, UUID projectId, String title, String description, String status,
            String priority) {
    }

    public void deleteAllData() {
        try (Connection connection = open(); Statement statement = connection.createStatement()) {
            statement.execute(TRUNCATE_ALL);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not truncate the system-test database", e);
        }
    }

    public Optional<TaskRow> findTask(UUID taskId) {
        String sql = "select id, project_id, title, description, status, priority from task where id = ?";
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, taskId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new TaskRow(rs.getObject("id", UUID.class), rs.getObject("project_id", UUID.class),
                        rs.getString("title"), rs.getString("description"), rs.getString("status"),
                        rs.getString("priority")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read task " + taskId, e);
        }
    }

    public long countTasks(UUID projectId) {
        return count("select count(*) from task where project_id = ?", projectId);
    }

    public long countProjects(UUID projectId) {
        return count("select count(*) from project where id = ?", projectId);
    }

    public long countMembers(UUID projectId) {
        return count("select count(*) from project_member where project_id = ?", projectId);
    }

    public long countComments(UUID taskId) {
        return count("select count(*) from task_comment where task_id = ?", taskId);
    }

    public long countAssignees(UUID taskId) {
        return count("select count(*) from task_assignee where task_id = ?", taskId);
    }

    public String projectName(UUID projectId) {
        try (Connection connection = open();
                PreparedStatement statement = connection.prepareStatement("select name from project where id = ?")) {
            statement.setObject(1, projectId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read project " + projectId, e);
        }
    }

    private long count(String sql, UUID id) {
        try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Query failed: " + sql, e);
        }
    }

    private static Connection open() throws SQLException {
        return DriverManager.getConnection(ConfigHelper.jdbcUrl(), ConfigHelper.databaseUser(),
                ConfigHelper.databasePassword());
    }
}

package com.example.demo.tracking;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 埋点表数据访问（JdbcTemplate，避免引入 MyBatis 依赖，H2/MySQL 通用）。
 */
@Repository
public class TrackingMapper {

    private final JdbcTemplate jdbcTemplate;

    public TrackingMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insert(TrackingRecord record) {
        String sql = "INSERT INTO demo_api_tracking "
                + "(api_name, caller_id, person_type, person_level, department, call_time, gmt_create, gmt_modified) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                record.getApiName(),
                record.getCallerId(),
                record.getPersonType(),
                record.getPersonLevel(),
                record.getDepartment(),
                record.getCallTime(),
                record.getGmtCreate(),
                record.getGmtModified());
    }

    /**
     * 按可选条件（api_name、call_time 范围）取原始行，分桶与聚合在应用层完成，规避方言差异。
     */
    public List<TrackingRow> selectRows(String apiName, LocalDateTime start, LocalDateTime end) {
        StringBuilder sql = new StringBuilder(
                "SELECT person_type, person_level, department, call_time FROM demo_api_tracking");
        List<Object> params = new ArrayList<>();
        if (apiName != null && !apiName.isBlank()) {
            append(sql, params, "api_name = ?", apiName);
        }
        if (start != null) {
            append(sql, params, "call_time >= ?", start);
        }
        if (end != null) {
            append(sql, params, "call_time <= ?", end);
        }
        sql.append(" ORDER BY call_time");
        return jdbcTemplate.query(sql.toString(),
                (rs, i) -> new TrackingRow(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getTimestamp(4) == null ? null : rs.getTimestamp(4).toLocalDateTime()),
                params.toArray());
    }

    public DimensionValues selectDistinctDimensions() {
        return new DimensionValues(distinct("person_type"), distinct("person_level"), distinct("department"));
    }

    private List<String> distinct(String column) {
        // column 仅来自固定白名单调用，无注入风险
        return jdbcTemplate.queryForList(
                "SELECT DISTINCT " + column + " FROM demo_api_tracking ORDER BY " + column, String.class);
    }

    private void append(StringBuilder sql, List<Object> params, String condition, Object value) {
        sql.append(params.isEmpty() ? " WHERE " : " AND ").append(condition);
        params.add(value);
    }
}
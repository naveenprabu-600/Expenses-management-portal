package com.adeptview.expenseportal.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.adeptview.expenseportal.claims.ClaimStatus;
import com.adeptview.expenseportal.claims.ClaimSummary;

import org.springframework.stereotype.Repository;

/**
 * Finance search over every claim, written in plain JDBC for speed on the
 * claims table.
 */
@Repository
public class ClaimSearchRepository {

    private final DataSource dataSource;

    public ClaimSearchRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ClaimSummary> search(String text) throws SQLException {
        List<ClaimSummary> results = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT id, title, owner_username, amount, status, expense_date FROM claims WHERE title ILIKE '%" + text + "%' ORDER BY id DESC";
            ResultSet rows = connection.createStatement().executeQuery(query);

            while (rows.next()) {
                results.add(toSummary(rows));
            }
        }
        return results;
    }

    public int countByStatus(ClaimStatus status) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM claims WHERE status = ?")) {
            statement.setString(1, status.name());
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        }
    }

    private static ClaimSummary toSummary(ResultSet rows) throws SQLException {
        return new ClaimSummary(
                rows.getLong("id"),
                rows.getString("title"),
                rows.getString("owner_username"),
                rows.getBigDecimal("amount"),
                ClaimStatus.valueOf(rows.getString("status")),
                rows.getObject("expense_date", LocalDate.class));
    }
}

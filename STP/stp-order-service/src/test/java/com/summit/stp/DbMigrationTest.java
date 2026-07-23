package com.summit.stp;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DbMigrationTest {

    @Test
    public void upgradeTagTableUuidLength() {
        String url = "jdbc:mysql://localhost:3306/stp?useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String user = "root";
        String password = "root";
        System.out.println("====== DB MIGRATION: Altering tag uuid column length ======");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("-> Connecting to database: " + url);
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement()) {
                System.out.println("-> Connected. Executing Alter query...");
                stmt.execute("ALTER TABLE tag MODIFY COLUMN uuid VARCHAR(64) NOT NULL");
                System.out.println("-> Successfully altered tag.uuid to VARCHAR(64)");
            }
        } catch (Exception e) {
            System.err.println("-> DB migration failed with exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

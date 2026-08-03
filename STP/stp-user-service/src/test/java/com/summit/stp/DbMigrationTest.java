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

    @Test
    public void migratePOFields() throws Exception {
        java.nio.file.Path start = java.nio.file.Paths.get("d:\\Code\\STP\\STP");
        java.util.List<java.nio.file.Path> javaFiles = java.nio.file.Files.walk(start)
                .filter(p -> p.toString().endsWith(".java"))
                .collect(java.util.stream.Collectors.toList());

        for (java.nio.file.Path path : javaFiles) {
            String content = java.nio.file.Files.readString(path);
            if (content.contains("@TableName")) {
                // 提取表名
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("@TableName\\(\"([^\"]+)\"\\)").matcher(content);
                if (m.find()) {
                    String tableName = m.group(1);
                    if ("user_stat".equals(tableName) || "user_member".equals(tableName) || 
                        "user_sign_stats".equals(tableName) || "member_level_config".equals(tableName)) {
                        continue;
                    }
                    
                    if (content.contains("publicId") || content.contains("public_id")) {
                        continue;
                    }

                    System.out.println("Processing PO: " + path.getFileName() + " for table: " + tableName);

                    // 1. 替换 imports
                    if (content.contains("import com.baomidou.mybatisplus.annotation.TableId;")) {
                        content = content.replace("import com.baomidou.mybatisplus.annotation.TableId;",
                            "import com.baomidou.mybatisplus.annotation.TableId;\nimport com.baomidou.mybatisplus.annotation.TableField;\nimport com.baomidou.mybatisplus.annotation.FieldFill;");
                    } else if (content.contains("import com.baomidou.mybatisplus.annotation.TableName;")) {
                        content = content.replace("import com.baomidou.mybatisplus.annotation.TableName;",
                            "import com.baomidou.mybatisplus.annotation.TableName;\nimport com.baomidou.mybatisplus.annotation.TableField;\nimport com.baomidou.mybatisplus.annotation.FieldFill;");
                    }

                    // 2. 将 IdType.INPUT 和 IdType.ASSIGN_ID 替换为 IdType.AUTO
                    content = content.replaceAll("IdType\\.INPUT", "IdType.AUTO");
                    content = content.replaceAll("IdType\\.ASSIGN_ID", "IdType.AUTO");

                    // 3. 找到 id 属性，并在其后插入 publicId 属性
                    java.util.regex.Pattern idPattern = java.util.regex.Pattern.compile("(private\\s+(Long|Integer)\\s+id;)");
                    java.util.regex.Matcher idMatcher = idPattern.matcher(content);
                    if (idMatcher.find()) {
                        content = idMatcher.replaceFirst(idMatcher.group(1) + "\n    @TableField(fill = FieldFill.INSERT)\n    private Long publicId;");
                    } else {
                        System.out.println("Warning: id field not matched for " + path.getFileName());
                    }

                    java.nio.file.Files.writeString(path, content);
                }
            }
        }
        System.out.println("====== PO Migration Completed Successfully ======");
    }
}

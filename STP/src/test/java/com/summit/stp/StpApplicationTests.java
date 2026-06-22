package com.summit.stp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

@SpringBootTest
class StpApplicationTests {

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Autowired
    private org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry registry;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void testSendMq() {
        com.summit.stp.payment.domain.event.PaySuccessEvent event = new com.summit.stp.payment.domain.event.PaySuccessEvent(2058208390793920512L);
        rabbitTemplate.convertAndSend("pay.exchange", "pay.queue.success", event);
        System.out.println("====== MQ Test Message Sent Successfully ======");
    }

    @Test
    void testRegistry() {
        System.out.println("====== Listener Container Count: " + registry.getListenerContainers().size() + " ======");
        for (org.springframework.amqp.rabbit.listener.MessageListenerContainer container : registry.getListenerContainers()) {
            System.out.println("Container: " + container + ", running: " + container.isRunning());
        }
    }

    @Test
    void dropForeignKeyAndRemoveAutoIncrement() {
        System.out.println("====== Drop Foreign Key and Disable Auto Increment ======");
        try {
            // 1. 删除 comments 表对 posts 表 id 的外键约束
            jdbcTemplate.execute("ALTER TABLE comments DROP FOREIGN KEY comments_ibfk_1");
            System.out.println("-> Successfully dropped foreign key: comments_ibfk_1");
        } catch (Exception e) {
            System.out.println("-> Dropping foreign key failed (maybe already dropped?): " + e.getMessage());
        }

        try {
            // 2. 将 posts 表的 id 字段设为非自增 (MODIFY COLUMN)
            jdbcTemplate.execute("ALTER TABLE posts MODIFY COLUMN id BIGINT NOT NULL");
            System.out.println("-> Successfully modified posts.id to be non-auto-increment");
        } catch (Exception e) {
            System.out.println("-> Modifying posts.id failed: " + e.getMessage());
        }
    }

    @Test
    void testInsert100Posts() {
        System.out.println("====== Clean Old Test Posts ======");
        try {
            jdbcTemplate.update("DELETE FROM posts WHERE title LIKE '%游标测试帖子%'");
            System.out.println("-> Successfully cleaned old test posts");
        } catch (Exception e) {
            System.out.println("-> Clean failed: " + e.getMessage());
        }

        System.out.println("====== Start Inserting 100 Posts with correct timeline ======");
        Long userId = 1L;
        try {
            List<Long> userIds = jdbcTemplate.queryForList("SELECT id FROM user LIMIT 1", Long.class);
            if (userIds != null && !userIds.isEmpty()) {
                userId = userIds.get(0);
            }
        } catch (Exception e) {
            System.out.println("Get userId failed, fallback to 1L. Exception: " + e.getMessage());
        }

        // 显式插入 id，因为我们已经去掉了数据库的自增属性
        String sql = "INSERT INTO posts (id, creator_id, title, type, content, reply_count, status, create_time, update_time, media_urls) " +
                     "VALUES (?, ?, ?, 1, ?, 0, 1, ?, ?, NULL)";

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (int i = 1; i <= 100; i++) {
            // 生成 Hutool 8字节雪花 ID 作为主键
            Long postId = cn.hutool.core.util.IdUtil.getSnowflakeNextId();
            // 让时间的递增顺序与 ID 的递增顺序对齐 (i 越大，时间越新)
            java.time.LocalDateTime createTime = now.minusSeconds((101 - i) * 10);
            java.sql.Timestamp createTimestamp = java.sql.Timestamp.valueOf(createTime);
            java.sql.Timestamp updateTimestamp = java.sql.Timestamp.valueOf(now);

            String title = "雪花ID游标测试帖子 第 " + i + " 号";
            String content = "这是使用雪花ID主键和全新表结构，测试游标翻页的帖子。ID为: " + postId + "，序号: " + i + "，发帖时间: " + createTime;

            jdbcTemplate.update(sql, postId, userId, title, content, createTimestamp, updateTimestamp);
        }

        System.out.println("====== Successfully Inserted 100 Test Posts ======");
    }

    @Test
    void insert50CommentsForPost() {
        Long postId = 2060958926496780288L;
        System.out.println("====== Start Inserting 50 Comments/Replies for Post: " + postId + " ======");
        
        List<Long> userIds = List.of(1L, 2L, 3L);
        String[] firstLevelTexts = {
            "这是一篇很有深度的帖子，学到了很多！",
            "赞同作者的观点，非常客观。",
            "感谢楼主分享，期待下一篇！",
            "内容详实，思路清晰，感谢！",
            "这个观点挺新颖的，有启发。",
            "写的真不错，支持一下！",
            "非常实用的经验分享，收藏了。",
            "通俗易懂，受教了！",
            "看完收获颇丰，楼主加油！",
            "这个问题分析得太透彻了。"
        };
        
        String[] replyTexts = {
            "确实是这样，我也深有同感。",
            "赞同！特别是那一点分析得很到位。",
            "请问这个有什么具体的应用场景吗？",
            "楼上说的有道理，学习了。",
            "我怎么觉得这个方法有些局限性呢？",
            "大佬分析得太牛了，强力推荐！",
            "非常同意你的看法，点赞！",
            "原来还可以这样理解，大开眼界。",
            "有道理，很有参考价值。",
            "回复得真专业，受教了！"
        };
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.Random random = new java.util.Random();
        
        // 存储生成的一级评论的 ID
        List<Long> rootCommentIds = new java.util.ArrayList<>();
        // 存储所有已生成的评论 ID，用于随机作为被回复的父 ID
        List<Long> allCommentIds = new java.util.ArrayList<>();
        // 存储评论 ID 到 rootId 的映射，用于子回复查找它的根评论
        java.util.Map<Long, Long> commentToRootMap = new java.util.HashMap<>();

        // 1. 插入 20 条一级评论
        String insertSql = "INSERT INTO comments (id, post_id, user_id, parent_id, root_id, content, create_time, is_audit, type, is_top, status, reply_count, ip_location, client_type) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, 1, 1, 0, 1, 0, ?, ?)";
                           
        for (int i = 0; i < 20; i++) {
            Long commentId = cn.hutool.core.util.IdUtil.getSnowflakeNextId();
            Long userId = userIds.get(random.nextInt(userIds.size()));
            String content = firstLevelTexts[random.nextInt(firstLevelTexts.length)] + " (测试一级评论#" + (i + 1) + ")";
            java.time.LocalDateTime createTime = now.minusMinutes((60 - i) * 10);
            java.sql.Timestamp createTimestamp = java.sql.Timestamp.valueOf(createTime);
            String ipLocation = "北京";
            String clientType = "Web";
            
            jdbcTemplate.update(insertSql, commentId, postId, userId, null, null, content, createTimestamp, ipLocation, clientType);
            
            rootCommentIds.add(commentId);
            allCommentIds.add(commentId);
            commentToRootMap.put(commentId, commentId);
        }
        
        // 2. 插入 30 条回复评论 (子评论)
        for (int i = 0; i < 30; i++) {
            Long commentId = cn.hutool.core.util.IdUtil.getSnowflakeNextId();
            Long userId = userIds.get(random.nextInt(userIds.size()));
            // 随机选一条已有的评论作为父评论
            Long parentId = allCommentIds.get(random.nextInt(allCommentIds.size()));
            // 根据父评论找到对应的根评论
            Long rootId = commentToRootMap.get(parentId);
            
            String content = replyTexts[random.nextInt(replyTexts.length)] + " (测试回复#" + (i + 1) + ")";
            java.time.LocalDateTime createTime = now.minusMinutes((30 - i) * 5);
            java.sql.Timestamp createTimestamp = java.sql.Timestamp.valueOf(createTime);
            String ipLocation = "上海";
            String clientType = "Android";
            
            jdbcTemplate.update(insertSql, commentId, postId, userId, parentId, rootId, content, createTimestamp, ipLocation, clientType);
            
            allCommentIds.add(commentId);
            commentToRootMap.put(commentId, rootId);
            
            // 更新父级/根级评论的回复数字
            jdbcTemplate.update("UPDATE comments SET reply_count = reply_count + 1 WHERE id = ?", parentId);
            if (!parentId.equals(rootId)) {
                jdbcTemplate.update("UPDATE comments SET reply_count = reply_count + 1 WHERE id = ?", rootId);
            }
        }
        
    }

    @Test
    void insert1RootAnd30Replies() {
        Long postId = 2060958926496780288L;
        System.out.println("====== Start Inserting 1 Root Comment and 30 Replies for Post: " + postId + " ======");
        
        List<Long> userIds = List.of(1L, 2L, 3L);
        java.util.Random random = new java.util.Random();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        String insertSql = "INSERT INTO comments (id, post_id, user_id, parent_id, root_id, content, create_time, is_audit, type, is_top, status, reply_count, ip_location, client_type) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, 1, 1, 0, 1, 0, ?, ?)";
                           
        // 1. 插入一条一级评论
        Long rootCommentId = cn.hutool.core.util.IdUtil.getSnowflakeNextId();
        Long rootUserId = userIds.get(random.nextInt(userIds.size()));
        String rootContent = "这是一条专门用于测试回复堆叠和多级加载的一级评论！";
        java.time.LocalDateTime rootCreateTime = now.minusHours(2);
        java.sql.Timestamp rootTimestamp = java.sql.Timestamp.valueOf(rootCreateTime);
        
        jdbcTemplate.update(insertSql, rootCommentId, postId, rootUserId, null, null, rootContent, rootTimestamp, "广州", "Web");
        
        // 记录所有生成的二级评论 ID
        List<Long> replyIds = new java.util.ArrayList<>();
        
        // 2. 插入 30 条属于该一级评论的二级评论
        for (int i = 1; i <= 30; i++) {
            Long commentId = cn.hutool.core.util.IdUtil.getSnowflakeNextId();
            Long replyUserId = userIds.get(random.nextInt(userIds.size()));
            
            // 随机决定是直接回复根评论，还是回复前面已经生成的某条二级评论
            Long parentId = rootCommentId;
            if (i > 1 && random.nextBoolean()) {
                parentId = replyIds.get(random.nextInt(replyIds.size()));
            }
            
            String replyContent = "测试盖楼二级回复 #" + i + "，对测试有用。";
            java.time.LocalDateTime replyCreateTime = now.minusMinutes((32 - i) * 2);
            java.sql.Timestamp replyTimestamp = java.sql.Timestamp.valueOf(replyCreateTime);
            
            jdbcTemplate.update(insertSql, commentId, postId, replyUserId, parentId, rootCommentId, replyContent, replyTimestamp, "深圳", "iOS");
            
            replyIds.add(commentId);
            
            // 更新父评论的回复计数（如果是二级评论自身被回复了）
            jdbcTemplate.update("UPDATE comments SET reply_count = reply_count + 1 WHERE id = ?", parentId);
            // 如果 parentId 不是根评论，还要单独给根评论的回复数也加 1
            if (!parentId.equals(rootCommentId)) {
                jdbcTemplate.update("UPDATE comments SET reply_count = reply_count + 1 WHERE id = ?", rootCommentId);
            }
        }
        
        System.out.println("====== Successfully Inserted 1 Root Comment and 30 Replies for Post: " + postId + " ======");
    }

}



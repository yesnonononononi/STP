package com.summit.stp.shared.constants;

public interface MqConstants {
    interface Pay {
        String EXCHANGE = "pay.topic.exchange";
        String QUEUE_SUCCESS = "pay.queue.success";
        String QUEUE_FAIL = "pay.queue.fail";
        String QUEUE_FAIL_RECOVERER = "pay.queue.fail.recoverer";

        String ROUTING_KEY_SUCCESS = "pay.success";
        String ROUTING_KEY_FAIL = "pay.fail";
        String ROUTING_KEY_FAIL_RECOVERER = "pay.fail.recoverer";
    }

    interface Member {
        String EXCHANGE = "member.topic.exchange";
        String QUEUE = "member.queue.pay";
        String ROUTING_KEY = "member.pay";
    }

    interface User {
        String EXCHANGE = "user.topic.exchange";
        String QUEUE_REGISTER = "user.queue.register";
        String ROUTING_KEY_REGISTER = "user.register";

        String QUEUE_FANS = "user.queue.stat.fans";
        String ROUTING_KEY_FANS = "user.fans.change";
        String QUEUE_LIKED = "user.queue.stat.liked";
        String ROUTING_KEY_LIKED = "user.liked.change";
        String QUEUE_TOPIC = "user.queue.stat.topic";

    }

    interface Post {
        String EXCHANGE = "post.topic.exchange";
        String QUEUE = "post.tag.queue";
        String ROUTING_KEY = "post.publish";
    }

    interface Rank {
        String QUEUE_POST_PUBLISH = "rank.queue.post.publish";

    }

    interface Comment {
        String EXCHANGE = "stp.comment.exchange";
        String QUEUE = "stp.comment.queue";
        String ROUTING_KEY = "stp.comment.routing.key";
    }
}

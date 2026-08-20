package com.summit.stp.common.constants;

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
    interface Admin{
        String EXCHANGE = "admin.topic.exchange";
        String QUEUE = "admin.queue";
        String ROUTING_KEY = "admin.routing.key";
    }
    interface Coupon{
        String EXCHANGE= "coupon.topic.exchange";
        String COUPON_SECKILL_QUEUE = "coupon.seckill.queue";
        String COUPON_SECKILL_ROUTING_KEY = "coupon.seckill.routing.key";
    }
    interface Member {
        String EXCHANGE = "member.topic.exchange";
        String QUEUE = "member.queue.pay";
        String ROUTING_KEY = "member.pay";
    }

    interface User {
        String EXCHANGE = "user.topic.exchange";

        String QUEUE_CHANGE = "user.queue.change";
        String ROUTING_KEY_CHANGE = "user.change.routing.key";

        String QUEUE_ES_UPDATE = "user.queue.es.update";
        String ROUTING_KEY_ES_UPDATE = "user.es.update.routing.key";

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

        String QUEUE_INTERACTION = "post.interaction.queue";
        String ROUTING_KEY_INTERACTION = "post.interaction";
        String QUEUE_CHANGE = "post.change.queue";
        String ROUTING_KEY_CHANGE = "post.change.routing.key";
    }

    interface Rank {
        String QUEUE_POST_PUBLISH = "rank.queue.post.publish";
        String QUEUE_CREATOR_LIKED = "rank.queue.creator.liked";
        String QUEUE_CREATOR_FANS = "rank.queue.creator.fans";
        String QUEUE_CREATOR_ACTIVITY = "rank.queue.creator.activity";
    }

    interface Comment {
        String EXCHANGE = "stp.comment.exchange";
        String QUEUE = "stp.comment.queue";
        String ROUTING_KEY = "stp.comment.routing.key";
    }

    interface Es {
        String EXCHANGE = "es.topic.exchange";

        String QUEUE_POST_UPDATE = "es.post.update.queue";
        String ROUTING_KEY_POST_UPDATE = "es.post.update.routing.key";

        String QUEUE_USER_UPDATE = "es.user.update.queue";
        String ROUTING_KEY_USER_UPDATE = "es.user.update.routing.key";
    }
}

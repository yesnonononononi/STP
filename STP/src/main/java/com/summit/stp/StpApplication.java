package com.summit.stp;

import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.summit.stp.**.mapper")
public class StpApplication {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    public static void main(String[] args) {
        SpringApplication.run(StpApplication.class, args);
    }

}
/**
 * {
 *     "code": 1,
 *     "data": [
 *         {
 *             "id": "1",
 *             "creatorId": "1",
 *             "title": "发帖功能测试1",
 *             "type": "image",
 *             "content": "开心",
 *             "mediaUrls": null,
 *             "likeCount": 0,
 *             "replyCount": 0,
 *             "collectCount": 0,
 *             "status": 1,
 *             "createTime": 1779708332000,
 *             "updateTime": 1779708332000,
 *             "publisher": {
 *                 "id": "1",
 *                 "nick": "U_18573757527",
 *                 "avatar": "https://uploadfiles.nowcoder.com/images/20260413/480237176_1776080790933/FECD76F09C4EFFA7102ECDBC1795FB3B?x-oss-process=image%2Fresize%2Cw_72%2Ch_72%2Cm_mfit",
 *                 "memberLevel": "1",
 *                 "introduction": null,
 *                 "fans": "0",
 *                 "liked": "0",
 *                 "topic": "0",
 *                 "gender": "男",
 *                 "ip": "未知",
 *                 "vipType": "会员",
 *                 "vipConfigIcon": "../../../../public/v1.png",
 *                 "followed": false
 *             }
 *         }
 *     ],
 *     "errMsg": null,
 *     "signature": null
 * }
 */
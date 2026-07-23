package com.summit.stp.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.InputStream;

@Slf4j
public class IpUtil {

    private static byte[] v4Buffer;


    static {
        // 一次性加载 xdb 库至内存，支持 Jar 包直接读取
        try (InputStream v4Stream = IpUtil.class.getClassLoader().getResourceAsStream("ip2region_v4.xdb")) {
            if (v4Stream != null) {
                v4Buffer = v4Stream.readAllBytes();
                log.info("【IpUtil】成功将 ip2region_v4.xdb 加载至内存缓存");
            } else {
                log.error("【IpUtil】未在 classpath 下找到 ip2region_v4.xdb");
            }
        } catch (Exception e) {
            log.error("【IpUtil】读取 ip2region_v4.xdb 异常: ", e);
        }

    }

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    /**
     * 获取用户真实IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 如果有多层代理，取第一个非 unknown 的 IP
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 将 IP 归属地转为友好显示的文本 (如：美国加利福尼亚, 中国台湾, 湖南长沙)
     */
    public static String toString(String ip) throws Exception {
        final String TEMPLATE =  "%s • %s • %s";
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return "未知";
        }
        Searcher searcher = Searcher.newWithBuffer(v4Buffer);
        String resStr = searcher.search(ip);
        //System.out.printf("result: %S", resStr);  //中国|浙江省|杭州市|阿里|CN
        String[] arr = resStr.split("\\|");
        if(arr.length == 0 || arr[0].trim().equals("Reserved"))return "未知";
        switch (arr.length){
            case 1 -> {
                return arr[0];
            }
            case 2 ->  {
                return String.format(TEMPLATE, arr[0], arr[1], "未知市区");
            }
            default ->  {
                return String.format(TEMPLATE, arr[0], arr[1], arr[2]);
            }
        }
    }
}

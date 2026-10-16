package com.summit.stp.payment.domain;

import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.util.EncryptUtil;
import com.summit.stp.common.util.PaymentSignHelper;
import com.summit.stp.payment.api.vo.PayVO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PayDomainService 纯单元测试。
 *
 * <p>使用真实的 PaymentSignHelper + EncryptUtil 做 RSA 签名/验签配对，
 * @Value 字段通过 ReflectionTestUtils 注入。</p>
 */
class PayDomainServiceTest {

    private static String privateKeyBase64;
    private static String publicKeyBase64;

    private PayDomainService payDomainService;

    @BeforeAll
    static void initKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    @BeforeEach
    void setUp() {
        PaymentSignHelper helper = new PaymentSignHelper();
        ReflectionTestUtils.setField(helper, "currentSignType", "RSA");
        ReflectionTestUtils.setField(helper, "privateKey", privateKeyBase64);
        ReflectionTestUtils.setField(helper, "md5Sign", "");

        payDomainService = new PayDomainService(helper);
        ReflectionTestUtils.setField(payDomainService, "pid", 10001);
        ReflectionTestUtils.setField(payDomainService, "to", "pay-target");
        ReflectionTestUtils.setField(payDomainService, "notifyUrl", "http://notify");
        ReflectionTestUtils.setField(payDomainService, "returnUrl", "http://return");
        ReflectionTestUtils.setField(payDomainService, "platformPublicKey", publicKeyBase64);
    }

    @Test
    @DisplayName("buildParamMap: 字段齐全、TreeMap 排序、timestamp 为10位秒级")
    void buildParamMap_fieldsAndSorting() {
        Timestamp timestamp = new Timestamp(1_700_000_000_000L);

        Map<String, Object> params = payDomainService.buildParamMap(
                PayType.ALI_PAY, 123L, "VIP", new BigDecimal("19.90"), timestamp);

        assertThat(params).isInstanceOf(TreeMap.class);
        assertThat(params.get("pid")).isEqualTo(10001);
        assertThat(params.get("type")).isEqualTo("alipay");
        assertThat(params.get("sign_type")).isEqualTo("RSA");
        assertThat(params.get("out_trade_no")).isEqualTo("123");
        assertThat(params.get("notify_url")).isEqualTo("http://notify");
        assertThat(params.get("return_url")).isEqualTo("http://return?orderId=123");
        assertThat(params.get("name")).isEqualTo("VIP");
        assertThat(params.get("money")).isEqualTo(new BigDecimal("19.90"));
        assertThat(params.get("timestamp")).isEqualTo("1700000000");
        assertThat((String) params.get("timestamp")).hasSize(10);

        // TreeMap 迭代顺序应为 ASCII 升序
        Iterator<String> it = params.keySet().iterator();
        String prev = it.next();
        while (it.hasNext()) {
            String cur = it.next();
            assertThat(prev.compareTo(cur)).isLessThan(0);
            prev = cur;
        }
    }

    @Test
    @DisplayName("buildParamMap: timestamp 为 null 时对应值为 null")
    void buildParamMap_nullTimestamp() {
        Map<String, Object> params = payDomainService.buildParamMap(
                PayType.WX_PAY, 9L, "m", new BigDecimal("1.00"), null);

        assertThat(params).containsEntry("timestamp", null);
        assertThat(params.get("type")).isEqualTo("wxpay");
    }

    @Test
    @DisplayName("buildSignPayload: 过滤 sign/sign_type 与空值，ASCII 排序 & 拼接")
    void buildSignPayload_filtersAndJoins() {
        Timestamp timestamp = new Timestamp(1_700_000_000_000L);

        String payload = payDomainService.buildSignPayload(
                PayType.ALI_PAY, 123L, "VIP", new BigDecimal("19.90"), timestamp);

        assertThat(payload).doesNotContain("sign_type");
        assertThat(payload).isEqualTo(
                "money=19.90&name=VIP&notify_url=http://notify&out_trade_no=123"
                        + "&pid=10001&return_url=http://return?orderId=123&timestamp=1700000000&type=alipay");
    }

    @Test
    @DisplayName("buildSignPayload: 空值字段被过滤")
    void buildSignPayload_filtersEmptyValues() {
        Map<String, Object> params = payDomainService.buildParamMap(
                PayType.WX_PAY, 1L, "", new BigDecimal("5.00"), null);

        // memberName 为空字符串 -> 由 buildSignPayload 过滤
        String payload = payDomainService.buildSignPayload(
                PayType.WX_PAY, 1L, "", new BigDecimal("5.00"), null);

        assertThat(payload).doesNotContain("name=");
        assertThat(payload).doesNotContain("timestamp=");
        assertThat((String) params.get("name")).isEmpty();
    }

    @Test
    @DisplayName("generateSign + EncryptUtil.rsaVerify 配对验签成功")
    void generateSign_thenVerifySuccess() {
        Timestamp timestamp = new Timestamp(1_700_000_000_000L);

        String sign = payDomainService.generateSign(
                PayType.ALI_PAY, 123L, "VIP", new BigDecimal("19.90"), timestamp);

        String payload = payDomainService.buildSignPayload(
                PayType.ALI_PAY, 123L, "VIP", new BigDecimal("19.90"), timestamp);

        assertThat(sign).isNotBlank();
        assertThat(EncryptUtil.rsaVerify(publicKeyBase64, payload, sign)).isTrue();
    }

    @Test
    @DisplayName("checkSign: 正确回调签名通过，篡改后失败")
    void checkSign_correctAndTampered() {
        Map<String, String> params = new TreeMap<>();
        params.put("a", "1");
        params.put("b", "2");
        params.put("sign_type", "RSA"); // 应被剔除

        String payload = "a=1&b=2";
        String correctSign = EncryptUtil.rsaSign(privateKeyBase64, payload);

        assertThat(payDomainService.checkSign(params, correctSign)).isTrue();

        // 篡改业务参数后验签失败
        params.put("b", "3");
        assertThat(payDomainService.checkSign(params, correctSign)).isFalse();

        // 合法 Base64 但内容错误(other 用另一对密钥签名)的签名 -> 验签失败
        String otherKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        params.put("b", "2");
        assertThat(payDomainService.checkSign(params, otherKey)).isFalse();
    }

    @Test
    @DisplayName("buildResponse: money 2 位小数、timestamp 秒级、return_url 带 orderId")
    void buildResponse_formatting() {
        Timestamp timestamp = new Timestamp(1_700_000_000_000L);

        PayVO vo = payDomainService.buildResponse(
                PayType.ALI_PAY, 123L, new BigDecimal("19.9"), "VIP", "SIGN", timestamp);

        assertThat(vo.getPid()).isEqualTo(10001);
        assertThat(vo.getType()).isEqualTo("alipay");
        assertThat(vo.getOrderId()).isEqualTo(123L);
        assertThat(vo.getMoney()).isEqualTo("19.90");
        assertThat(vo.getMemberName()).isEqualTo("VIP");
        assertThat(vo.getTo()).isEqualTo("pay-target");
        assertThat(vo.getSign()).isEqualTo("SIGN");
        assertThat(vo.getTimestamp()).isEqualTo("1700000000");
        assertThat(vo.getTimestamp()).hasSize(10);
        assertThat(vo.getReturnUrl()).isEqualTo("http://return?orderId=123");
        assertThat(vo.getNotifyUrl()).isEqualTo("http://notify");
        assertThat(vo.getSignType()).isEqualTo("RSA");
    }

    @Test
    @DisplayName("checkSign 空参数不抛异常")
    void checkSign_emptyParams() {
        // 使用合法 Base64 的空签名，避免底层 Base64 解码异常
        assertThat(payDomainService.checkSign(new HashMap<>(), "AAAA")).isFalse();
    }
}

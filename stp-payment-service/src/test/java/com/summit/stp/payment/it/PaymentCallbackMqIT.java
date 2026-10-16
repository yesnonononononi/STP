package com.summit.stp.payment.it;

import com.summit.stp.common.application.domain.event.PayFailEvent;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.util.EncryptUtil;
import com.summit.stp.order.api.client.OrderFeignClient;
import com.summit.stp.payment.application.command.PayCallbackCheckCommand;
import com.summit.stp.payment.application.service.PayAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 支付回调链路集成测试（真实 RabbitMQ）。
 *
 * <p>支付回调验签使用平台公钥（{@code payment.platform.publicKey}）。本机内置的是平台公钥、
 * 并不持有其对应私钥，因此这里生成一对测试密钥，并通过 {@link DynamicPropertySource}
 * 覆盖 {@code payment.platform.publicKey}，从而能构造合法签名做端到端验签。</p>
 *
 * <p>覆盖：{@code PayAppServiceImpl.checkResult} 验签通过后经真实 RabbitMQ 发出
 * {@code PaySuccessEvent}/{@code PayFailEvent}；验签失败抛 {@link BusinessException} 且不投递消息；
 * 另含 {@code PayController} 回调入口的集成验证。</p>
 */
@Tag("integration")
class PaymentCallbackMqIT extends AbstractPaymentIntegrationTest {

    private static final KeyPair TEST_KEY_PAIR;
    private static final String TEST_PRIVATE_KEY_B64;
    private static final String TEST_PUBLIC_KEY_B64;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            TEST_KEY_PAIR = generator.generateKeyPair();
            TEST_PRIVATE_KEY_B64 = Base64.getEncoder().encodeToString(TEST_KEY_PAIR.getPrivate().getEncoded());
            TEST_PUBLIC_KEY_B64 = Base64.getEncoder().encodeToString(TEST_KEY_PAIR.getPublic().getEncoded());
        } catch (Exception e) {
            throw new IllegalStateException("生成测试 RSA 密钥对失败", e);
        }
    }

    /**
     * 用测试公钥覆盖平台公钥，使回调验签可被本测试私钥满足。
     *
     * @param registry 动态属性注册表
     */
    @DynamicPropertySource
    static void paymentSignProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.platform.publicKey", () -> TEST_PUBLIC_KEY_B64);
    }

    @MockitoBean
    private OrderFeignClient orderFeignClient;

    @Autowired
    private PayAppService payAppService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        amqpAdmin.purgeQueue(MqConstants.Pay.QUEUE_SUCCESS);
        amqpAdmin.purgeQueue(MqConstants.Pay.QUEUE_FAIL);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private PayCallbackCheckCommand command(long orderId, String tradeStatus, boolean validSign) {
        PayCallbackCheckCommand command = new PayCallbackCheckCommand();
        command.setPid("297");
        command.setOut_trade_no(orderId);
        command.setTrade_status(tradeStatus);
        command.setMoney("19.90");
        command.setName("VIP");
        command.setTimestamp("1700000000");
        command.setSign_type("RSA");
        command.setSign(buildSign(command, validSign));
        return command;
    }

    /**
     * 复刻 {@code PayDomainService.buildCallbackSignPayload} 的待签名串规则：
     * 剔除 sign/sign_type，按 ASCII 升序以 {@code &} 拼接。validSign=false 时对篡改后的载荷签名。
     */
    private String buildSign(PayCallbackCheckCommand command, boolean validSign) {
        TreeMap<String, String> params = new TreeMap<>();
        putIfNotNull(params, "pid", command.getPid());
        putIfNotNull(params, "out_trade_no", command.getOut_trade_no() == null ? null : String.valueOf(command.getOut_trade_no()));
        putIfNotNull(params, "trade_status", command.getTrade_status());
        putIfNotNull(params, "money", command.getMoney());
        putIfNotNull(params, "name", command.getName());
        putIfNotNull(params, "timestamp", command.getTimestamp());

        String payload = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        if (!validSign) {
            payload = payload + "&__tampered__=1";
        }
        return EncryptUtil.rsaSign(TEST_PRIVATE_KEY_B64, payload);
    }

    private void putIfNotNull(TreeMap<String, String> map, String key, String value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    @Test
    @DisplayName("checkResult：合法签名 + TRADE_SUCCESS -> 发出 PaySuccessEvent")
    void checkResult_successPublishesPaySuccess() {
        long orderId = 500_000_001L;

        payAppService.checkResult(command(orderId, "TRADE_SUCCESS", true));

        Object received = rabbitTemplate.receiveAndConvert(MqConstants.Pay.QUEUE_SUCCESS, 15_000L);
        assertThat(received).isInstanceOf(PaySuccessEvent.class);
        assertThat(((PaySuccessEvent) received).getOrderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("checkResult：合法签名 + 非成功状态 -> 发出 PayFailEvent")
    void checkResult_failPublishesPayFail() {
        long orderId = 500_000_002L;

        payAppService.checkResult(command(orderId, "TRADE_FAIL", true));

        Object received = rabbitTemplate.receiveAndConvert(MqConstants.Pay.QUEUE_FAIL, 15_000L);
        assertThat(received).isInstanceOf(PayFailEvent.class);
        PayFailEvent event = (PayFailEvent) received;
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getReason()).isEqualTo("TRADE_FAIL");
    }

    @Test
    @DisplayName("checkResult：签名不合法 -> 抛 BusinessException 且不投递消息")
    void checkResult_invalidSignThrowsAndNoMessage() {
        PayCallbackCheckCommand command = command(500_000_003L, "TRADE_SUCCESS", false);

        assertThatThrownBy(() -> payAppService.checkResult(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("签名验证失败");

        assertThat(rabbitTemplate.receiveAndConvert(MqConstants.Pay.QUEUE_SUCCESS, 2_000L)).isNull();
        assertThat(rabbitTemplate.receiveAndConvert(MqConstants.Pay.QUEUE_FAIL, 2_000L)).isNull();
    }

    @Test
    @DisplayName("PayController /pay/check：回调入口经真实 MQ 触发 PaySuccessEvent")
    void payControllerCheck_publishesPaySuccess() throws Exception {
        long orderId = 500_000_004L;
        PayCallbackCheckCommand command = command(orderId, "TRADE_SUCCESS", true);

        mockMvc.perform(get("/pay/check")
                        .param("pid", command.getPid())
                        .param("out_trade_no", String.valueOf(command.getOut_trade_no()))
                        .param("trade_status", command.getTrade_status())
                        .param("money", command.getMoney())
                        .param("name", command.getName())
                        .param("timestamp", command.getTimestamp())
                        .param("sign_type", command.getSign_type())
                        .param("sign", command.getSign()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("success"));

        Object received = rabbitTemplate.receiveAndConvert(MqConstants.Pay.QUEUE_SUCCESS, 15_000L);
        assertThat(received).isInstanceOf(PaySuccessEvent.class);
        assertThat(((PaySuccessEvent) received).getOrderId()).isEqualTo(orderId);
    }
}

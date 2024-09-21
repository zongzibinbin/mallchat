package com.abin.mallchat.common;

import com.abin.mallchat.common.common.domain.enums.IdempotentEnum;
import com.abin.mallchat.common.common.utils.JwtUtils;
import com.abin.mallchat.common.user.domain.enums.ItemEnum;
import com.abin.mallchat.common.user.service.IUserBackpackService;
import com.abin.mallchat.common.user.service.LoginService;
import com.abin.mallchat.oss.MinIOTemplate;
import com.abin.mallchat.oss.domain.OssReq;
import com.abin.mallchat.oss.domain.OssResp;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-27
 */

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class DaoTest {
    public static final long UID = 12717L;
    @Autowired
    private WxMpService wxMpService;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private MinIOTemplate minIOTemplate;

    @Test
    public void getUploadUrl() {
        OssReq ossReq = OssReq.builder()
                .fileName("test.jpeg")
                .filePath("/test")
                .autoPath(false)
                .build();
        OssResp preSignedObjectUrl = minIOTemplate.getPreSignedObjectUrl(ossReq);
        System.out.println(preSignedObjectUrl);
    }

    @Test
    public void sendMQ() {
        Message<String> build = MessageBuilder.withPayload("123").build();
        rocketMQTemplate.send("test-topic", build);
    }

    @Test
    public void jwt() {
        String login = loginService.login(UID);
        System.out.println(login);
    }

    @Autowired
//    @Lazy
    private RedissonClient redissonClient;
    @Autowired
    private LoginService loginService;

    @Test
    public void redis() {
        String s = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjExMDAyLCJjcmVhdGVUaW1lIjoxNjkzNjYzOTU1fQ.qISTe8UDzggilWqz0HKtGLrkgiG1IRGafS10qHih9iM";
        Long validUid = loginService.getValidUid(s);
        System.out.println(validUid);
    }

    @Autowired
    private IUserBackpackService iUserBackpackService;

    @Test
    public void acquireItem() {
        iUserBackpackService.acquireItem(UID, ItemEnum.REG_TOP100_BADGE.getId(), IdempotentEnum.UID, UID + "");
    }

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    public void thread() throws InterruptedException {
        threadPoolTaskExecutor.execute(() -> {
            if (1 == 1) {
                log.error("123");
                throw new RuntimeException("1243");
            }
        });
        Thread.sleep(200);
    }

    @Test
    public void test() throws WxErrorException {
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(1, 10000);
        String url = wxMpQrCodeTicket.getUrl();
        System.out.println(url);
    }

    @Test
    public void testCreateToken() {
        String token = loginService.login(10276L);
        System.out.println("token = " + token);
    }
}

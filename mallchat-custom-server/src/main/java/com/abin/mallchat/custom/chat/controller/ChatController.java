package com.abin.mallchat.custom.chat.controller;


import com.abin.mallchat.common.common.annotation.FrequencyControl;
import com.abin.mallchat.common.common.domain.vo.request.CursorPageBaseReq;
import com.abin.mallchat.common.common.domain.vo.response.ApiResult;
import com.abin.mallchat.common.common.domain.vo.response.CursorPageBaseResp;
import com.abin.mallchat.common.common.utils.RequestHolder;
import com.abin.mallchat.common.user.domain.enums.BlackTypeEnum;
import com.abin.mallchat.common.user.service.cache.UserCache;
import com.abin.mallchat.custom.chat.domain.vo.request.ChatMessageBaseReq;
import com.abin.mallchat.custom.chat.domain.vo.request.ChatMessageMarkReq;
import com.abin.mallchat.custom.chat.domain.vo.request.ChatMessagePageReq;
import com.abin.mallchat.custom.chat.domain.vo.request.ChatMessageReq;
import com.abin.mallchat.custom.chat.domain.vo.request.msg.TextMsgReq;
import com.abin.mallchat.custom.chat.domain.vo.response.ChatMemberResp;
import com.abin.mallchat.custom.chat.domain.vo.response.ChatMemberStatisticResp;
import com.abin.mallchat.custom.chat.domain.vo.response.ChatMessageResp;
import com.abin.mallchat.custom.chat.domain.vo.response.ChatRoomResp;
import com.abin.mallchat.custom.chat.service.ChatService;
import com.abin.mallchat.custom.user.service.impl.UserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * 群聊相关接口
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2023-03-19
 */
@RestController
@RequestMapping("/capi/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;
    @Autowired
    private UserCache userCache;

    @GetMapping("/public/room/page")
    @ApiOperation("会话列表")
    public ApiResult<CursorPageBaseResp<ChatRoomResp>> getRoomPage(@Valid CursorPageBaseReq request) {
        return ApiResult.success(chatService.getRoomPage(request, RequestHolder.get().getUid()));

    }

    @GetMapping("/public/member/page")
    @ApiOperation("群成员列表")
    @FrequencyControl(time = 120, count = 10, target = FrequencyControl.Target.IP)
    public ApiResult<CursorPageBaseResp<ChatMemberResp>> getMemberPage(@Valid CursorPageBaseReq request) {
        black(request);

        CursorPageBaseResp<ChatMemberResp> memberPage = chatService.getMemberPage(request);
        filterBlackMember(memberPage);
        return ApiResult.success(memberPage);
    }

    private void filterBlackMember(CursorPageBaseResp<ChatMemberResp> memberPage) {
        Set<String> blackMembers = getBlackUidSet();
        memberPage.getList().removeIf(a -> blackMembers.contains(a.getUid().toString()));
    }

    private Set<String> getBlackUidSet() {
        return userCache.getBlackMap().getOrDefault(BlackTypeEnum.UID.getType(), new HashSet<>());
    }

    @GetMapping("public/member/statistic")
    @ApiOperation("群成员人数统计")
    public ApiResult<ChatMemberStatisticResp> getMemberStatistic() {
        return ApiResult.success(chatService.getMemberStatistic());
    }

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/public/msg/page")
    @ApiOperation("消息列表")
    @FrequencyControl(time = 120, count = 10, target = FrequencyControl.Target.IP)
    public ApiResult<CursorPageBaseResp<ChatMessageResp>> getMsgPage(@Valid ChatMessagePageReq request) {
        black(request);
        CursorPageBaseResp<ChatMessageResp> msgPage = chatService.getMsgPage(request, RequestHolder.get().getUid());
        filterBlackMsg(msgPage);
        return ApiResult.success(msgPage);
    }

    private void black(CursorPageBaseReq baseReq) {
        if (baseReq.getPageSize() > 50) {
            log.info("limit request:{}", baseReq);
            baseReq.setPageSize(10);
            userService.blackIp(RequestHolder.get().getIp());
        }

    }

    private void filterBlackMsg(CursorPageBaseResp<ChatMessageResp> memberPage) {
        Set<String> blackMembers = getBlackUidSet();
        memberPage.getList().removeIf(a -> blackMembers.contains(a.getFromUser().getUid().toString()));
        System.out.println(1);
    }

    @PostMapping("/msg")
    @ApiOperation("发送消息")
    @FrequencyControl(time = 5, count = 2, target = FrequencyControl.Target.UID)
    @FrequencyControl(time = 30, count = 5, target = FrequencyControl.Target.UID)
    @FrequencyControl(time = 60, count = 10, target = FrequencyControl.Target.UID)
    public ApiResult<ChatMessageResp> sendMsg(@Valid @RequestBody ChatMessageReq request) {
        if (Objects.isNull(request.getBody())) {
            TextMsgReq req = new TextMsgReq(request.getContent(), request.getReplyMsgId());//todo 消息兼容之后删了
            request.setBody(req);
        }
        Long msgId = chatService.sendMsg(request, RequestHolder.get().getUid());
        //返回完整消息格式，方便前端展示
        return ApiResult.success(chatService.getMsgResp(msgId, RequestHolder.get().getUid()));
    }

    @PutMapping("/msg/mark")
    @ApiOperation("消息标记")
    @FrequencyControl(time = 20, count = 3, target = FrequencyControl.Target.UID)
    public ApiResult<Void> setMsgMark(@Valid @RequestBody ChatMessageMarkReq request) {
        chatService.setMsgMark(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }

    @PutMapping("/msg/recall")
    @ApiOperation("撤回消息")
    @FrequencyControl(time = 20, count = 3, target = FrequencyControl.Target.UID)
    public ApiResult<Void> recallMsg(@Valid @RequestBody ChatMessageBaseReq request) {
        chatService.recallMsg(RequestHolder.get().getUid(), request);
        return ApiResult.success();
    }
}


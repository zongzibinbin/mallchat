package com.abin.mallchat.common.chat.service.impl;

import com.abin.mallchat.common.chat.dao.*;
import com.abin.mallchat.common.chat.domain.entity.Room;
import com.abin.mallchat.common.chat.domain.entity.RoomGroup;
import com.abin.mallchat.common.chat.domain.vo.request.admin.AdminAddReq;
import com.abin.mallchat.common.chat.domain.vo.request.admin.AdminRevokeReq;
import com.abin.mallchat.common.chat.domain.vo.request.member.MemberExitReq;
import com.abin.mallchat.common.chat.service.IGroupMemberService;
import com.abin.mallchat.common.chat.service.adapter.MemberAdapter;
import com.abin.mallchat.common.chat.service.cache.GroupMemberCache;
import com.abin.mallchat.common.common.exception.CommonErrorEnum;
import com.abin.mallchat.common.common.exception.GroupErrorEnum;
import com.abin.mallchat.common.common.utils.AssertUtil;
import com.abin.mallchat.common.user.domain.enums.WSBaseResp;
import com.abin.mallchat.common.user.domain.vo.response.ws.WSMemberChange;
import com.abin.mallchat.common.user.service.impl.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.abin.mallchat.common.chat.constant.GroupConst.MAX_MANAGE_COUNT;

/**
 * @Author Kkuil
 * @Date 2023/10/24 15:45
 * @Description 群成员服务类
 */
@Service
public class GroupMemberServiceImpl implements IGroupMemberService {

    @Autowired
    private GroupMemberDao groupMemberDao;

    @Autowired
    private RoomGroupDao roomGroupDao;

    @Autowired
    private RoomDao roomDao;

    @Autowired
    private ContactDao contactDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private GroupMemberCache groupMemberCache;

    @Autowired
    private PushService pushService;

    /**
     * 增加管理员
     *
     * @param uid     用户ID
     * @param request 请求信息
     */
    @Override
    public void addAdmin(Long uid, AdminAddReq request) {
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(request.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, GroupErrorEnum.GROUP_NOT_EXIST);

        // 2. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(isLord, GroupErrorEnum.NOT_ALLOWED_OPERATION);

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), request.getUidList());
        AssertUtil.isTrue(isGroupShip, GroupErrorEnum.USER_NOT_IN_GROUP);

        // 4. 判断管理员数量是否达到上限
        // 4.1 查询现有管理员数量
        List<Long> manageUidList = groupMemberDao.getManageUidList(roomGroup.getId());
        // 4.2 去重
        HashSet<Long> manageUidSet = new HashSet<>(manageUidList);
        manageUidSet.addAll(request.getUidList());
        AssertUtil.isFalse(manageUidSet.size() > MAX_MANAGE_COUNT, GroupErrorEnum.MANAGE_COUNT_EXCEED);

        // 5. 增加管理员
        groupMemberDao.addAdmin(roomGroup.getId(), request.getUidList());
    }

    /**
     * 撤销管理员
     *
     * @param uid     用户ID
     * @param request 请求信息
     */
    @Override
    public void revokeAdmin(Long uid, AdminRevokeReq request) {
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(request.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, GroupErrorEnum.GROUP_NOT_EXIST);

        // 2. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(isLord, GroupErrorEnum.NOT_ALLOWED_OPERATION);

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), request.getUidList());
        AssertUtil.isTrue(isGroupShip, GroupErrorEnum.USER_NOT_IN_GROUP);

        // 4. 撤销管理员
        groupMemberDao.revokeAdmin(roomGroup.getId(), request.getUidList());
    }

    /**
     * 退出群聊
     *
     * @param uid     需要退出的用户ID
     * @param request 请求信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitGroup(Long uid, MemberExitReq request) {
        Long roomId = request.getRoomId();
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(roomId);
        AssertUtil.isNotEmpty(roomGroup, GroupErrorEnum.GROUP_NOT_EXIST);

        // 2. 判断房间是否是大群聊 （大群聊禁止退出）
        Room room = roomDao.getById(roomId);
        AssertUtil.isFalse(room.isHotRoom(), GroupErrorEnum.NOT_ALLOWED_FOR_EXIT_GROUP);

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), Collections.singletonList(uid));
        AssertUtil.isTrue(isGroupShip, GroupErrorEnum.USER_NOT_IN_GROUP);

        // 4. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        if (isLord) {
            // 4.1 删除房间
            boolean isDelRoom = roomDao.removeById(roomId);
            AssertUtil.isTrue(isDelRoom, CommonErrorEnum.SYSTEM_ERROR);
            // 4.2 删除会话
            // add: 查询房间内所有成员ID
            List<Long> memberUidList = groupMemberDao.getMemberUidList(roomGroup.getId());
            Boolean isDelContact = contactDao.removeByRoomId(roomId, memberUidList);
            AssertUtil.isTrue(isDelContact, CommonErrorEnum.SYSTEM_ERROR);
            // 4.3 删除群成员
            Boolean isDelGroupMember = groupMemberDao.removeByGroupId(roomGroup.getId(), memberUidList);
            AssertUtil.isTrue(isDelGroupMember, CommonErrorEnum.SYSTEM_ERROR);
            // 4.4 删除消息记录 (逻辑删除)
            Boolean isDelMessage = messageDao.removeByRoomId(roomId, memberUidList);
            AssertUtil.isTrue(isDelMessage, CommonErrorEnum.SYSTEM_ERROR);
            // TODO 这里也可以告知群成员 群聊已被删除的消息
        } else {
            // 4.5 删除会话
            Boolean isDelContact = contactDao.removeByRoomId(roomId, Collections.singletonList(uid));
            AssertUtil.isTrue(isDelContact, CommonErrorEnum.SYSTEM_ERROR);
            // 4.6 删除群成员
            Boolean isDelGroupMember = groupMemberDao.removeByGroupId(roomGroup.getId(), Collections.singletonList(uid));
            AssertUtil.isTrue(isDelGroupMember, CommonErrorEnum.SYSTEM_ERROR);
            // 4.7 发送移除事件告知群成员
            List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberRemoveWS(roomGroup.getRoomId(), uid);
            pushService.sendPushMsg(ws, memberUidList);
            groupMemberCache.evictMemberUidList(room.getId());
        }
    }
}

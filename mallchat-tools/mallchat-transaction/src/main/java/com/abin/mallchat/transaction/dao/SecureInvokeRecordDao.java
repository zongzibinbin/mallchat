package com.abin.mallchat.transaction.dao;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.abin.mallchat.transaction.domain.entity.SecureInvokeRecord;
import com.abin.mallchat.transaction.mapper.SecureInvokeRecordMapper;
import com.abin.mallchat.transaction.service.SecureInvokeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Description:
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-08-06
 */
@Component
public class SecureInvokeRecordDao extends ServiceImpl<SecureInvokeRecordMapper, SecureInvokeRecord> {

    public List<SecureInvokeRecord> getWaitRetryRecords() {
        Date now = new Date();
        //查2分钟前的失败数据。避免刚入库的数据被查出来
        DateTime afterTime = DateUtil.offsetMinute(now, -(int) SecureInvokeService.RETRY_INTERVAL_MINUTES);
        return lambdaQuery()
                .eq(SecureInvokeRecord::getStatus, SecureInvokeRecord.STATUS_WAIT)
                .lt(SecureInvokeRecord::getNextRetryTime, new Date())
                .lt(SecureInvokeRecord::getCreateTime, afterTime)
                .list();
    }
}

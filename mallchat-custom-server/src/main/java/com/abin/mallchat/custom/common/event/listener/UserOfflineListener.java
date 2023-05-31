package com.abin.mallchat.custom.common.event.listener;

import com.abin.mallchat.common.common.event.UserOfflineEvent;
import com.abin.mallchat.common.user.dao.UserDao;
import com.abin.mallchat.common.user.domain.entity.User;
import com.abin.mallchat.common.user.service.cache.UserCache;
import com.abin.mallchat.custom.user.service.WebSocketService;
import com.abin.mallchat.custom.user.service.adapter.WSAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户下线监听器
 *
 * @author zhongzb create on 2022/08/26
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserOfflineListener {
    private final WebSocketService webSocketService;
    private final UserDao userDao;
    private final UserCache userCache;
    private final WSAdapter wsAdapter;

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveRedisAndPush(UserOfflineEvent event) {
        User user = event.getUser();
        userCache.offline(user.getId(), user.getLastOptTime());
        //推送给所有在线用户，该用户下线
        webSocketService.sendToAllOnline(wsAdapter.buildOfflineNotifyResp(event.getUser()), event.getUser().getId());
    }

    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void saveDB(UserOfflineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLastOptTime(user.getLastOptTime());
        userDao.updateById(update);
    }

}

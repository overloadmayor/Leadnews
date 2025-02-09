package com.heima.utils.thread;

import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.users.pojos.ApUser;

public class AppThreadLocalUtil {
    private final static ThreadLocal<ApUser> threadLocal = new ThreadLocal<>();

    public static ApUser getUser() {
        return threadLocal.get();
    }
    public static void setUser(ApUser user) {
        threadLocal.set(user);
    }
    public static void clear() {
        threadLocal.remove();
    }
}

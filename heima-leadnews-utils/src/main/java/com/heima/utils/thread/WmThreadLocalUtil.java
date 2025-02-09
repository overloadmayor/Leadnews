package com.heima.utils.thread;

import com.heima.model.wemedia.pojos.WmUser;

public class WmThreadLocalUtil {
    private final static ThreadLocal<WmUser> threadLocal = new ThreadLocal<>();

    public static WmUser getUser() {
        return threadLocal.get();
    }
    public static void setUser(WmUser user) {
        threadLocal.set(user);
    }
    public static void clear() {
        threadLocal.remove();
    }
}

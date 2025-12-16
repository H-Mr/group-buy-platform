package cn.hjw.dev.platform.types.utils;

/**
 * 用户上下文，用于在一次请求的线程内传递用户信息
 * 使用 ThreadLocal 保证线程安全
 */
public class UserContext {

    private static final ThreadLocal<String> userThreadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     */
    public static void setUserId(String userId) {
        userThreadLocal.set(userId);
    }

    /**
     * 获取当前线程的用户ID
     */
    public static String getUserId() {
        return userThreadLocal.get();
    }

    /**
     * 清除上下文，防止内存泄漏（这点很重要，尤其是在使用线程池时）
     */
    public static void remove() {
        userThreadLocal.remove();
    }
}

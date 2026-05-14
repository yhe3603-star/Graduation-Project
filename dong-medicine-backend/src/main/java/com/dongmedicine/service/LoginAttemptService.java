package com.dongmedicine.service;

/**
 * 登录尝试服务接口
 *
 * <p>用于跟踪用户登录失败次数并在超过阈值时临时锁定账户，
 * 防止暴力破解攻击。锁定信息存储在 Redis 中，自动过期。</p>
 */
public interface LoginAttemptService {

    /**
     * 记录一次登录失败
     *
     * @param username 用户名
     */
    void recordFailure(String username);

    /**
     * 登录成功后清除失败记录
     *
     * @param username 用户名
     */
    void recordSuccess(String username);

    /**
     * 判断账户是否被锁定
     *
     * @param username 用户名
     * @return true 表示账户已被锁定
     */
    boolean isLocked(String username);

    /**
     * 获取剩余登录尝试次数
     *
     * @param username 用户名
     * @return 剩余尝试次数，未锁定时返回最大尝试次数
     */
    int getRemainingAttempts(String username);

    /**
     * 获取账户锁定剩余秒数
     *
     * @param username 用户名
     * @return 锁定剩余秒数，未锁定时返回 0
     */
    long getLockoutRemainingSeconds(String username);
}

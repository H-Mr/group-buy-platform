package cn.hjw.dev.platform.infrastructure.redis;

import org.redisson.api.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * Redis 服务
 *
 * @author Fuzhengwei hjw.dev.cn @小傅哥
 */
public interface IRedisService {

    /**
     * 设置指定 key 的值
     *
     * @param key   键
     * @param value 值
     */
    <T> void setValue(String key, T value);

    /**
     * 设置指定 key 的值
     *
     * @param key     键
     * @param value   值
     * @param expired 过期时间(毫秒)
     */
    <T> void setValue(String key, T value, long expired);

    /**
     * 获取指定 key 的值
     *
     * @param key 键
     * @return 值
     */
    <T> T getValue(String key);

    /**
     * 获取队列
     *
     * @param key 键
     * @param <T> 泛型
     * @return 队列
     */
    <T> RQueue<T> getQueue(String key);

    /**
     * 加锁队列
     *
     * @param key 键
     * @param <T> 泛型
     * @return 队列
     */
    <T> RBlockingQueue<T> getBlockingQueue(String key);

    /**
     * 延迟队列
     *
     * @param rBlockingQueue 加锁队列
     * @param <T>            泛型
     * @return 队列
     */
    <T> RDelayedQueue<T> getDelayedQueue(RBlockingQueue<T> rBlockingQueue);

    /**
     * 设置值
     *
     * @param key   key 键
     * @param value 值
     */
    void setAtomicLong(String key, long value);

    /**
     * 获取值
     *
     * @param key key 键
     */
    Long getAtomicLong(String key);

    /**
     * 自增 Key 的值；1、2、3、4
     *
     * @param key 键
     * @return 自增后的值
     */
    long incr(String key);

    /**
     * 指定值，自增 Key 的值；1、2、3、4
     *
     * @param key 键
     * @return 自增后的值
     */
    long incrBy(String key, long delta);

    /**
     * 自减 Key 的值；1、2、3、4
     *
     * @param key 键
     * @return 自增后的值
     */
    long decr(String key);

    /**
     * 指定值，自增 Key 的值；1、2、3、4
     *
     * @param key 键
     * @return 自增后的值
     */
    long decrBy(String key, long delta);


    /**
     * 移除指定 key 的值
     *
     * @param key 键
     */
    void remove(String key);

    /**
     * 判断指定 key 的值是否存在
     *
     * @param key 键
     * @return true/false
     */
    boolean isExists(String key);

    /**
     * 将指定的值添加到集合中
     *
     * @param key   键
     * @param value 值
     */
    void addToSet(String key, String value);

    /**
     * 判断指定的值是否是集合的成员
     *
     * @param key   键
     * @param value 值
     * @return 如果是集合的成员返回 true，否则返回 false
     */
    boolean isSetMember(String key, String value);

    /**
     * 将指定的值添加到列表中
     *
     * @param key   键
     * @param value 值
     */
    void addToList(String key, String value);

    /**
     * 获取列表中指定索引的值
     *
     * @param key   键
     * @param index 索引
     * @return 值
     */
    String getFromList(String key, int index);

    /**
     * 获取Map
     *
     * @param key 键
     * @return 值
     */
    <K, V> RMap<K, V> getMap(String key);

    /**
     * 将指定的键值对添加到哈希表中
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     */
    void addToMap(String key, String field, String value);

    /**
     * 获取哈希表中指定字段的值
     *
     * @param key   键
     * @param field 字段
     * @return 值
     */
    String getFromMap(String key, String field);

    /**
     * 获取哈希表中指定字段的值
     *
     * @param key   键
     * @param field 字段
     * @return 值
     */
    <K, V> V getFromMap(String key, K field);

    /**
     * 将指定的值添加到有序集合中
     *
     * @param key   键
     * @param value 值
     */
    void addToSortedSet(String key, String value);

    /**
     * 获取 Redis 锁（可重入锁）
     *
     * @param key 键
     * @return Lock
     */
    RLock getLock(String key);

    /**
     * 获取 Redis 锁（公平锁）
     *
     * @param key 键
     * @return Lock
     */
    RLock getFairLock(String key);

    /**
     * 获取 Redis 锁（读写锁）
     *
     * @param key 键
     * @return RReadWriteLock
     */
    RReadWriteLock getReadWriteLock(String key);

    /**
     * 获取 Redis 信号量
     *
     * @param key 键
     * @return RSemaphore
     */
    RSemaphore getSemaphore(String key);

    /**
     * 获取 Redis 过期信号量
     * <p>
     * 基于Redis的Redisson的分布式信号量（Semaphore）Java对象RSemaphore采用了与java.util.concurrent.Semaphore相似的接口和用法。
     * 同时还提供了异步（Async）、反射式（Reactive）和RxJava2标准的接口。
     *
     * @param key 键
     * @return RPermitExpirableSemaphore
     */
    RPermitExpirableSemaphore getPermitExpirableSemaphore(String key);

    /**
     * 闭锁
     *
     * @param key 键
     * @return RCountDownLatch
     */
    RCountDownLatch getCountDownLatch(String key);

    /**
     * 布隆过滤器
     *
     * @param key 键
     * @param <T> 存放对象
     * @return 返回结果
     */
    <T> RBloomFilter<T> getBloomFilter(String key);

    Boolean setNx(String key);

    Boolean setNx(String key, long expired, TimeUnit timeUnit);

    RBitSet getBitSet(String key);

    /**
     * 使用哈希函数（MD5）：MD5 是一种常见的哈希算法，能够将输入（如用户 ID）映射为一个固定长度的字节数组，确保分布均匀。
     * 转换为正整数：通过 BigInteger 将哈希值转换为正整数，避免负数问题。
     * 取模操作：通过对一个固定范围（如 Integer.MAX_VALUE）取模，确保结果在指定范围内，适合用作索引。
     * 这种方法的优点是：
     * 分布均匀：MD5 的输出具有良好的随机性，能够减少哈希冲突。
     * 可扩展性：适合在分布式环境中将数据分配到不同的分片或节点。
     *
     * 1. MurmurHash
     * MurmurHash 是一种非加密哈希函数，主要用于高性能场景。它的特点是：
     * 高效性：计算速度非常快，适合对性能要求较高的场景。
     * 分布性好：哈希值分布均匀，冲突率低。
     * 非加密：不适合需要安全性的场景，因为它不是加密安全的。
     * 适用场景：
     * 分布式系统中的数据分片。
     * 哈希表的键值映射。
     * 需要快速生成哈希值的场景。
     *
     * 2. SHA-256
     * SHA-256 是一种加密安全的哈希算法，属于 SHA-2 系列。它的特点是：
     * 安全性高：抗碰撞性强，适合需要数据完整性验证的场景。
     * 输出长度固定：生成 256 位（32 字节）的哈希值。
     * 性能较慢：计算复杂度高，适合对安全性要求高的场景。
     * 适用场景：
     * 数据完整性校验。
     * 数字签名。
     * 密码存储。
     * @param userId
     * @return
     */
    default int getIndexFromUserId(String userId) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(userId.getBytes(StandardCharsets.UTF_8));
            // 将哈希字节数组转换为正整数
            BigInteger bigInt = new BigInteger(1, hashBytes);
            // 取模以确保索引在合理范围内
            return bigInt.mod(BigInteger.valueOf(Integer.MAX_VALUE)).intValue();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

}

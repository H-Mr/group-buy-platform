package cn.hjw.dev.platform.infrastructure.dao;

import cn.hjw.dev.platform.infrastructure.dao.po.PayOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IOrderDao {

    void insert(PayOrder payOrder);

    PayOrder queryUnPayOrder(PayOrder payOrder);

    int updateOrderPayInfo(PayOrder payOrder);

    void changeOrderPaySuccess(PayOrder payOrderReq);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose();

/**
 * 1. 悲观锁（FOR UPDATE）必须依赖事务
 * MySQL 的行锁（FOR UPDATE）是在事务生命周期内生效的：
 * 如果没有开启事务，SELECT ... FOR UPDATE会被当作 “普通查询” 执行（不会加锁），无法达到 “线程排队” 的效果；
 * 只有在事务中执行SELECT ... FOR UPDATE，才会持有行锁，直到事务提交 / 回滚后释放锁。
 * 最终落库的方法，使用悲观锁
 */
    PayOrder queryPayOrderIdForUpdate(PayOrder payOrder);

    PayOrder queryPayOrderById(String orderId);
}

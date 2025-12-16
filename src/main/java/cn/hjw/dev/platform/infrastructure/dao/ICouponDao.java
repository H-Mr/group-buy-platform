package cn.hjw.dev.platform.infrastructure.dao;
import cn.hjw.dev.platform.infrastructure.dao.po.Coupon;
import cn.hjw.dev.platform.infrastructure.dao.po.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ICouponDao {
    // 查询用户所有未使用的优惠券关联记录
    // SQL: SELECT * FROM user_coupon WHERE user_id = #{userId} AND status = 0
    List<UserCoupon> queryUnusedUserCoupons(String userId);

    // 根据ID查询优惠券详情
    // SQL: SELECT * FROM coupon WHERE coupon_id = #{couponId}
    Coupon queryCouponById(String couponId);

    // 锁定/使用优惠券
    // SQL: UPDATE user_coupon SET status = 1, order_id = #{orderId}, use_time = NOW() WHERE id = #{id} AND status = 0
    int lockUserCoupon(Long id, String orderId);
}
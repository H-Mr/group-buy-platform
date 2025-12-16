package cn.hjw.dev.platform.infrastructure.dao;

import cn.hjw.dev.platform.infrastructure.dao.po.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserDao {

    // 根据 OpenID 查询
    User queryUserByOpenId(String openid);

    // 插入新用户
    int insert(User user);


}

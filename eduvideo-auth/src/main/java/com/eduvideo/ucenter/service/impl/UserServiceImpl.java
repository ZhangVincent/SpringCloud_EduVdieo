package com.eduvideo.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduvideo.ucenter.mapper.XcUserMapper;
import com.eduvideo.ucenter.model.po.XcUser;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author zkp15
 * @version 1.0
 * @description 实现springsecurity中UserDetailsService类下的接口方法
 * @date 2023/6/23 22:39
 */
@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private XcUserMapper xcUserMapper;

    /***
    * @description 根据用户名查密码，返回密码或者null，在DaoAuthenticationProvider类中被调用
    * @param s
    * @return org.springframework.security.core.userdetails.UserDetails
    * @author zkp15
    * @date 2023/6/23 22:43
    */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, s));
        if(user==null){
            //返回空表示用户不存在
            return null;
        }
        //取出数据库存储的正确密码
        String password  =user.getPassword();
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities= {"p1"};
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String jsonString = JSON.toJSONString(user);
        //创建UserDetails对象,权限信息待实现授权功能时再向UserDetail中加入
        UserDetails userDetails = User.withUsername(jsonString).password(password).authorities(authorities).build();

        return userDetails;

    }
}

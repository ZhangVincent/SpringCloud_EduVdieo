package com.eduvideo.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eduvideo.ucenter.mapper.XcMenuMapper;
import com.eduvideo.ucenter.mapper.XcUserMapper;
import com.eduvideo.ucenter.model.dto.AuthParamsDto;
import com.eduvideo.ucenter.model.dto.XcUserExt;
import com.eduvideo.ucenter.model.po.XcMenu;
import com.eduvideo.ucenter.model.po.XcUser;
import com.eduvideo.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    // spring的上下文对象，可以根据名称返回对应的Bean
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private XcMenuMapper xcMenuMapper;

    /***
     * @description 传入的用户名统一使用AuthParamsDto请求体，整合成UserDetails对象返回，在DaoAuthenticationProvider类中被调用
     * @param s
     * @return org.springframework.security.core.userdetails.UserDetails
     * @author zkp15
     * @date 2023/6/23 22:43
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        AuthParamsDto authParamsDto = null;
        try {
            //将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }

        // 根据传入的校验类型获取对应名称的Bean，分别有password，sms，wx
        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_authservice", AuthService.class);
        XcUserExt user = authService.execute(authParamsDto);

        return getUserPrincipal(user);
    }


    /***
     * @description 根据XcUserExt对象封装成UserDetails对象并返回
     * @param user
     * @return org.springframework.security.core.userdetails.UserDetails
     * @author zkp15
     * @date 2023/6/24 16:18
     */
    public UserDetails getUserPrincipal(XcUserExt user) {
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(user.getId());
        List<String> permissions = new ArrayList<>();
        if (xcMenus.size() == 0) {
            //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
            permissions.add("p1");
        } else {
            xcMenus.stream().forEach(c -> permissions.add(c.getCode()));
        }
        String[] authorities = permissions.toArray(new String[0]);
        //将用户权限放在XcUserExt中
        user.setPermissions(permissions);

        String password = user.getPassword();
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();
        return userDetails;
    }

}


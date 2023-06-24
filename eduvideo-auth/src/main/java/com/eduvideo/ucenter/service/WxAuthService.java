package com.eduvideo.ucenter.service;

import com.eduvideo.ucenter.model.po.XcUser;

/**
 * @author zkp15
 * @version 1.0
 * @description 微信认证接口
 * @date 2023/6/24 20:31
 */
public interface WxAuthService {
    /***
    * @description 通过授权码获取令牌
    * @param code
    * @return com.eduvideo.ucenter.model.po.XcUser
    * @author zkp15
    * @date 2023/6/24 20:33
    */
    public XcUser wxAuth(String code);
}

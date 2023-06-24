package com.eduvideo.ucenter.service;

import com.eduvideo.ucenter.model.dto.AuthParamsDto;
import com.eduvideo.ucenter.model.dto.XcUserExt;

/**
 * @author zkp15
 * @version 1.0
 * @description 认证接口
 * @date 2023/6/24 16:05
 */
public interface AuthService {

    /**
    * @description 认证方法，每一种认证方法都需要实现这个接口
    * @param authParamsDto
    * @return com.eduvideo.ucenter.model.dto.XcUserExt
    * @author zkp15
    * @date 2023/6/24 16:06
    */
    XcUserExt execute(AuthParamsDto authParamsDto);
}

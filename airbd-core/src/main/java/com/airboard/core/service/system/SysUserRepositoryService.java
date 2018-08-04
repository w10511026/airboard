package com.airboard.core.service.system;


import com.airboard.core.base.BaseRepositoryService;
import com.airboard.core.model.system.SysUser;

import java.util.List;

public interface SysUserRepositoryService extends BaseRepositoryService<SysUser, Long> {

    List<SysUser> getByUserName(String userName);

    List<SysUser> getByUserNameAndPassWord(String userName, String passWord);

}
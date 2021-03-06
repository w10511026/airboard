package com.airboard.api.service.system;

import com.airboard.api.model.system.SysPermission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *  服务类
 * @author Wangshuo
 * @since 2018-08-07
 */
public interface SysPermissionService extends IService<SysPermission> {

    Integer countByPermission(String permission);
}

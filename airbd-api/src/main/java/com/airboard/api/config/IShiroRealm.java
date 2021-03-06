package com.airboard.api.config;

import com.airboard.client.enums.SysUserStatusEnum;
import com.airboard.core.util.JWTToken;
import com.airboard.core.util.JWTUtil;
import com.airboard.api.service.system.SysUserService;
import com.airboard.client.dto.system.SysRoleDTO;
import com.airboard.client.dto.system.SysUserDTO;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * @Description: 自定义的shiro realm，用作登录用户的认证和授权
 */
public class IShiroRealm extends AuthorizingRealm {

    /**
     * 初始化myshiroRealm的时候需要用到数据库（基于其它方式访问数据的realm，不在讨论范围），
     * 于是不可避免的用到dataSource。如果在初始化shiroRealm的时候容器没有dataSource，
     * 容器就会为我们创建一个，就会产生循环依赖的问题
     * 如果在初始化shiroRealm的时候，容器已经有了dataSource，就不会有循环依赖的问题
     * 我的方法是让shiroRealm初始化的时候某个类变成“懒加载”
     */
    @Lazy
    @Autowired
    SysUserService sysUserService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * @Description: 认证方法
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getCredentials();
        String userName = JWTUtil.getUserName(token);
        if (userName == null) {
            throw new AuthenticationException("token invalid");
        }
        List<SysUserDTO> userList = sysUserService.getByUserName(userName);
        if (CollectionUtils.isEmpty(userList)) {
            throw new AuthenticationException("User didn't existed!");
        }
        SysUserDTO sysUser = userList.get(0);
        if (sysUser.getStatus().equals(SysUserStatusEnum.UNABLE.type)) {
            throw new AuthenticationException("Account is locked!");
        }
        if (!JWTUtil.verify(token, userName, sysUser.getPassWord())) {
            throw new UnauthorizedException("Username or password error");
        }
        return new SimpleAuthenticationInfo(sysUser, token, ByteSource.Util.bytes(sysUser.getSalt()), getName());
    }

    /**
     * @Description: 授权方法
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SysUserDTO sysUser = (SysUserDTO) principalCollection.getPrimaryPrincipal();
        List<SysRoleDTO> roleVOS = sysUser.getRoles();
        List<String> roleNameList = Lists.newArrayList();
        List<String> permissionNameList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(roleVOS)) {
            roleVOS.forEach(item -> {
                roleNameList.add(item.getName());
                if (CollectionUtils.isNotEmpty(item.getPermissions())) {
                    item.getPermissions().forEach(per -> {
                        permissionNameList.add(per.getName());
                    });
                }
            });
        }
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.addRoles(roleNameList);
        authorizationInfo.addStringPermissions(permissionNameList);
        return authorizationInfo;
    }

}

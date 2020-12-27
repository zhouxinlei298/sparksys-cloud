package com.github.sparkzxl.oauth.infrastructure.repository;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.sparkzxl.core.context.BaseContextHandler;
import com.github.sparkzxl.core.tree.TreeUtils;
import com.github.sparkzxl.database.entity.RemoteData;
import com.github.sparkzxl.oauth.domain.model.aggregates.*;
import com.github.sparkzxl.oauth.domain.repository.IAuthUserRepository;
import com.github.sparkzxl.oauth.infrastructure.convert.AuthRoleConvert;
import com.github.sparkzxl.oauth.infrastructure.convert.AuthUserConvert;
import com.github.sparkzxl.oauth.infrastructure.entity.*;
import com.github.sparkzxl.oauth.infrastructure.entity.RoleResource;
import com.github.sparkzxl.oauth.infrastructure.mapper.*;
import com.github.sparkzxl.database.annonation.InjectionResult;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * description：用户仓储层实现类
 *
 * @author zhouxinlei
 * @date 2020/6/5 8:45 下午
 */
@AllArgsConstructor
@Repository
@Slf4j
public class AuthUserRepository implements IAuthUserRepository {

    public final AuthUserMapper authUserMapper;
    private final UserRoleMapper userRoleMapper;
    private final AuthRoleMapper authRoleMapper;
    private final RoleAuthorityMapper roleAuthorityMapper;
    private final AuthResourceMapper authResourceMapper;
    private final AuthMenuMapper authMenuMapper;
    private final CoreOrgMapper coreOrgMapper;

    @Override
    public AuthUser selectById(Long id) {
        return authUserMapper.selectById(id);
    }

    @Override
    @InjectionResult
    public AuthUser selectByAccount(String account) {
        QueryWrapper<AuthUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AuthUser::getAccount, account);
        queryWrapper.lambda().eq(AuthUser::getStatus, 1);
        return authUserMapper.selectOne(queryWrapper);
    }

    @Override
    public List<String> getAuthUserPermissions(Long id) {
        return authUserMapper.getAuthUserPermissions(id);
    }

    @Override
    public List<String> getAuthUserRoles(Long id) {
        return authUserMapper.getAuthUserRoles(id);
    }

    @Override
    public List<RoleResource> getRoleResourceList() {
        return authUserMapper.getRoleResourceList();
    }

    @Override
    public void deleteUserRelation(List<Long> ids) {
        userRoleMapper.delete(new LambdaUpdateWrapper<UserRole>().in(UserRole::getUserId, ids));
    }

    @Override
    public LoginAuthUser getLoginAuthUser(Long id) {
        String account = BaseContextHandler.getAccount();
        Long userId = BaseContextHandler.getUserId(Long.class);
        String name = BaseContextHandler.getName();
        log.info("当前登录用户信息，account：{}，userId：{}，name：{}", account, userId, name);
        AuthUser authUser = selectById(id);
        LoginAuthUser loginAuthUser = AuthUserConvert.INSTANCE.convertLoginAuthUser(authUser);
        List<Long> roleIds =
                userRoleMapper.selectList(new LambdaUpdateWrapper<UserRole>().eq(UserRole::getUserId, id)).stream().map(UserRole::getRoleId)
                        .collect(Collectors.toList());
        List<RoleAuthority> roleAuthorities =
                roleAuthorityMapper.selectList(new LambdaQueryWrapper<RoleAuthority>().in(RoleAuthority::getRoleId, roleIds)
                        .groupBy(RoleAuthority::getAuthorityId, RoleAuthority::getAuthorityType, RoleAuthority::getRoleId));
        List<Long> authorityIds = roleAuthorityMapper.selectList(new LambdaQueryWrapper<RoleAuthority>().in(RoleAuthority::getRoleId,
                roleIds)).stream().filter(x -> "RESOURCE".equals(x.getAuthorityType()))
                .map(RoleAuthority::getAuthorityId).collect(Collectors.toList());
        Map<Long, Long> roleAuthorityIdMap =
                roleAuthorities.stream().collect(Collectors.toMap(RoleAuthority::getAuthorityId, RoleAuthority::getRoleId));
        List<AuthResource> resourceList = authResourceMapper.selectBatchIds(authorityIds);
        List<LoginPermission> loginPermissionList = Lists.newArrayList();
        Map<Long, List<LoginPermission>> loginPermissionMap;
        if (CollectionUtils.isNotEmpty(resourceList)) {
            resourceList.forEach(resource -> {
                LoginPermission loginPermission = new LoginPermission();
                loginPermission.setPermissionId(resource.getCode());
                loginPermission.setPermissionName(resource.getName());
                loginPermission.setRoleId(roleAuthorityIdMap.get(resource.getId()));
                loginPermissionList.add(loginPermission);
            });
        }
        List<Long> menuIds = roleAuthorityMapper.selectList(new LambdaQueryWrapper<RoleAuthority>().in(RoleAuthority::getRoleId,
                roleIds)).stream().filter(x -> "MENU".equals(x.getAuthorityType()))
                .map(RoleAuthority::getAuthorityId).collect(Collectors.toList());

        List<AuthMenu> menuList = authMenuMapper.selectBatchIds(menuIds);
        if (CollectionUtils.isNotEmpty(menuList)) {
            menuList.forEach(menu -> {
                LoginPermission loginPermission = new LoginPermission();
                loginPermission.setPermissionId(menu.getCode());
                loginPermission.setPermissionName(menu.getLabel());
                loginPermission.setRoleId(roleAuthorityIdMap.get(menu.getId()));
                loginPermissionList.add(loginPermission);
            });
        }

        loginPermissionMap = loginPermissionList.stream().collect(Collectors.groupingBy(LoginPermission::getRoleId));
        List<AuthRole> roleList = authRoleMapper.selectBatchIds(roleIds);
        List<LoginRole> loginRoles = AuthRoleConvert.INSTANCE.convertLoginRoles(roleList);
        Map<Long, List<LoginPermission>> finalLoginPermissionMap = loginPermissionMap;
        loginRoles.forEach(x -> x.setPermissions(finalLoginPermissionMap.get(x.getId())));
        loginAuthUser.setRoles(loginRoles);
        return loginAuthUser;
    }


    @Override
    @InjectionResult
    public List<AuthUser> getAuthUserList(AuthUser authUser) {
        LambdaQueryWrapper<AuthUser> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(authUser.getAccount())) {
            queryWrapper.like(AuthUser::getAccount, authUser.getAccount());
        }
        if (StringUtils.isNotEmpty(authUser.getName())) {
            queryWrapper.like(AuthUser::getName, authUser.getName());
        }
        if (ObjectUtils.isNotEmpty(authUser.getStatus())) {
            queryWrapper.eq(AuthUser::getStatus, authUser.getStatus());
        }
        if (ObjectUtils.isNotEmpty(authUser.getSex()) && ObjectUtils.isNotEmpty(authUser.getSex().getCode())) {
            queryWrapper.eq(AuthUser::getSex, authUser.getSex());
        }
        if (ObjectUtils.isNotEmpty(authUser.getNation()) && StringUtils.isNotEmpty(authUser.getNation().getKey())) {
            queryWrapper.eq(AuthUser::getNation, authUser.getNation());
        }
        return authUserMapper.selectList(queryWrapper);
    }

    @Override
    public AuthUserBasicInfo getAuthUserBasicInfo(Long userId) {
        AuthUser authUser = authUserMapper.getById(userId);
        AuthUserBasicInfo authUserBasicInfo = AuthUserConvert.INSTANCE.convertAuthUserBasicInfo(authUser);
        RemoteData<Long, CoreOrg> org = authUser.getOrg();
        List<OrgBasicInfo> orgTreeList = CollUtil.newArrayList();
        if (ObjectUtils.isNotEmpty(org)) {
            CoreOrg data = org.getData();
            if (ObjectUtils.isNotEmpty(data)) {
                OrgBasicInfo orgBasicInfo = new OrgBasicInfo();
                orgBasicInfo.setId(data.getId());
                orgBasicInfo.setLabel(data.getLabel());
                orgBasicInfo.setParentId(data.getParentId());
                orgBasicInfo.setSortValue(data.getSortValue());
                orgTreeList.add(orgBasicInfo);
                if (data.getParentId() != 0) {
                    CoreOrg coreOrg = coreOrgMapper.selectById(data.getParentId());
                    OrgBasicInfo parentOrgBasicInfo = new OrgBasicInfo();
                    parentOrgBasicInfo.setId(coreOrg.getId());
                    parentOrgBasicInfo.setLabel(coreOrg.getLabel());
                    parentOrgBasicInfo.setParentId(coreOrg.getParentId());
                    parentOrgBasicInfo.setSortValue(coreOrg.getSortValue());
                    orgTreeList.add(parentOrgBasicInfo);
                }
                authUserBasicInfo.setOrg(TreeUtils.buildTree(orgTreeList));
            }
        }

        List<Long> roleIds =
                userRoleMapper.selectList(new LambdaUpdateWrapper<UserRole>().eq(UserRole::getUserId, userId)).stream().map(UserRole::getRoleId)
                        .collect(Collectors.toList());
        List<AuthRole> roleList = authRoleMapper.selectBatchIds(roleIds);
        List<RoleBasicInfo> roleBasicInfos = AuthRoleConvert.INSTANCE.convertRoleBasicInfo(roleList);
        authUserBasicInfo.setRoleBasicInfos(roleBasicInfos);
        List<RoleAuthority> roleAuthorities =
                roleAuthorityMapper.selectList(new LambdaQueryWrapper<RoleAuthority>().in(RoleAuthority::getRoleId, roleIds)
                        .groupBy(RoleAuthority::getAuthorityId, RoleAuthority::getAuthorityType, RoleAuthority::getRoleId));
        List<Long> authorityIds = roleAuthorities.stream().filter(x -> "RESOURCE".equals(x.getAuthorityType()))
                .map(RoleAuthority::getAuthorityId).collect(Collectors.toList());
        Map<Long, Long> roleAuthorityIdMap =
                roleAuthorities.stream().collect(Collectors.toMap(RoleAuthority::getAuthorityId, RoleAuthority::getRoleId));
        // 获取用户资源列表
        List<AuthResource> resourceList = authResourceMapper.selectBatchIds(authorityIds);
        List<ResourceBasicInfo> resourceBasicInfos = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(resourceList)) {
            resourceList.forEach(resource -> {
                ResourceBasicInfo resourceBasicInfo = new ResourceBasicInfo();
                resourceBasicInfo.setCode(resource.getCode());
                resourceBasicInfo.setName(resource.getName());
                resourceBasicInfo.setRoleId(roleAuthorityIdMap.get(resource.getId()));
                resourceBasicInfos.add(resourceBasicInfo);
            });
        }
        authUserBasicInfo.setResourceBasicInfos(resourceBasicInfos);

        List<Long> menuIds = roleAuthorities.stream().filter(x -> "MENU".equals(x.getAuthorityType()))
                .map(RoleAuthority::getAuthorityId).collect(Collectors.toList());

        List<AuthMenu> menuList = authMenuMapper.selectBatchIds(menuIds);
        List<MenuBasicInfo> menuBasicInfos = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(menuList)) {
            menuList.forEach(menu -> {
                MenuBasicInfo menuBasicInfo = new MenuBasicInfo();
                menuBasicInfo.setId(menu.getId());
                menuBasicInfo.setLabel(menu.getLabel());
                menuBasicInfo.setParentId(menu.getParentId());
                menuBasicInfo.setSortValue(menu.getSortValue());
                menuBasicInfos.add(menuBasicInfo);
            });
            authUserBasicInfo.setMenuBasicInfos(menuBasicInfos);
            authUserBasicInfo.setMenuTree(TreeUtils.buildTree(menuBasicInfos));
        }
        return authUserBasicInfo;
    }
}

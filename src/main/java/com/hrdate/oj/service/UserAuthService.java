package com.hrdate.oj.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hrdate.oj.dto.user.UserDetailDTO;
import com.hrdate.oj.entity.UserAuthModel;
import com.hrdate.oj.entity.UserInfoModel;
import com.hrdate.oj.mapper.UserAuthMapper;
import com.hrdate.oj.utils.IpUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 用户
 * @author: huangrendi
 * @date: 2022-11-08
 **/

@Service
public class UserAuthService extends ServiceImpl<UserAuthMapper, UserAuthModel> implements UserDetailsService {
    @Resource
    private HttpServletRequest request;
    @Autowired
    private UserInfoService userInfoService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<UserAuthModel> queryWrapper = Wrappers.lambdaQuery();
                queryWrapper.eq(StringUtils.isNoneBlank(username), UserAuthModel::getUsername, username);
        // 根据用户名username查询
        UserAuthModel userAuthModel = this.getOne(queryWrapper,false);
        if(userAuthModel == null){
            // 根据邮箱email查询
            userAuthModel = this.getOne(new LambdaQueryWrapper<UserAuthModel>()
                    .eq(StringUtils.isNoneBlank(username), UserAuthModel::getEmail, username), false);
        }
        // 用户不存在
        if(userAuthModel == null){
            throw new UsernameNotFoundException("用户名[" + username + "]不存在！");
        }
        // 用户登录信息
        return convertUserDetail(userAuthModel, request);
    }

    /**
     * 封装用户登录信息
     *
     * @param userAuthModel    用户账号
     * @param request 请求
     * @return 用户登录信息
     */
    public UserDetailDTO convertUserDetail(UserAuthModel userAuthModel, HttpServletRequest request) {
        // 获取用户ip信息
        String ipAddress = IpUtil.getIpAddress(request);
        String ipSource = IpUtil.getIpSource(ipAddress);

        // 用户信息详情
        UserInfoModel userInfoModel = userInfoService.getById(userAuthModel.getUserInfoId());

        // 构建用户信息
        return UserDetailDTO.builder()
                .id(userAuthModel.getId())
                .username(userAuthModel.getUsername())
                .email(userAuthModel.getEmail())
                .password(null)
                .roleList(userAuthModel.getRoleList())
                .nickname(userInfoModel.getNickname())
                .intro(userInfoModel.getIntro())
                .avatar(userInfoModel.getAvatar())
                .ipAddress(ipAddress)
                .ipSource(ipSource)
                .lastLoginTime(LocalDateTime.now())
                .isDisable(userAuthModel.getIsDisable())
                .build();
    }
}

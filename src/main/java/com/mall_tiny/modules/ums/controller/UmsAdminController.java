package com.mall_tiny.modules.ums.controller;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall_tiny.common.api.CommonPage;
import com.mall_tiny.common.api.CommonResult;
import com.mall_tiny.modules.ums.dto.UmsAdminLoginParam;
import com.mall_tiny.modules.ums.dto.UmsAdminParam;
import com.mall_tiny.modules.ums.dto.UpdateAdminPasswordParam;
import com.mall_tiny.modules.ums.model.UmsAdmin;
import com.mall_tiny.modules.ums.model.UmsRole;
import com.mall_tiny.modules.ums.service.UmsAdminService;
import com.mall_tiny.modules.ums.service.UmsRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 后台用户表 前端控制器
 * </p>
 *
 * @author baosight
 * @since 2024-10-21
 */
@RestController
@Api(tags = "UmsAdminController")
@Tag(name = "UmsAdminController", description = "后台用户管理")
@RequestMapping("/admin")
public class UmsAdminController {

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    private UmsAdminService umsAdminService;

    @Autowired
    private UmsRoleService umsRoleService;

    @ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public CommonResult<UmsAdmin> register(@Validated @RequestBody UmsAdminParam umsAdminParam) {
        UmsAdmin umsAdmin = umsAdminService.register(umsAdminParam);
        if (umsAdmin == null) {
            return CommonResult.failed();
        }
        return CommonResult.success(umsAdmin);
    }

    @ApiOperation(value = "登录，并返回token")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public CommonResult login(@Validated @RequestBody UmsAdminLoginParam umsAdminLoginParam) {
        String token = umsAdminService.login(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
        if(token == null) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        System.out.println(tokenMap);
        // {tokenHead=Bearer , token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTczNTExMjM5NTY4NSwiZXhwIjoxNzM1NzE3MTk1fQ.GlGZMUS71qTTv0rXAnbmezlYqEgvQS8nBU-W9m9W4VST0uEWXlhK1Lisz_QJFUBhfjlNfM_vL6YAvpKnHE-BYQ}
        return CommonResult.success(tokenMap);
    }

    @ApiOperation(value = "刷新token")
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    public CommonResult refreshToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        System.out.println(token);
        String refreshToken = umsAdminService.refreshToken(token);
        if (refreshToken == null) {
            return CommonResult.failed("token已经过期");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }

    @ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult getAdminInfo(Principal principal) {
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        String username = principal.getName();
        UmsAdmin admin = umsAdminService.getAdminByUsername(username);
        Map<String, Object> data = new HashMap<>();
        data.put("username", admin.getUsername());
        data.put("menus", umsRoleService.getMenuList(admin.getId()));
        data.put("icon", admin.getIcon());
        List<UmsRole> roleList = umsAdminService.getRoleList(admin.getId());
        if (CollUtil.isNotEmpty(roleList)) {
            List<String> roles = roleList.stream().map(UmsRole::getName).collect(Collectors.toList());
            data.put("roles", roles);
        }
        return CommonResult.success(data);
    }

    @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public CommonResult logout() {
        return CommonResult.success(null);
    }

    @ApiOperation(value = "根据用户名或姓名分页获取用户列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult<CommonPage<UmsAdmin>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        Page<UmsAdmin> adminList = umsAdminService.list(keyword, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(adminList));
    }

    @ApiOperation(value = "获取指定用户信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<UmsAdmin> getItem(@PathVariable Long id) {
        UmsAdmin admin = umsAdminService.getById(id);
        return CommonResult.success(admin);
    }

    @ApiOperation(value = "修改指定用户信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult update(@PathVariable Long id, @RequestBody UmsAdmin admin) {
        boolean success = umsAdminService.update(id, admin);
        if (success) {
            return CommonResult.success(null);
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "修改制定用户密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public CommonResult updatePassword(@Validated @RequestBody UpdateAdminPasswordParam updateAdminPasswordParam) {
        int status = umsAdminService.updatePassword(updateAdminPasswordParam);
        if (status > 0) {
            return CommonResult.success(status);
        } else if (status == -1) {
            return CommonResult.failed("提交参数不合法");
        } else if (status == -2) {
            return CommonResult.failed("找不到该用户");
        } else if (status == -3) {
            return CommonResult.failed("旧密码错误");
        } else {
            return CommonResult.failed();
        }
    }

    @ApiOperation(value = "删除指定用户信息")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult delete(@PathVariable Long id) {
        boolean success = umsAdminService.delete(id);
        if (success) {
            return CommonResult.success(null);
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "修改账户状态")
    @RequestMapping(value = "/updateStatus/{id}", method = RequestMethod.POST)
    public CommonResult updateStatus(@PathVariable Long id, @RequestParam(value = "status") Integer status) {
        UmsAdmin umsAdmin = new UmsAdmin();
        umsAdmin.setStatus(status);
        boolean success = umsAdminService.update(id, umsAdmin);
        if (success) {
            return CommonResult.success(null);
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "给用户分配角色")
    @RequestMapping(value = "/role/update", method = RequestMethod.POST)
    public CommonResult updateRole(@RequestParam("adminId") Long adminId,
                                   @RequestParam("roleIds") List<Long> roleIds) {
        int count = umsAdminService.updateRole(adminId, roleIds);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    @ApiOperation(value = "获取指定用户的角色")
    @RequestMapping(value = "/role/{adminId}")
    public CommonResult<List<UmsRole>> getRoleList(@PathVariable Long adminId) {
        List<UmsRole> roleList = umsAdminService.getRoleList(adminId);
        return CommonResult.success(roleList);
    }
}


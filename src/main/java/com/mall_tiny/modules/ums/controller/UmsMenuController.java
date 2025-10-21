package com.mall_tiny.modules.ums.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall_tiny.common.api.CommonPage;
import com.mall_tiny.common.api.CommonResult;
import com.mall_tiny.modules.ums.dto.UmsMenuNode;
import com.mall_tiny.modules.ums.model.UmsMenu;
import com.mall_tiny.modules.ums.service.UmsMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 后台菜单表 前端控制器
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
@RestController
@Api(tags = "UmsMenuController")
@Tag(name = "UmsMenuController", description = "后台目录管理")
@RequestMapping("/menu")
public class UmsMenuController {

    @Autowired
    private UmsMenuService umsMenuService;

    @ApiOperation(value = "添加后台目录")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult create(@RequestBody UmsMenu umsMenu) {
        boolean success = umsMenuService.create(umsMenu);
        if (success) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }

    @ApiOperation(value = "修改后台目录")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult update(@PathVariable Long id,
                               @RequestBody UmsMenu umsMenu) {
        boolean success = umsMenuService.update(id, umsMenu);
        if (success) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }

    @ApiOperation(value = "根据id获取目录详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<UmsMenu> getItem(@PathVariable Long id) {
        UmsMenu umsMenu = umsMenuService.getById(id);
        return CommonResult.success(umsMenu);
    }

    @ApiOperation(value = "根据id删除后台目录")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult delete(@PathVariable Long id) {
        boolean success = umsMenuService.removeById(id);
        if (success) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }

    @ApiOperation(value = "分页查询后台目录")
    @RequestMapping(value = "/list/{parentId}", method = RequestMethod.GET)
    public CommonResult<CommonPage<UmsMenu>> list(@PathVariable Long parentId,
                                                  @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                  @RequestParam(value = "pageNum", defaultValue = "1") Integer pageMum) {
        Page<UmsMenu> menuList = umsMenuService.list(parentId, pageSize, pageMum);
        return CommonResult.success(CommonPage.restPage(menuList));
    }

    @ApiOperation(value = "树形结构返回所有目录列表")
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public CommonResult<List<UmsMenuNode>> treeList() {
        List<UmsMenuNode> list = umsMenuService.treeList();
        return CommonResult.success(list);
    }

    @ApiOperation(value = "修改目录显示状态")
    @RequestMapping(value = "/updateHidden/{id}", method = RequestMethod.POST)
    public CommonResult updateHidden(@PathVariable Long id, @RequestParam("hidden") Integer hidden) {
        boolean success = umsMenuService.updateHidden(id, hidden);
        if (success) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }
}


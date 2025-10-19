package com.mall_tiny.modules.ums.controller;


import com.mall_tiny.common.api.CommonResult;
import com.mall_tiny.modules.ums.mapper.UmsResourceMapper;
import com.mall_tiny.modules.ums.model.UmsResourceCategory;
import com.mall_tiny.modules.ums.service.UmsResourceCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 资源分类表 前端控制器
 * </p>
 *
 * @author baosight
 * @since 2024-09-13
 */
@RestController
@Api(tags = "UmsResourceCategoryController")
@Tag(name = "UmsResourceCategoryController", description = "后台资源分类管理")
@RequestMapping("/resourceCategory")
public class UmsResourceCategoryController {

    @Autowired
    private UmsResourceCategoryService resourceCategoryService;

    @ApiOperation(value = "查询所有后台资源分类")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public CommonResult<List<UmsResourceCategory>> listAll() {
        List<UmsResourceCategory> list = resourceCategoryService.listAll();
        return CommonResult.success(list);
    }

    @ApiOperation(value = "添加后台资源分类")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult create(@RequestBody UmsResourceCategory resourceCategory) {
        boolean success = resourceCategoryService.create(resourceCategory);
        if (success) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }

    @ApiOperation(value = "修改后台资源分类")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult update(@PathVariable Long id,
                               @RequestBody UmsResourceCategory resourceCategory) {
        resourceCategory.setId(id);
        boolean success = resourceCategoryService.updateById(resourceCategory);
        if (success) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }

    @ApiOperation(value = "根据id删除后台资源")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult delete(@PathVariable Long id) {
        boolean success = resourceCategoryService.removeById(id);
        if (success) {
            return CommonResult.success(null);
        } else {
            return CommonResult.failed();
        }
    }
}


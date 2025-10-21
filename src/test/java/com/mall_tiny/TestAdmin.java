package com.mall_tiny;

import com.mall_tiny.common.api.CommonResult;
import com.mall_tiny.domain.AdminUserDetails;
import com.mall_tiny.modules.ums.controller.UmsAdminController;
import com.mall_tiny.modules.ums.dto.UmsAdminLoginParam;
import com.mall_tiny.modules.ums.dto.UmsAdminParam;
import com.mall_tiny.modules.ums.mapper.UmsAdminMapper;
import com.mall_tiny.modules.ums.mapper.UmsResourceMapper;
import com.mall_tiny.modules.ums.model.UmsAdmin;
import com.mall_tiny.modules.ums.model.UmsResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = {MallTinyApplication.class})
public class TestAdmin {

    @Autowired
    private UmsAdminController adminController;

    @Autowired
    private UmsAdminMapper adminMapper;

    @Autowired
    private UmsResourceMapper resourceMapper;

    @Test
    void testRegister() {
        // 注册用户，不涉及token
        UmsAdminParam umsAdminParam = new UmsAdminParam();
//        umsAdminParam.setUsername("test");
//        umsAdminParam.setPassword("123456");
//        umsAdminParam.setIcon("https://xxx.xx.xx/1.png");
//        umsAdminParam.setEmail("test@example.com");
//        umsAdminParam.setNickName("测试账号");
//        umsAdminParam.setNote("");
        umsAdminParam.setUsername("admin");
        umsAdminParam.setPassword("123456");
        umsAdminParam.setIcon("https://xxx.xx.xx/admin.png");
        umsAdminParam.setEmail("admin@example.com");
        umsAdminParam.setNickName("系统管理员");
        umsAdminParam.setNote("系统管理员");
        adminController.register(umsAdminParam);
    }

    @Test
    void testLogin() {
        // pass
        UmsAdminLoginParam loginParam = new UmsAdminLoginParam();
        loginParam.setUsername("test");
        loginParam.setPassword("123456");
        CommonResult result = adminController.login(loginParam);
        System.out.println(result);
    }

    @Test
    void testRefreshToken() {
        // pass
    }

    @Test
    void testAdminUserDetailsGetAuthorities() {
        // 测试AdminUserDetails类的getAuthorities()的返回结果
        UmsAdmin umsAdmin = adminMapper.selectById(16L);
        System.out.println(umsAdmin);
        List<UmsResource> resourceList = resourceMapper.getResourceList(16L);
        System.out.println(resourceList);
        AdminUserDetails adminUserDetails = new AdminUserDetails(umsAdmin, resourceList);
        System.out.println(adminUserDetails.getAuthorities());
        /**
         * [1:商品品牌管理, 2:商品属性分类管理, 3:商品属性管理]
         */
    }

    @Test
    void testGenerateJwt() {
        // header
        // {"alg":"HS512"}
        // header  --base64-->  eyJhbGciOiJIUzUxMiJ9

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test");
        claims.put("created", new Date());
//        claims.put("created", 1732860097871L);
        Long exp = new Date(System.currentTimeMillis() + 604800 * 1000).getTime()/1000; // 令牌过期时间为当前时间后7天
//        claims.put("exp", 1733464897L);
        claims.put("exp", exp);
        String jwt = TestGenerateJwt.generateJwt(claims);
        System.out.println("Generated JWT: " + jwt);

        // payload  --base64-->  eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTczMjg2MDA5Nzg3MSwiZXhwIjoxNzMzNDY0ODk3fQ
        // signature  --> alg( base64(header) + '.' + base64(payload), secret)
        // ----> eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTczMjg2MDA5Nzg3MSwiZXhwIjoxNzMzNDY0ODk3fQ.qbreR3KtbtwI_vD3ZceZQ1-1SH8wWkhkxRPvf5g-LINsI8Ycbknx3m_KGKxeJN2E4vFTHaufOLArI9l5SejsaA
    }

    @Test
    void verifyJWT() {
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTczMjg2MDA5Nzg3MSwiZXhwIjoxNzMzNDY0ODk3fQ.qnRyVZ48z_L5VFK8Xjg2wNUf29kY7bs_2Nj0Efd2UBicUxNDTx97or8VsOOAGYqoQu5J__attn5swiMPRW98Ag";
        // 在JwtTokenUtil的main中临时测试
    }



}

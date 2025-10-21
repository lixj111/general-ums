package com.mall_tiny.security.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.mall_tiny.domain.AdminUserDetails;
import com.mall_tiny.modules.ums.model.UmsAdmin;
import com.mall_tiny.modules.ums.model.UmsResource;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * JwtToken生成的工具类
 * JWT token的格式：header.payload.signature
 * header的格式（算法、token的类型）：{"alg": "HS512","typ": "JWT"}
 * payload的格式（用户名、创建时间、生成时间）：{"sub":"wang","created":1489079981393,"exp":1489684781}
 * signature的生成算法：HMACSHA512(base64UrlEncode(header)+"."+base64UrlEncode(payload),secret)
 */
public class JwtTokenUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);
    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    /**
     * 根据用户信息生成token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        return generateToken(claims);
    }

    /**
     * 根据负载payload生成JWT的token
     */
    private String generateToken(Map<String, Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成token的过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    /**
     * 从token中获取JWT的负载
     */
    private Claims getClaimsFromToken(String token){
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            LOGGER.info("JWT格式验证失败{}", token);
        }
        return claims;
    }

    /**
     * 从token中获取登录用户名
     */
    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 验证token是否还有效
     *
     * @param token 客户端传来的token
     * @param userDetails 从数据库中查询出来的用户信息
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserNameFromToken(token);
        // 1. token未失效；2. 用户名相同
        // System.out.println(!isTokenExpired(token));
        // System.out.println(userDetails.getUsername());
        return !isTokenExpired(token) && username.equals(userDetails.getUsername());
    }

    /**
     * 验证token是否已经失效
     */
    private boolean isTokenExpired(String token) {
        // 1. 从token获取时间； 2. 时间在现在之后
        Date expiredDate = getExpiredDateFromToken(token);
        return expiredDate.before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    private Date getExpiredDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 当原来的token没有过期时，可以刷新token
     *
     * @param oldToken 带 tokenHead 的token
     */
    public String refreshHeadToken(String oldToken) {
        if(StrUtil.isEmpty(oldToken)){
            return null;
        }
        String token = oldToken.substring(tokenHead.length());
        if(StrUtil.isEmpty(token)){
            return null;
        }
        // token校验不通过，不可刷新
        Claims claims = getClaimsFromToken(token);
        if(claims == null){
            return null;
        }
        // token过期，不可刷新
        if(isTokenExpired(token)){
            return null;
        }
        // 如果token在30分钟之内刷新过，不刷新，返回原token
        if(tokenRefreshJustBefore(token, 30*60)){
            return token;
        }else {
            claims.put(CLAIM_KEY_CREATED, new Date());
            return generateToken(claims);
        }
    }

    /**
     * 判断token是否在指定时间之内刷新过
     *
     * @param token 原token
     * @param time 指定时间，单位为秒
     */
    private boolean tokenRefreshJustBefore(String token, int time) {
        Claims claims = getClaimsFromToken(token);
        Date created = claims.get(CLAIM_KEY_CREATED, Date.class);
        Date refreshDate = new Date();
        // 判断刷新时间（即现在）是否在创建时间之后的指定时间内
        return refreshDate.after(created) && refreshDate.before(DateUtil.offsetSecond(created, time));
    }

    public static void main(String[] args) {
        // TODO：执行失败，JWT格式验证失败，等需得到正确的token及其格式

        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTczMjg2MDA5Nzg3MSwiZXhwIjoxNzMzNDY0ODk3fQ.qnRyVZ48z_L5VFK8Xjg2wNUf29kY7bs_2Nj0Efd2UBicUxNDTx97or8VsOOAGYqoQu5J__attn5swiMPRW98Ag";
        Claims claims = jwtTokenUtil.getClaimsFromToken(token);
        System.out.println(claims);

        String userName = jwtTokenUtil.getUserNameFromToken(token);
        System.out.println(userName);

        List<UmsResource> resourceList = new ArrayList<>();
        UmsResource umsResource = new UmsResource();
        umsResource.setId(1L);
        umsResource.setName("资源1");
        resourceList.add(umsResource);
        UmsAdmin admin = new UmsAdmin();
        admin.setId(16L);
        admin.setUsername("test");
        UserDetails userDetails = new AdminUserDetails(admin, resourceList);

        // validateToken ：token过期之后，无法获取username，函数执行会报错：NullPointException。
        // 实际调用终会先检查 username 的有效性
        boolean result = jwtTokenUtil.validateToken(token, userDetails);
        System.out.println("result: " + result);
    }
}

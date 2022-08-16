package com.lys.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author lys
 * @Date 2022/8/16 17:52
 */
@Slf4j
@Service
public class TokenUtilService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String IDEMPOTENT_TOKEN_PREFIX = "idempotent_token";

    /**
     * 创建token，并存入到redis中
     * @param value 用来辅助验证的value值
     * @return 生成的token令牌
     */
    public String getIdempotentToken(String value){
        //1、ThreadLocalRandom生成token
        String token = ThreadLocalRandom.current().ints(0, 9).limit(10).toString();
        //2、设置存入redis中的key
        String key = IDEMPOTENT_TOKEN_PREFIX + token;
        //3、存储token到redis中，并设置过气时间五分钟
        redisTemplate.opsForValue().set(key, value, 5, TimeUnit.MINUTES);
        return token;
    }

    /**
     * 验证token正确性
     * @param token token字符串
     * @param value 存储在redis中辅助验证信息
     * @return 验证结果
     */
    public boolean validToken(String token, String value) {
        // 设置 Lua 脚本，其中 KEYS[1] 是 key，KEYS[2] 是 value
        String script = "if redis.call('get', KEYS[1]) == KEYS[2] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script, Long.class);
        //根据key的前缀拼接key
        String key = IDEMPOTENT_TOKEN_PREFIX + token;
        //执行Lua脚本
        Long result = redisTemplate.execute(redisScript, Arrays.asList(key, value));
        //根据返回结果判断是否成功匹配并删除Redis中的健值对，若结果不为空和0，则验证通过
        if (result != null && result != 0) {
            log.info("验证 token={},key={},value={} 成功", token, key, value);
            return true;
        }
        log.info("验证 token={},key={},value={} 失败", token, key, value);
        return false;
    }
}

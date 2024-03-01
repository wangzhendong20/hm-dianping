package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_LIST_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryShopType() {

        // 1.从 Redis 中查询商铺缓存
        List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_LIST_KEY, 0, -1);

        // 2.判断 Redis 中是否有该缓存
        if (!shopTypeJsonList.isEmpty() && shopTypeJsonList != null){
            ArrayList<ShopType> typeList = new ArrayList<>();
            for (String str : shopTypeJsonList) {
                typeList.add(JSONUtil.toBean(str,ShopType.class));
            }

            return Result.ok(typeList);
        }

        // Redis 中若不存在该数据，则从数据库中查询
        List<ShopType> typeList = query().orderByAsc("sort").list();

        if (typeList == null || typeList.isEmpty()){
            return Result.fail("分类不存在！");
        }

        for (ShopType shopType : typeList) {
            stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_LIST_KEY,JSONUtil.toJsonStr(shopType));
        }


        return Result.ok(typeList);
    }
}

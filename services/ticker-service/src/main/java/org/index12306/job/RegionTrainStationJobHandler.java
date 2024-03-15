/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.index12306.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.index12306.constant.Index12306Constant;
import org.index12306.constant.RedisKeyConstant;
import org.index12306.entity.RegionDO;
import org.index12306.entity.TrainStationRelationDO;
import org.index12306.framework.starter.cache.DistributedCache;
import org.index12306.framework.starter.common.toolkit.EnvironmentUtil;
import org.index12306.mapper.RegionMapper;
import org.index12306.mapper.TrainStationRelationMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * 地区站点查询定时任务
 */
@RestController
@RequiredArgsConstructor
public class RegionTrainStationJobHandler extends IJobHandler {

    private final RegionMapper regionMapper;
    private final TrainStationRelationMapper trainStationRelationMapper;
    private final DistributedCache distributedCache;

    @XxlJob(value = "regionTrainStationJobHandler")
    @GetMapping("/api/ticket-service/region-train-station/job/cache-init/execute")
    @Override
    public void execute() {
        List<String> regionList = regionMapper.selectList(Wrappers.emptyWrapper())
                .stream()
                .map(RegionDO::getName)
                .toList();
        String requestParam = getJobRequestParam();
        var dateTime = StrUtil.isNotBlank(requestParam) ? requestParam : DateUtil.tomorrow().toDateStr();
        for (int i = 0; i < regionList.size(); i++) {
            for (int j = 0; j < regionList.size(); j++) {
                if (i != j) {
                    String startRegion = regionList.get(i);
                    String endRegion = regionList.get(j);
                    LambdaQueryWrapper<TrainStationRelationDO> relationQueryWrapper = Wrappers.lambdaQuery(TrainStationRelationDO.class)
                            .eq(TrainStationRelationDO::getStartRegion, startRegion)
                            .eq(TrainStationRelationDO::getEndRegion, endRegion);
                    List<TrainStationRelationDO> trainStationRelationDOList = trainStationRelationMapper.selectList(relationQueryWrapper);
                    if (CollUtil.isEmpty(trainStationRelationDOList)) {
                        continue;
                    }
                    Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
                    for (TrainStationRelationDO item : trainStationRelationDOList) {
                        String zSetKey = StrUtil.join("_", item.getTrainId(), item.getDeparture(), item.getArrival());
                        ZSetOperations.TypedTuple<String> tuple = ZSetOperations.TypedTuple.of(zSetKey, Double.valueOf(item.getDepartureTime().getTime()));
                        tuples.add(tuple);
                    }
                    StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
                    String buildCacheKey = RedisKeyConstant.REGION_TRAIN_STATION + StrUtil.join("_", startRegion, endRegion, dateTime);
                    stringRedisTemplate.opsForZSet().add(buildCacheKey, tuples);
                    stringRedisTemplate.expire(buildCacheKey, Index12306Constant.ADVANCE_TICKET_DAY, TimeUnit.DAYS);
                }
            }
        }
    }

    private String getJobRequestParam() {
        return EnvironmentUtil.isDevEnvironment()
                ? ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("requestParam")
                : XxlJobHelper.getJobParam();
    }
}

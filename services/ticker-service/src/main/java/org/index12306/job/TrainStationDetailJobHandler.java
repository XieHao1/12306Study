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
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.index12306.constant.Index12306Constant;
import org.index12306.constant.RedisKeyConstant;
import org.index12306.entity.TrainDO;
import org.index12306.entity.TrainStationRelationDO;
import org.index12306.framework.starter.cache.DistributedCache;
import org.index12306.job.base.AbstractTrainStationJobHandlerTemplate;
import org.index12306.mapper.TrainStationRelationMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_MINUTE_FORMAT;

/**
 * 站点详细信息定时任务
 */
@RestController
@RequiredArgsConstructor
public class TrainStationDetailJobHandler extends AbstractTrainStationJobHandlerTemplate {

    private final TrainStationRelationMapper trainStationRelationMapper;
    private final DistributedCache distributedCache;

    @XxlJob(value = "trainStationDetailJobHandler")
    @GetMapping("/api/ticket-service/train-station-detail/job/cache-init/execute")
    @Override
    public void execute() {
        super.execute();
    }

    @Override
    protected void actualExecute(List<TrainDO> trainDOPageRecords) {
        for (TrainDO each : trainDOPageRecords) {
            LambdaQueryWrapper<TrainStationRelationDO> relationQueryWrapper = Wrappers.lambdaQuery(TrainStationRelationDO.class)
                    .eq(TrainStationRelationDO::getTrainId, each.getId());
            List<TrainStationRelationDO> trainStationRelationDOList = trainStationRelationMapper.selectList(relationQueryWrapper);
            if (CollUtil.isEmpty(trainStationRelationDOList)) {
                return;
            }
            for (TrainStationRelationDO item : trainStationRelationDOList) {
                Map<String, String> actualCacheHashValue = MapUtil.builder("trainNumber", each.getTrainNumber())
                        .put("departureFlag", BooleanUtil.toStringTrueFalse(item.getDepartureFlag()))
                        .put("arrivalFlag", BooleanUtil.toStringTrueFalse(item.getArrivalFlag()))
                        .put("departureTime", DateUtil.format(item.getDepartureTime(), "HH:mm"))
                        .put("arrivalTime", DateUtil.format(item.getArrivalTime(), "HH:mm"))
                        .put("saleTime", DateUtil.format(each.getSaleTime(), NORM_DATETIME_MINUTE_FORMAT))
                        .put("trainTag", each.getTrainTag().toString())
                        .build();
                StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
                String buildCacheKey = RedisKeyConstant.TRAIN_STATION_DETAIL + StrUtil.join("_", each.getId(), item.getDeparture(), item.getArrival());
                stringRedisTemplate.opsForHash().putAll(buildCacheKey, actualCacheHashValue);
                stringRedisTemplate.expire(buildCacheKey, Index12306Constant.ADVANCE_TICKET_DAY, TimeUnit.DAYS);
            }
        }
    }
}

package org.index12306;

import cn.hippo4j.core.enable.EnableDynamicThreadPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 购票服务应用启动器
 *
 */
@SpringBootApplication
@EnableDynamicThreadPool
//@MapperScan("org.opengoofy.index12306.biz.ticketservice.dao.mapper")
//@EnableFeignClients("org.opengoofy.index12306.biz.ticketservice.remote")
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }
}

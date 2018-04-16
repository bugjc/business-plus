package com.bujgc.ha.task;

import com.bugjc.ha.task.RetryStrategyTimerUtil;
import com.bugjc.ha.util.NettyTimerUtil;
import com.bugjc.ha.task.RetryStrategyTask;
import com.xiaoleilu.hutool.util.RandomUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TaskTest {

    /*
     * 线程计数器
     * 	将线程数量初始化
     * 	每执行完成一条线程，调用countDown()使计数器减1
     * 	主线程调用方法await()使其等待，当计数器为0时才被执行
     */
    private CountDownLatch latch = new CountDownLatch(20);


    /**
     * 开发步骤：
     * 1、实现RetryStrategyService接口
     * 2、在businessLogicRun方法编写具体的业务执行代码，failureCallback接口编写策略超阈值处理失败业务执行逻辑
     * 3、下面是具体的调用示例
     */
    @Test
    public void nettyTimerTaskTest(){

        for (int i = 0; i < 1; i++) {
            //参数除了targetService必填外，其它的都非必填；当目标方法需要参数时，则需按下面示例参数结构进行参数和目标方法依赖对象的传递
            Map<String,Object> map = new HashMap<>();
            map.put("targetService",new DefaultRetryStrategyServiceImpl());
            map.put("targetMethodParam",new HashMap(){{
                put("testService",new TestService());
                put("a",RandomUtil.randomInt(1,10000));
                put("b",RandomUtil.randomInt(1,10000));
            }});
            RetryStrategyTimerUtil.addRetryStrategyTimer(map);
        }

        try {
            latch.await(); // 主线程等待
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

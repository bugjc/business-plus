package com.bujgc.ha.task;

import com.bugjc.ha.task.RetryStrategyService;
import com.xiaoleilu.hutool.lang.Console;

import java.util.Map;

public class DefaultRetryStrategyServiceImpl implements RetryStrategyService {

    //自定延迟时间
    private int[] DELAY_TIME = new int[]{
            2,10,30,60,150,400,800,1600,4800,10000
    };

    @Override
    public int expTimeRule(int currentRetryNumber, int retryCount) {
        if (currentRetryNumber <= DELAY_TIME.length){
            return DELAY_TIME[currentRetryNumber];
        }
        return 0;
    }

    @Override
    public boolean businessLogicRun(Object obj) throws Exception {
        try{
            Map<String,Object> paramMap = (Map<String, Object>) obj;
            TestService testService = (TestService) paramMap.get("testService");
            int count = testService.add(
                    Integer.parseInt(paramMap.get("a").toString()),
                    Integer.parseInt(paramMap.get("b").toString())
            );
            Console.log("执行结果："+count);
            Console.log("调用成功！参数:"+paramMap);
            return true;
        }catch (Exception ex){
            Console.error("调用失败，等待再一次通知。");
            return false;
        }
    }

    @Override
    public void failureCallback(Object obj) {
        Console.error(obj);
        Console.error("失败记录日志，并通知维护人员。");
    }
}

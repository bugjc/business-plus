package com.bugjc.ha.task;

import com.bugjc.ha.util.NettyTimerUtil;
import com.xiaoleilu.hutool.lang.Console;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 1、重试策略抽象出服务功能
 * 2、测试目标服务是否具有事务特性
 * 3、集中命中环形队列中的几个格子，测试性能
 * 4、分散任务到不同的格子，测试性能
 * TODO 5、抽取fastJson增强map工具类代替Map对象
 * 重试策略异步任务类
 * @author aoki
 * @create 2018-03-23
 */
@Getter
@Setter
public class RetryStrategyTask implements TimerTask{

	//重试次数（默认最大重试10次,0则是无限次数
	private Integer retryCount = 10;
	//当前重试数（默认1开始）
	private Integer currentRetryNumber = 1;
	//时间单位
	private TimeUnit timeUnit = TimeUnit.SECONDS;
	//调用参数
	private Map<String,Object> param;

	private RetryStrategyTask(){};
	public RetryStrategyTask(Map<String,Object> param){
		this.param = param;
	}

	public RetryStrategyTask(Map<String,Object> param,TimeUnit timeUnit){
		this.param = param;
		this.timeUnit = timeUnit;
	}

	public RetryStrategyTask(Integer currentRetryNumber, Map<String,Object> param){
		this.currentRetryNumber = currentRetryNumber;
		this.param = param;
	}
	
	@Override
	public void run(Timeout timeout) throws Exception {
		try {
			RetryStrategyService serviceTask = (RetryStrategyService) getParam().get("targetService");//方法实现
			Object targetMethodParam = null;//主方法参数
			if (getParam().containsKey("targetMethodParam")){
				targetMethodParam = getParam().get("targetMethodParam");
			}

			if (getRetryCount() == 0 || getCurrentRetryNumber() <= getRetryCount()){ //券使用回调通知重试总次数
				boolean flag = false;
				try {
					flag = serviceTask.businessLogicRun(targetMethodParam);
				}catch (Exception ex){
					flag = false;
				}

				if (flag){
					return;
				}


				//计算下一次调度时间
				int delayTime = serviceTask.expTimeRule(getCurrentRetryNumber(),getRetryCount());
				delayTime = delayTime == 0 ? getCurrentRetryNumber() * 2 : delayTime;
				Console.log("触发重试策略-下一次调度时间是"+ delayTime+" "+getTimeUnit()+"后，总共调度了"+getCurrentRetryNumber()+"次。");
				//添加任务
				NettyTimerUtil.addTask(new RetryStrategyTask(getCurrentRetryNumberIncrement(),getParam()),delayTime, getTimeUnit());

			}else{
				Console.log("重试多次还未得到预定结果");
				Console.log("记录参数信息：");
				Console.log("param:"+targetMethodParam);
				serviceTask.failureCallback(targetMethodParam);
			}
		}catch (Exception ex){
			Console.error(ex);
		}

	}

	private int getCurrentRetryNumberIncrement(){
		setCurrentRetryNumber(getCurrentRetryNumber() + 1);
		return getCurrentRetryNumber();
	}

}
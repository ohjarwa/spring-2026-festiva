package org.example.newyear.service.callback;

import org.example.newyear.entity.enums.AlgorithmEnum;
import org.example.newyear.entity.task.TaskResult;

/**
 * 回调结果转换器
 */
public interface CallbackResultConverter<T> {

    /**
     * 转换回调数据为统一任务结果
     */
    TaskResult convert(T callback);

    /**
     * 获取算法类型
     */
    AlgorithmEnum getAlgorithm(T callback);
}
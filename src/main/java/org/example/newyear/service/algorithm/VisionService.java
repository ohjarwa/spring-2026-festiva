package org.example.newyear.service.algorithm;

import org.example.newyear.dto.algorithm.vision.*;

public interface VisionService {

    /**
     * 异步提交任务
     *
     * @param request 算法请求参数
     * @param headers 请求头配置
     * @return 提交结果（队列位置、预估等待时间）
     */
    AsyncSubmitResponse submitAsync(Object request, VisionRequestHeaders headers);

    /**
     * 查询任务结果
     *
     * @param taskId   任务ID
     * @param ability  原子能力名
     * @param dataType data 内层的类型
     * @return 回调结构（外层 + 算法特定 data）
     */
    <T extends AlgorithmResultBase> VisionCallbackResponse<T> queryResult(
            String taskId, String ability, Class<T> dataType);

    /**
     * 取消任务
     *
     * @param taskId  任务ID
     * @param ability 原子能力名
     * @return 取消结果
     */
    CancelTaskResponse cancelTask(String taskId, String ability);
}



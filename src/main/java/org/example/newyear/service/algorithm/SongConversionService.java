package org.example.newyear.service.algorithm;

import org.example.newyear.dto.algorithm.audio.SongConversionQueryResponse;
import org.example.newyear.dto.algorithm.audio.SongConversionRequest;
import org.example.newyear.dto.algorithm.audio.SongConversionResultWrapper;
import org.example.newyear.dto.algorithm.audio.SongConversionSubmitResponse;

public interface SongConversionService {
    
    /**
     * 异步提交歌曲转换任务
     *
     * @param request 请求参数
     * @return 提交结果
     */
    SongConversionSubmitResponse submitAsync(SongConversionRequest request);
    
    /**
     * 查询转换结果
     *
     * @param businessTaskId 任务ID
     * @return 查询结果
     */
    SongConversionQueryResponse queryResult(String businessTaskId);
    
    /**
     * 查询并解析转换结果
     *
     * @param businessTaskId 任务ID
     * @return 解析后的结果数据
     */
    SongConversionResultWrapper queryAndParseResult(String businessTaskId);
}
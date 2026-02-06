package org.example.newyear.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.audio.SongConversionCallbackResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/audio/callback")
@RequiredArgsConstructor
public class SongConversionCallbackController {
    
    /**
     * 歌曲转换结果回调
     */
    @PostMapping("/song-conversion")
    public ResponseEntity<String> handleCallback(@RequestBody SongConversionCallbackResponse callback) {
        log.info("收到歌曲转换回调, code={}, msg={}", callback.getCode(), callback.getMsg());
        
        if (callback.isSuccess()) {
            handleSuccess(callback);
        } else {
            handleFailure(callback);
        }
        
        return ResponseEntity.ok("success");
    }
    
    private void handleSuccess(SongConversionCallbackResponse callback) {
        if (callback.getData() != null) {
            log.info("歌曲转换成功, taskId={}, resultUrl={}", 
                callback.getData().getTaskId(), 
                callback.getData().getResult());
            
            // TODO: 处理成功逻辑
            // 1. 更新任务状态
            // 2. 通知业务方
            // 3. 触发后续流程
        }
    }
    
    private void handleFailure(SongConversionCallbackResponse callback) {
        log.warn("歌曲转换失败, code={}, msg={}", callback.getCode(), callback.getMsg());
        
        // TODO: 处理失败逻辑
    }
}

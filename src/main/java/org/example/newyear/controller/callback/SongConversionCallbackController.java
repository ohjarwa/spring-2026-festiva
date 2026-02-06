package org.example.newyear.controller.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.newyear.dto.algorithm.audio.SongConversionCallbackResponse;
import org.example.newyear.entity.task.TaskResult;
import org.example.newyear.service.callback.CallbackHandler;
import org.example.newyear.service.callback.SongConversionCallbackConverter;
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

    private final CallbackHandler callbackHandler;
    private final SongConversionCallbackConverter converter;

    @PostMapping("/song-conversion")
    public ResponseEntity<String> handleCallback(
            @RequestBody SongConversionCallbackResponse callback) {

        log.info("收到歌曲转换回调, code={}, msg={}", callback.getCode(), callback.getMsg());

        try {
            TaskResult result = callbackHandler.handleCallback(callback, converter);

            if (!result.isSuccess()) {
                log.warn("歌曲转换失败, taskId={}, errorCode={}, errorMessage={}",
                        result.getTaskId(), result.getErrorCode(), result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("处理回调异常", e);
        }

        return ResponseEntity.ok("success");
    }
}
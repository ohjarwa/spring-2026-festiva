package org.example.newyear.service;

import org.example.newyear.dto.VideoCreateDTO;
import org.example.newyear.entity.Spring2026Template;

/**
 * 模板流程处理器接口
 *
 * @author Claude
 * @since 2026-02-05
 */
public interface ITemplateProcessor {

    /**
     * 处理模板流程
     *
     * @param recordId  记录ID
     * @param template  模板信息
     * @param dto       创建视频请求
     * @return 最终视频URL
     */
    String process(String recordId, Spring2026Template template, VideoCreateDTO dto);
}

package org.example.newyear.dto.algorithm.audio;

import lombok.Data;

@Data
public class SongConversionUrlData {
    
    /**
     * 人声转换结果下载地址
     */
    private String vocalUrl;
    
    /**
     * 背景音转换结果下载地址
     */
    private String bgmUrl;
    
    /**
     * 完整音频下载地址
     */
    private String allUrl;
}
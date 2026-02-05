package org.example.newyear.dto.algorithm.audio;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SongConversionStatus {
    
    PROCESSING(1, "处理中"),
    SUCCESS(2, "成功"),
    FAILED(3, "失败");
    
    private final int code;
    private final String description;
    
    public static SongConversionStatus of(Integer code) {
        if (code == null) return null;
        for (SongConversionStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}
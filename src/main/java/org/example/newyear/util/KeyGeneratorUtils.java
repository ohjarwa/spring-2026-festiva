package org.example.newyear.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class KeyGeneratorUtils {
    public static String redisKeyGen(String taskId, String aiName) {
        return taskId + ":" + aiName;
    }

    public static String taskIdGen() {
        return "activity2026"+":"+ UUID.randomUUID();
    }

    public static String materialIdGen() {
        return "mat" + ":" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String recordIdGen() {
        return "rec" + ":" + UUID.randomUUID().toString().replace("-", "");
    }
}

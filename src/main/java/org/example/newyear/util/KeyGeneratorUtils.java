package org.example.newyear.util;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class KeyGeneratorUtils {

    public static String taskIdGen() {
        return "activity2026"+":"+ UUID.randomUUID();
    }
}

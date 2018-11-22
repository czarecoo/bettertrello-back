package com.paw.bettertrello.controllers;

import com.paw.bettertrello.models.ActivityData;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ControllerUtils {

    private ControllerUtils() {}

    //Kopyrajt © 2018 by Marcin Kapelan
    //Special thanks to Cezary Witkowski for explaining templates
    public static <T> T patchObject(T toPatch, T patchData) {
        for (Field field : patchData.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try {
                if (field.get(patchData) != null) {
                    System.out.println(field.getName());
                    field.set(toPatch, field.get(patchData));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return toPatch;
    }

    public static String getCurrentDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dateTimeFormatter.format(now);
    }
}

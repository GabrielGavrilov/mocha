package com.gabrielgavrilov.mocha;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Field;

public class MochaReflectionTools {

    public static Class<?> getClassFromObject(Object object) {
        return object.getClass();
    }

    public static Object hydrateClassFromJsonObject(Class<?> clazz, JsonObject jsonObject) {
        try {
            Object classInstance = clazz.getDeclaredConstructor().newInstance();
            Gson gson = new Gson();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (!jsonObject.has(field.getName()))
                    continue;

                JsonElement jsonElement = jsonObject.get(field.getName());
                Object value = gson.fromJson(jsonElement, field.getGenericType());
                field.set(classInstance, value);
            }

            return classInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

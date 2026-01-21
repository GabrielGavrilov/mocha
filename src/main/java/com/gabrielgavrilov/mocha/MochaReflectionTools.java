package com.gabrielgavrilov.mocha;

import java.lang.reflect.Field;

public class MochaReflectionTools {

    public static Object hydrateClass(Class<?> clazz, Object... data) {
        try {
            Object classInstance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                field.set(classInstance, data[0]);
            }

            return classInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

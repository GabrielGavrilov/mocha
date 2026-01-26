package com.gabrielgavrilov.mocha;

import com.gabrielgavrilov.mocha.annotations.Body;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

public class MochaReflectionTools {

    public static Class<?>[] getParameterTypesFromMethod(Method method) {
        return method.getParameterTypes();
    }

    public static ArrayList<Parameter> getAllParametersWithAnnotation(Class<?> annotation, Method method) {
        ArrayList<Parameter> parameters = new ArrayList<>();
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent((Class<? extends Annotation>) annotation))
                parameters.add(parameter);
        }
        return parameters;
    }

    public static Class<?> getBodyParameterTypeFromMethod(Method method) {
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(Body.class))
                return parameter.getType();
        }
        return null;
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

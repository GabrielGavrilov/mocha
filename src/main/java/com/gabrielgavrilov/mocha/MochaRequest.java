package com.gabrielgavrilov.mocha;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Objects;

public class MochaRequest<T>
{
    public Object payload;
    public HashMap<String, Object> parameter = new HashMap<>();
    public HashMap<String, String> cookie = new HashMap<>();
    public String header = "";

//    public String get(String value)
//    {
//        if(payload instanceof HashMap<?,?>)
//            return ((HashMap<String, String>)payload).get(value);
//
//        else if(payload instanceof JsonObject)
//            return ((JsonObject)payload).get(value).getAsString();
//
//        return null;
//    }
}

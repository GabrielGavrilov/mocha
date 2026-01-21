package com.gabrielgavrilov.mocha;

import com.google.gson.JsonObject;

import java.util.HashMap;

public class MochaPayload<T>
{
    private T PAYLOAD;

    public MochaPayload(T payload) {
        System.out.println(payload.toString());
        this.PAYLOAD = payload;
    }

    public String get(String value)
    {
        if(this.PAYLOAD instanceof HashMap<?,?>)
        {
            return ((HashMap<String, String>)this.PAYLOAD).get(value);
        }
        else if(this.PAYLOAD instanceof JsonObject)
        {
            return ((JsonObject)this.PAYLOAD).get(value).getAsString();
        }
        return null;
    }
}

package com.gabrielgavrilov.mocha;

import java.util.HashMap;

public class MochaRequest {
    public Object payload;
    public HashMap<String, String> parameter = new HashMap<>();
    public HashMap<String, String> cookie = new HashMap<>();
    public String header = "";
}

package com.gabrielgavrilov.mocha;

import java.lang.reflect.Method;

public class ControllerRoute {
    public Object controllerInstance;
    public Method controllerMethod;

    ControllerRoute(Object controllerInstance, Method controllerMethod) {
        this.controllerInstance = controllerInstance;
        this.controllerMethod = controllerMethod;
    }

}

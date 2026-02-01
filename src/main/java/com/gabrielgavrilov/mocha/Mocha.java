package com.gabrielgavrilov.mocha;

import com.gabrielgavrilov.mocha.annotations.Controller;
import com.gabrielgavrilov.mocha.annotations.Dependency;
import com.gabrielgavrilov.mocha.annotations.Get;
import com.gabrielgavrilov.mocha.annotations.Route;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Mocha - A tiny flexible web server framework for Java
 * @author Gabriel Gavriloiv <gabriel.gavrilov02@gmail.com>
 */
public class Mocha
{
    /**
     * Mocha Controllers
     */

    protected static HashMap<String, ControllerRoute> _GET_ROUTES = new HashMap<>();

    private static final ArrayList<Class<?>> controllers = new ArrayList<>();
    private static final HashMap<Class<?>, Object> dependencies = new HashMap<>();

    public static void attach(Class<?> controller) {
        controllers.add(controller);
    }

    /**
     * Starts the Mocha web server at the given port and listens for new sockets.
     *
     * @param port Port for the server.
     */
    public static void listen(int port) {
        try {
            instantiateDependencies();
            buildControllers();
            MochaListenerThread serverThread = new MochaListenerThread(port);
            serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void instantiateDependencies() {
        for (Class<?> controller : controllers) {
            instantiateDependency(controller);
        }
    }

    private static void instantiateDependency(Class<?> controller) {
        try {
            for (Field field : controller.getDeclaredFields()) {
                if (field.isAnnotationPresent(Dependency.class)) {
                    dependencies.put(field.getType(), field.getType().getDeclaredConstructor().newInstance());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildControllers() {
        for (Class<?> controller : controllers) {
            if (controller.isAnnotationPresent(Controller.class) && controller.isAnnotationPresent(Route.class)) {
//                buildControllerDependencies(controller);
                buildController(controller);
            }
        }
    }

//    private static void buildControllerDependencies(Class<?> controller) {
//        try {
//            for (Field field : controller.getDeclaredFields()) {
//                if (field.isAnnotationPresent(Dependency.class)) {
//                    field.setAccessible(true);
//                    field.set(field.getType(), dependencies.get(field.getType()));
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    private static void buildController(Class<?> controller) {
        try {
            Route route = controller.getAnnotation(Route.class);
            Object controllerInstance = controller.getDeclaredConstructor().newInstance();

            for (Field field : controller.getDeclaredFields()) {
                if (field.isAnnotationPresent(Dependency.class)) {
                    field.setAccessible(true);
                    Object dependency = dependencies.get(field.getType());
                    field.set(controllerInstance, dependency);
                }
            }

            for (Method method : controller.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get get = method.getAnnotation(Get.class);
                    _GET_ROUTES.put(route.value() + get.value(), new ControllerRoute(controllerInstance, method));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * Starts the Mocha web server at the given port and host address and listens for new sockets.
     *
     * @param port Port for the server.
     * @param host Host IP for the server.
     * @param callback Runnable callback that gets executed when the server starts
     *                 listening for new sockets.
     */
    public static void listen(int port, String host, Runnable callback)
    {
        try
        {
            callback.run();
            MochaListenerThread serverThread = new MochaListenerThread(port, host);
            serverThread.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

}

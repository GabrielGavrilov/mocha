package com.gabrielgavrilov.mocha;

import com.gabrielgavrilov.mocha.annotations.Controller;
import com.gabrielgavrilov.mocha.annotations.Get;
import com.gabrielgavrilov.mocha.annotations.Route;

import java.io.*;
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
     * CRUD routes
     */
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> GET_ROUTES = new HashMap<>();
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> POST_ROUTES = new HashMap<>();
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> PUT_ROUTES = new HashMap<>();
    protected static HashMap<String, BiConsumer<MochaRequest, MochaResponse>> DELETE_ROUTES = new HashMap<>();

    /**
     * Mocha Controllers
     */
    protected static ArrayList<Class<?>> controllers = new ArrayList<>();

    protected static HashMap<String, ControllerRoute> get_routes = new HashMap<>();


    public static void attach(Class<?> controller) {
        controllers.add(controller);
    }


    protected static String VIEWS_DIRECTORY = "";
    protected static String STATIC_DIRECTORY = "";

    /**
     * Used to set server settings.
     *
     * @param setting Setting name.
     * @param value Setting value.
     */
    public static void set(String setting, String value)
    {
        switch(setting)
        {
            case "views":
                VIEWS_DIRECTORY = value;
                break;
            case "static":
                STATIC_DIRECTORY = value;
                break;
        }
    }

    /**
     * Creates a new GET route. Stores the route and its callback into a HashMap. Gets called by
     * the MochaClient class when needed.
     *
     * @param route Route.
     * @param callback Callback function (BiConsumer that accepts MochaRequest and MochaResponse).
     */
    public static void get(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        GET_ROUTES.put(route, callback);
    }

    /**
     * Creates a new POST route. Stores the route and its callback into a HashMap. Gets called by
     * the MochaClient class when needed.
     *
     * @param route Route.
     * @param callback Callback function (BiConsumer that accepts MochaRequest and MochaResponse).
     */
    public static void post(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        POST_ROUTES.put(route, callback);
    }

    /**
     * Creates a new PUT route. Stores the route and its callback into a hashmap. Gets called by
     * the MochaClient class when needed.
     *
     * @param route Route
     * @param callback Callback function (BiConsumer that accepts MochaRequest and MochaResponse).
     */
    public static void put(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        PUT_ROUTES.put(route, callback);
    }

    /**
     * Creates as new DELETE route. Stores the route and its callback into a hashmap. Gets called
     * by the MochaClient class when needed.
     *
     * @param route
     * @param callback
     */
    public static void delete(String route, BiConsumer<MochaRequest, MochaResponse> callback)
    {
        DELETE_ROUTES.put(route, callback);
    }

    /**
     * Starts the Mocha web server at the given port and listens for new sockets.
     *
     * @param port Port for the server.
     */
    public static void listen(int port) {
        try {
            buildControllers();
            MochaListenerThread serverThread = new MochaListenerThread(port);
            serverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void buildControllers() {
        for (Class<?> controller : controllers) {
            if (controller.isAnnotationPresent(Controller.class) && controller.isAnnotationPresent(Route.class)) {
                buildController(controller);
            }
        }
    }

    private static void buildController(Class<?> controller) {
        try {
            Route route = controller.getAnnotation(Route.class);
            Object controllerInstance = controller.getDeclaredConstructor().newInstance();
            for (Method method : controller.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get get = method.getAnnotation(Get.class);
                    get_routes.put(route.value() + get.value(), new ControllerRoute(controllerInstance, method));
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

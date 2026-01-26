package com.gabrielgavrilov.mocha;

import com.gabrielgavrilov.mocha.annotations.Body;
import com.gabrielgavrilov.mocha.annotations.Param;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MochaClient {

    /**
     * Initializes the MochaClient class. Reads the socket's requested header
     * and determines the response based on the method and route.
     *
     * @param clientInput Socket InputStream.
     * @param clientOutput Socket OutputStream.
     */
    MochaClient(InputStream clientInput, OutputStream clientOutput) {
        try {
            InputStreamReader streamReader = new InputStreamReader(clientInput);
            BufferedReader buffReader = new BufferedReader(streamReader);
            StringBuilder clientHeader = new StringBuilder();

            String line;
            while((line = buffReader.readLine()) != null && !line.isEmpty()) {
                clientHeader.append(line).append("\r\n");
            }

            String route = getRequestedRoute(clientHeader.toString());
            String method = getRequestedMethod(clientHeader.toString());

            handleRequest(clientHeader.toString(), route, method, clientOutput, buffReader);

        /**
         * Clean this up so users can throw custom errors
          */
        } catch (RouteNotFoundException e) {
            this.handleRouteNotFoundRequest(clientOutput);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the given HTTP request.
     *
     * @param header Socket header.
     * @param route Requested route.
     * @param method Requested method.
     * @param clientOutput Client output stream.
     * @param buffReader Buffered Reader
     * @throws IOException
     */
    private void handleRequest(String header, String route, String method, OutputStream clientOutput, BufferedReader buffReader) throws IOException, RouteNotFoundException, InvocationTargetException, IllegalAccessException {
        switch(method)
        {
            case "GET":
                handleGetRequest(header, route, clientOutput, buffReader);
                break;
//            case "POST":
//                handlePostRequest(header, route, clientOutput, buffReader);
//                break;
//            case "PUT":
//                handlePutRequest(header, route, clientOutput, buffReader);
//                break;
//            case "DELETE":
//                handleDeleteRequest(header, route, clientOutput, buffReader);
//                break;
        }
    }

    /**
     * Handles the GET request.
     *
     * @param header Client HTTP header.
     * @param route Requested route.
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void handleGetRequest(String header, String route, OutputStream clientOutput, BufferedReader buffReader) throws InvocationTargetException, IllegalAccessException, IOException, RouteNotFoundException {
        ControllerRoute fromParsedRoute = getControllerRouteFromParsedRoute(route, Mocha._GET_ROUTES);
        StringBuilder payload = new StringBuilder();

        while(buffReader.ready()) {
            payload.append((char)buffReader.read());
        }

        if (fromParsedRoute != null) {
            handleParsedGetResponse(header, route, fromParsedRoute, clientOutput, payload.toString());
            return;
        }

        if (Mocha._GET_ROUTES.get(route) != null) {
            ControllerRoute controllerRoute = Mocha._GET_ROUTES.get(route);
            Object value = controllerRoute.controllerMethod.invoke(controllerRoute.controllerInstance);
            handleGetResponse(header, route, clientOutput, value);
            return;
        }

        throw new RouteNotFoundException();
    }

    private void handleGetResponse(String header, String route, OutputStream clientOutput, Object methodOutput) throws IOException
    {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();

        request.header = header;

        response.initializeHeader("200 OK", "application/json");
        response.send(new Gson().toJson(methodOutput));

        writeFullResponse(response, clientOutput);
    }

    private void handleParsedGetResponse(String header, String route, ControllerRoute controllerRoute, OutputStream clientOutput, String payload) throws IOException, InvocationTargetException, IllegalAccessException {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, Mocha._GET_ROUTES), route);

        parsePayload(header, payload, request, controllerRoute.controllerMethod);

        request.parameter = parser.parse();
        request.cookie = parseCookiesToHashMap(header);
        request.header = header;

        response.initializeHeader("200 OK", "application/json");

        Object[] args = toVarargs(
                test(request, controllerRoute.controllerMethod),
                test2(request, controllerRoute.controllerMethod)
        ).toArray();

        for (Object arg : args) {
            System.out.println(arg);
        }

        response.send(new Gson().toJson(controllerRoute.controllerMethod.invoke(
                controllerRoute.controllerInstance,
                args
        )));

        writeFullResponse(response, clientOutput);
    }

    // TODO: rename
    private ArrayList<String> test(MochaRequest request, Method controllerMethod) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<Parameter> param = MochaReflectionTools.getAllParametersWithAnnotation(Param.class, controllerMethod);
        for (Parameter parameter : param) {
            System.out.println("Got " + parameter.getName());
            result.add((String) request.parameter.get(parameter.getName()));
        }

        return result;
    }

    // TODO: rename
    private Object test2(MochaRequest request, Method controllerMethod) {
        if (!MochaReflectionTools.getAllParametersWithAnnotation(Body.class, controllerMethod).isEmpty()) {
            return request.payload;
        }
        return null;
    }

    private ArrayList<Object> toVarargs(ArrayList<String> params, Object body) {
        ArrayList<Object> result = new ArrayList<>();

        result.addAll(params);
        result.add(body);

        return result;
    }

    private ControllerRoute getControllerRouteFromParsedRoute(String route, HashMap<String, ControllerRoute> routes) {
        for (Map.Entry<String, ControllerRoute> entry : routes.entrySet()) {
            MochaParser parser = new MochaParser(entry.getKey(), route);
            if(parser.isParsable())
                return entry.getValue();
        }

        return null;
    }

    /**
     * Returns the template route from the parsed route.
     *
     * @param route Requested route.
     * @param hashMap Method hashmap.
     * @return String
     */
    private String getTemplateFromParsedRoute(String route, HashMap<String, ControllerRoute> hashMap)
    {
        for(Map.Entry<String, ControllerRoute> entry : hashMap.entrySet())
        {
            MochaParser parser = new MochaParser(entry.getKey(), route);
            if(parser.isParsable())
                return entry.getKey();
        }

        return null;
    }

    private void parsePayload(String header, String payload, MochaRequest request, Method controllerMethod)
    {
        String contentType = getRequestContentType(header);

        switch(contentType)
        {
//            case "text/plain":
//                request.payload = new MochaPayload(parsePayloadToHashMap(payload));
//                break;
            case "application/json":
//                request.payload = new MochaPayload(parsePayloadToJsonObject(payload));
                request.payload = parsePayloadToBodyObject(payload, controllerMethod);
                break;
        }

    }

    private Object parsePayloadToBodyObject(String payload, Method controllerMethod) {
        JsonObject object = JsonParser.parseString(payload).getAsJsonObject();
        // TODO: this can be cleaner
        return MochaReflectionTools.hydrateClassFromJsonObject(MochaReflectionTools.getBodyParameterTypeFromMethod(controllerMethod), object);
    }

    /**
     * Parses the raw payload into a hashmap.
     *
     * @param payload Raw payload.
     * @return String and String Hashmap.
     */
    private HashMap<String, String> parsePayloadToHashMap(String payload)
    {
        HashMap<String, String> payloadData = new HashMap<>();
        String[] payloads = payload.split("&");

        for(int i = 0; i < payloads.length; i++)
        {
            String[] currentPayload = payloads[i].split("=");
            payloadData.put(currentPayload[0], currentPayload[1]);
        }

        return payloadData;
    }

    private String parsePayloadToJsonObject(String payload)
    {
        return JsonParser.parseString(payload).getAsJsonObject().toString();
    }

    /**
     * Parses the cookies into a hash map.
     *
     * @param header Client HTTP header.
     * @return String and String Hashmap.
     */
    private HashMap<String, String> parseCookiesToHashMap(String header)
    {
        HashMap<String, String> cookieData = new HashMap<>();

        if(header.contains("Cookie"))
        {
            String[] headerSplit = header.split("\n");
            for(int i = 0; i < headerSplit.length; i++)
            {
                if(headerSplit[i].contains("Cookie"))
                {
                    String cookieHeader = headerSplit[i].substring(8);
                    String[] cookies = cookieHeader.split("; ");

                    for(int j = 0; j < cookies.length; j++)
                    {
                        String[] cookie = cookies[j].split("=");
                        cookieData.put(cookie[0], cookie[1]);
                    }

                    return cookieData;
                }
            }
        }

        return null;
    }

    /**
     * Handles the 404 page.
     *
     * @param clientOutput Client output stream.
     * @throws IOException
     */
    private void handleRouteNotFoundRequest(OutputStream clientOutput) {
//        for(Map.Entry<String, BiConsumer<MochaRequest, MochaResponse>> entry : Mocha.GET_ROUTES.entrySet())
//        {
//            if(entry.getKey().equals("*"))
//            {
//                MochaRequest request = new MochaRequest();
//                MochaResponse response = new MochaResponse();
//                BiConsumer<MochaRequest, MochaResponse> callback = entry.getValue();
//
//                consume(callback, request, response);
//                writeFullResponse(response, clientOutput);
//                return;
//            }
//        }

        MochaResponse response = new MochaResponse();
        response.initializeHeader("404 Not Found", "application/json");
        writeFullResponse(response, clientOutput);
    }

    /**
     * Executes the BiConsumer.
     *
     * @param consumer MochaRequest and MochaResponse BiConsumer.
     * @param request Mocha request.
     * @param response Mocha response.
     */
//    private static void consume(BiConsumer<MochaRequest, MochaResponse> consumer, MochaRequest request, MochaResponse response)
//    {
//        consumer.accept(request, response);
//    }


    /**
     * Returns the requested route.
     *
     * @param clientHeader Client HTTP header.
     * @return String
     */
    private static String getRequestedRoute(String clientHeader)
    {
        String[] routeSplit = clientHeader.split("\r\n")[0].split(" ");

        if(routeSplit.length > 1)
            return routeSplit[1];

        return "/";
    }

    /**
     * Returns the requested method.
     *
     * @param clientHeader Client HTTP header.
     * @return String
     */
    private static String getRequestedMethod(String clientHeader)
    {
        return clientHeader.split("\r\n")[0].split(" ")[0];
    }

    private static String getRequestContentType(String clientHeader)
    {
        return clientHeader.split("\r\n")[1].split(": ")[1];
    }

    private static void writeFullResponse(MochaResponse response, OutputStream clientOutput) {
        try {
            clientOutput.write(response.header.toString().getBytes());
            clientOutput.write(response.body.toString().getBytes());
            clientOutput.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
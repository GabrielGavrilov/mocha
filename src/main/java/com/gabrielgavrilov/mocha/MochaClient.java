package com.gabrielgavrilov.mocha;

import com.gabrielgavrilov.mocha.annotations.Body;
import com.gabrielgavrilov.mocha.annotations.Param;
import com.gabrielgavrilov.mocha.exceptions.BadRequest;
import com.gabrielgavrilov.mocha.exceptions.HttpException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
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

        } catch(HttpException e) {
            // TODO: rename
            this.test(clientOutput, e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof HttpException ex) {
                this.test(clientOutput, ex);
            }
            // TODO: print stack trace and throw 500 internal error if not an instance of HttpException
        } catch (Exception e) {
            // TODO: same here
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
    private void handleRequest(String header, String route, String method, OutputStream clientOutput, BufferedReader buffReader) throws IOException, InvocationTargetException, IllegalAccessException {
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
    private void handleGetRequest(String header, String route, OutputStream clientOutput, BufferedReader buffReader) throws InvocationTargetException, IllegalAccessException, IOException {
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
            handleGetResponse(header, route, clientOutput, controllerRoute, payload.toString());
            return;
        }
    }

    private void handleGetResponse(String header, String route, OutputStream clientOutput, ControllerRoute controllerRoute, String payload) throws IOException, InvocationTargetException, IllegalAccessException {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();

//        parsePayload(header, payload, request, controllerRoute.controllerMethod);
        request.header = header;

        response.initializeHeader("200 OK", "application/json");

        Object[] varargs = convertParametersAndPayloadToList(
                convertRequestParametersToList(request, controllerRoute.controllerMethod),
                maybeGetRequestPayload(request, controllerRoute.controllerMethod)
        ).toArray();

        response.send(new Gson().toJson(controllerRoute.controllerMethod.invoke(
                controllerRoute.controllerInstance,
                varargs
        )));

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

        Object[] varargs = convertParametersAndPayloadToList(
                convertRequestParametersToList(request, controllerRoute.controllerMethod),
                maybeGetRequestPayload(request, controllerRoute.controllerMethod)
        ).toArray();

        response.send(new Gson().toJson(controllerRoute.controllerMethod.invoke(
                controllerRoute.controllerInstance,
                varargs
        )));

        writeFullResponse(response, clientOutput);
    }

    private List<String> convertRequestParametersToList(MochaRequest request, Method controllerMethod) {
        List<String> result = new ArrayList<>();
        ArrayList<Parameter> param = MochaReflectionTools.getAllParametersWithAnnotation(Param.class, controllerMethod);
        for (Parameter parameter : param)
            result.add((String) request.parameter.get(parameter.getName()));

        return result;
    }

    private Optional<Object> maybeGetRequestPayload(MochaRequest request, Method controllerMethod) {
        if (!MochaReflectionTools.getAllParametersWithAnnotation(Body.class, controllerMethod).isEmpty()) {
            return Optional.of(request.payload);
        }
        return Optional.empty();
    }

    private List<Object> convertParametersAndPayloadToList(List<String> params, Optional<Object> payload) {
        List<Object> result = new ArrayList<>(params);
        payload.ifPresent(result::add);
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
    private String getTemplateFromParsedRoute(String route, HashMap<String, ControllerRoute> hashMap) {
        for(Map.Entry<String, ControllerRoute> entry : hashMap.entrySet())
        {
            MochaParser parser = new MochaParser(entry.getKey(), route);
            if(parser.isParsable())
                return entry.getKey();
        }

        return null;
    }

    private void parsePayload(String header, String payload, MochaRequest request, Method controllerMethod) {
        String contentType = getRequestContentType(header);

        switch(contentType)
        {
            default:
                request.payload = parseJsonPayloadToBodyObject(payload, controllerMethod);
                break;
        }

    }

    private Object parseJsonPayloadToBodyObject(String payload, Method controllerMethod) {
        JsonObject object = JsonParser.parseString(payload).getAsJsonObject();
        return MochaReflectionTools.hydrateClassFromJsonObject(MochaReflectionTools.getBodyParameterTypeFromMethod(controllerMethod), object);
    }

    /**
     * Parses the cookies into a hash map.
     *
     * @param header Client HTTP header.
     * @return String and String Hashmap.
     */
    private HashMap<String, String> parseCookiesToHashMap(String header) {
        HashMap<String, String> cookieData = new HashMap<>();

        if(header.contains("Cookie")) {
            String[] headerSplit = header.split("\n");
            for(int i = 0; i < headerSplit.length; i++) {
                if(headerSplit[i].contains("Cookie")) {
                    String cookieHeader = headerSplit[i].substring(8);
                    String[] cookies = cookieHeader.split("; ");

                    for(int j = 0; j < cookies.length; j++) {
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
        MochaResponse response = new MochaResponse();
        response.initializeHeader("404 Not Found", "application/json");
        writeFullResponse(response, clientOutput);
    }

    private void test(OutputStream clientOutput, HttpException e) {
        MochaResponse response = new MochaResponse();
        response.initializeHeader(String.format("%d %s", e.getStatusCode(), e.getStatusText()), "application/json");
        writeFullResponse(response, clientOutput);
    }

    /**
     * Returns the requested route.
     *
     * @param clientHeader Client HTTP header.
     * @return String
     */
    private static String getRequestedRoute(String clientHeader) {
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
    private static String getRequestedMethod(String clientHeader) {
        return clientHeader.split("\r\n")[0].split(" ")[0];
    }

    private static String getRequestContentType(String clientHeader) {
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
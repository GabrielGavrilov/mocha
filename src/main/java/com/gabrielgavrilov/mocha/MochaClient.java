package com.gabrielgavrilov.mocha;

import com.gabrielgavrilov.mocha.annotations.Body;
import com.gabrielgavrilov.mocha.annotations.Param;
import com.gabrielgavrilov.mocha.exceptions.HttpException;
import com.gabrielgavrilov.mocha.exceptions.InternalServerError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.text.html.Option;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class MochaClient {

    private OutputStream clientOutputStream;
    private BufferedReader clientBufferedReader;
    private StringBuilder clientHeader = new StringBuilder();

    private String route;
    private String method;

    /**
     * Initializes the MochaClient class. Reads the socket's requested header
     * and determines the response based on the method and route.
     *
     * @param clientInput Socket InputStream.
     * @param clientOutput Socket OutputStream.
     */
    MochaClient(InputStream clientInput, OutputStream clientOutput) {
        InputStreamReader streamReader = new InputStreamReader(clientInput);
        clientOutputStream = clientOutput;
        clientBufferedReader = new BufferedReader(streamReader);

        readRequestHeader();
        handleRequest();

//        try {
//
//
//        } catch(HttpException e) {
//            this.handleHttpException(clientOutput, e);
//        } catch (InvocationTargetException e) {
//            if (e.getCause() instanceof HttpException ex) {
//                this.handleHttpException(clientOutput, ex);
//            }
//            this.handleHttpException(clientOutput, new InternalServerError(e.getMessage()));
//        } catch (Exception e) {
//            this.handleHttpException(clientOutput, new InternalServerError(e.getMessage()));
//        }
    }

    private void readRequestHeader() {
        try {
            String line;
            while((line = this.clientBufferedReader.readLine()) != null && !line.isEmpty()) {
                this.clientHeader.append(line).append("\r\n");
            }

            this.route = getRequestedRoute(clientHeader.toString());
            this.method = getRequestedMethod(clientHeader.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the given HTTP request.
     * @throws IOException
     */
    private void handleRequest() {
        switch(method)
        {
            case "GET":
                handleRequest(Mocha._GET_ROUTES);
                break;
            case "POST":
                handleRequest(Mocha._POST_ROUTES);
                break;
            case "PUT":
                handleRequest(Mocha._PUT_ROUTES);
                break;
            case "DELETE":
                handleRequest(Mocha._DELETE_ROUTES);
                break;
        }
    }

    private Optional<String> readRequestPayload() {
       try {
           StringBuilder payload = new StringBuilder();

           while(this.clientBufferedReader.ready()) {
               payload.append((char)this.clientBufferedReader.read());
           }

           return !payload.isEmpty()
                   ? Optional.of(payload.toString())
                   : Optional.empty();

       } catch (IOException e) {
           return Optional.empty();
       }
    }

    private void handleRequest(HashMap<String, ControllerRoute> methodRoutes) {
        ControllerRoute fromParsedRoute = getControllerRouteFromParsedRoute(route, methodRoutes);
        Optional<String> payload = readRequestPayload();

        if (fromParsedRoute != null) {
            handleParsedResponse(fromParsedRoute, payload, methodRoutes);
        }

        if (methodRoutes.get(route) != null) {
            ControllerRoute controllerRoute = methodRoutes.get(route);
            handleResponse(controllerRoute, payload);
        }
    }

    private void handleResponse(ControllerRoute controllerRoute, Optional<String> payload) {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();

        if (payload.isPresent())
            parsePayload(clientHeader.toString(), payload.get(), request, controllerRoute.controllerMethod);

        request.header = clientHeader.toString();
        response.initializeHeader("200 OK", "application/json");

        Object[] varargs = convertParametersAndPayloadToList(
                convertRequestParametersToList(request, controllerRoute.controllerMethod),
                maybeGetRequestPayload(request, controllerRoute.controllerMethod)
        ).toArray();

        response.send(new Gson().toJson(MochaReflectionTools.invokeMethod(
                controllerRoute.controllerMethod,
                controllerRoute.controllerInstance,
                varargs
        )));

        writeFullResponse(response);
    }

    private void handleParsedResponse(ControllerRoute controllerRoute, Optional<String> payload, HashMap<String, ControllerRoute> methodRoutes) {
        MochaRequest request = new MochaRequest();
        MochaResponse response = new MochaResponse();
        MochaParser parser = new MochaParser(getTemplateFromParsedRoute(route, methodRoutes), route);

        if (payload.isPresent())
            parsePayload(clientHeader.toString(), payload.get(), request, controllerRoute.controllerMethod);

        request.parameter = parser.parse();
        request.header = clientHeader.toString();
        response.initializeHeader("200 OK", "application/json");

        Object[] varargs = convertParametersAndPayloadToList(
                convertRequestParametersToList(request, controllerRoute.controllerMethod),
                maybeGetRequestPayload(request, controllerRoute.controllerMethod)
        ).toArray();

        response.send(new Gson().toJson(MochaReflectionTools.invokeMethod(
                controllerRoute.controllerMethod,
                controllerRoute.controllerInstance,
                varargs
        )));

        writeFullResponse(response);
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
        for(Map.Entry<String, ControllerRoute> entry : hashMap.entrySet()) {
            MochaParser parser = new MochaParser(entry.getKey(), route);
            if(parser.isParsable())
                return entry.getKey();
        }

        return null;
    }

    private void parsePayload(String header, String payload, MochaRequest request, Method controllerMethod) {
        String contentType = getRequestContentType(header);
        switch(contentType) {
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


//    private void handleHttpException(OutputStream clientOutput, HttpException e) {
//        MochaResponse response = new MochaResponse();
//        response.initializeHeader(String.format("%d %s", e.getStatusCode(), e.getStatusText()), "application/json");
//        writeFullResponse(response, clientOutput);
//    }

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

    private void writeFullResponse(MochaResponse response) {
        try {
            clientOutputStream.write(response.header.toString().getBytes());
            clientOutputStream.write(response.body.toString().getBytes());
            clientOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
package service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

public class TodoClient {

    private static final String DEFAULT_BASE_URI = "http://localhost";

    private final Client client;
    private String baseUri = DEFAULT_BASE_URI;

    public TodoClient() {
        this.client = ClientBuilder.newBuilder()
                .register("application/json, */*")
                .build();
    }

    public TodoClient(Client client) {
        this.client = client;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }


}

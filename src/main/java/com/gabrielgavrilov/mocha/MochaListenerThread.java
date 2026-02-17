package com.gabrielgavrilov.mocha;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MochaListenerThread extends Thread {

    private String host;
    private int port;
    private ServerSocket server;

    /**
     * Constructor for the MochaListenerThread. Used to start the Mocha web server on
     * a new thread.
     *
     * @param port Port for the server.
     * @throws IOException
     */
    MochaListenerThread(int port) throws IOException {
        this.port = port;
        this.server = new ServerSocket(port);
    }

    MochaListenerThread(int port, String host) throws IOException {
        this.port = port;
        this.host = host;
        InetAddress hostAddress = InetAddress.getByName(host);
        this.server = new ServerSocket(port, 5, hostAddress);
    }

    /**
     * Run method override for the MochaListenerThread.
     */
    @Override
    public void run() {
        while(this.server.isBound() && !this.server.isClosed()) {
            try {
                Socket client = this.server.accept();
                MochaWorkerThread workerThread = new MochaWorkerThread(client);
                workerThread.start();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

}

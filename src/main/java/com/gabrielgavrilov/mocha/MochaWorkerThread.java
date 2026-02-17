package com.gabrielgavrilov.mocha;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MochaWorkerThread extends Thread {

    private Socket client;

    /**
     * Constructor for the MochaWorkerThread. Used to start the MochaClient on
     * a new thread.
     *
     * @param socket Client socket.
     */
    MochaWorkerThread(Socket socket) {
        this.client = socket;
    }

    /**
     * Run method override for the MochaWorkerThread.
     */
    @Override
    public void run() {
        try {
            InputStream clientInput = this.client.getInputStream();
            OutputStream clientOutput = this.client.getOutputStream();

            new MochaClient(clientInput, clientOutput);

            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

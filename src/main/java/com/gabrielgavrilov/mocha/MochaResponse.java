package com.gabrielgavrilov.mocha;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class MochaResponse
{

    public StringBuilder header = new StringBuilder();
    public StringBuilder body = new StringBuilder();

    public void initializeHeader(String status, String contentType)
    {
        this.header.append("HTTP/1.0 " + status + "\r\n");
        this.header.append("Content-Type: " + contentType + "\r\n");
    }

    public void addHeader(String header, String value)
    {
        this.header.append(header + ": " + value + "\r\n");
    }

    public void status(String status)
    {
        if(!this.header.toString().contains("HTTP/1.0" + status))
            this.header.append("HTTP/1.0 " + status + "\r\n");
    }

    public void contentType(String contentType)
    {
        if(!this.header.toString().contains("Content-Type: " + contentType))
            addHeader("Content-Type", contentType);
    }

    public void cookie(String name, String value)
    {
        addHeader("Set-Cookie", name+"="+value);
    }

    public void send(String data)
    {
        this.body.append(data);
        appendEmpty();
    }

//    public void render(String fileName)
//    {
//        try
//        {
//            String fileContent = Files.readString(Paths.get(Mocha.VIEWS_DIRECTORY + fileName));
//            send(fileContent);
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//        }
//    }

    private void appendEmpty()
    {
        this.header.append("\r\n");
    }
}

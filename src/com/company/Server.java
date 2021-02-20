package com.company;

import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.*;


public class Server {

    private static final String HTTP_RESPONSE = "HTTP/1.1 200 OK\r\n"
            + "Date: "          + new Date().toString()     +"\r\n"
            + "Server: "        + "Apache/2.0.52 (CentOS)"  +"\r\n"
            + "Accept-Ranges: " + "bytes"                   +"\r\n"
            + "Content-Length: "+ "%s"                      +"\r\n"
            +"\r\n";

    public static void main(String[] args) {

	    int PORT = 8080;
	    ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setReuseAddress(true);

            while (true){
                Socket clientSocket = serverSocket.accept();

                HTTPRequest httpRequest = new HTTPRequest(clientSocket);

                new Thread(httpRequest).start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }


    }
}

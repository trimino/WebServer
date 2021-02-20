package com.company;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HTTPRequest implements Runnable{

    private final static Map<String, String> oldNewFileName = Map.of("index.html", "newIndex.html");
    private final static String[] responseCode = {"200", "301", "404"};
    private final static String responseHeader = "HTTP/1.1 ";
    private final static String CRLF = "\r\n";

    private Socket socket;
    private BufferedReader bufferedReader;
    private DataOutputStream dataOutputStream;

    public HTTPRequest(Socket socket) throws Exception{
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
    }

    @Override
    public void run() {
        String requestLine      = null;
        String headerLine       = null;
        String filename         = null;
        String statusLine       = null;
        String contentType      = null;
        String entityBody       = null;
        StringTokenizer tokens  = null;
        FileInputStream fis     = null;
        boolean fileExists      = true;


        try {
            // Get the request line of the client's request message
            requestLine = bufferedReader.readLine();
            System.out.println();
            System.out.println(requestLine);

            // Get and display the header of the client's request message
            while ((headerLine = bufferedReader.readLine()).length() != 0)
                System.out.println(headerLine);

            // Extract the filename from the requestLine
            tokens = new StringTokenizer(requestLine);
            tokens.nextToken();
        }catch (IOException e){
            System.err.println("ERROR: In getting request message and displaying message");
            e.printStackTrace();
        }


        try {
            // Append a '.' so that file request is within the current directory
            filename = String.format(".%s", tokens.nextToken());

            // Open the requested file
            fis = new FileInputStream(filename);
            fileExists= true;
        }catch (FileNotFoundException e){
            System.err.println("File Not found: " + filename + '\n');
            fileExists = false;
        }


        // Determine the file contents
        contentType = String.format("Content-type: %s", fileType(filename));

        // Construct the response message
        // Determine if file exists and generate response code
        if (fileExists)
            statusLine = responseHeader + responseCode[0] + " OK";
        else {
            statusLine = responseHeader + responseCode[2];
            entityBody = "<html>\n" +
                    "\t<head>\n"    +
                    "\t\t<title>Not Found</title>\n" +
                    "\t</head>\n"    +
                    "\t<body>\n"    +
                    "\t\t<b>404 Error: PAGE NOT FOUND</b>\n" +
                    "\t</body>\n</html>";
        }




        try {
            // Send the status line
            System.out.println(statusLine);
            dataOutputStream.writeBytes(statusLine);
            dataOutputStream.writeBytes(CRLF);

            // Send the content line
            System.out.println(contentType);
            dataOutputStream.writeBytes(contentType);
            dataOutputStream.writeBytes(CRLF);

            // Send a blank line to indicate the end of the header lines
            dataOutputStream.writeBytes(CRLF);
            dataOutputStream.writeBytes(CRLF);

            // Send the entity body. If the file exists send the file
            if (fileExists){
                sendBytes(fis, dataOutputStream);
                fis.close();
            }else {
                System.out.println(entityBody);
                dataOutputStream.writeBytes(entityBody);
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            System.err.println("ERROR: In the sendBytes method");
        }finally {
            try{
                if (this.dataOutputStream != null)
                    this.dataOutputStream.close();

                if (this.bufferedReader != null)
                    this.bufferedReader.close();

                if (this.socket != null)
                    this.socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }



    /*
     * Name         : sendBytes
     * Parameters   : The fileInput is the inputStream to the file that was requested by the client and the output stream sends the file to the client's input
     * Description  : Creates a 2k buffer and sends the copy requested file to the client's input stream using the output stream
     * Returns      : void
     */
    private static void sendBytes(FileInputStream fileInput, OutputStream os) throws Exception{
        // Construct a 1k buffer to hold bytes on their way to the socket
        byte[] buffer = new byte[2048];
        int bytes = 0;

        // Copy requested file into the socket's output stream
        while ((bytes = fileInput.read(buffer)) != -1)
            os.write(buffer, 0, bytes);
    }


    /*
     * Name         : fileType
     * Parameters   : The filename that the client has requested
     * Description  : Determines the file extension and checks for : htm, html, jpg, png, and css extensions. If none are applicable return default
     * Returns      : A string
     */
    private static String fileType(String filename){
        if (filename.endsWith(".htm") || filename.endsWith(".html"))
            return "text/html";

        else if (filename.endsWith(".jpg"))
            return "text/jpg";

        else if (filename.endsWith(".png"))
            return "text/png";

        else if (filename.endsWith(".css"))
            return "text/css";

        else
            return "application/octet-stream";
    }

}

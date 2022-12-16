package edu.spbu.server.files;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class MyClient {


    public static void main(String[] args) throws Exception {

        MyClient client = new MyClient();
        //Runs SendReq passing in the url and port from the command line
        client.sendGetRequest("lsaft/test.html", 8080);
        client.sendGetRequest("localhost", 8080);
        client.sendGetRequest("localhost/smth.html", 8080);
        client.sendGetRequest("ru.wikipedia.org/wiki/Заглавная_страница", 80);


    }

    public void sendGetRequest(String url, int port) throws Exception {

        String[] urlInfo = url.strip().split("/", 2);
        String getInfo;
        if (urlInfo.length == 2)
            getInfo = urlInfo[1];
        else if (urlInfo.length == 1) {
            getInfo = "";
        } else{
            System.out.println("Wrong input");
            return;
        }
        url = urlInfo[0];
        //Instantiate a new socket

        try (Socket socket = new Socket(url, port);
             OutputStream outStream = socket.getOutputStream();
             InputStream inStream = socket.getInputStream()){
            //Instantiates a new PrintWriter passing in the sockets output stream
            PrintStream printer = new PrintStream(outStream);
            //Prints the request string to the output stream
            printer.printf("GET /%s HTTP/1.1%n", getInfo);
            printer.printf("Host: %s:%s%n%n", url, port);
            printer.println("");
            printer.flush();



            //Creates a BufferedReader that contains the server response
            BufferedReader bufRead = new BufferedReader(new InputStreamReader(inStream));
            String outStr;

            //Prints each line of the response
            while ((outStr = bufRead.readLine()) != null) {
                System.out.println(outStr);
            }


            //Closes out buffer and writer

            printer.close();
            bufRead.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            System.out.println();
        }
    }

}

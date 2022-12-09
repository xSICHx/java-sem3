package edu.spbu.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class MyHandler extends Thread{
    private final Socket socket;
    private final String directory;
    MyHandler(Socket socket, String directory) {
        this.socket = socket;
        this.directory = directory;
    }
    @Override
    public void run(){
        try (InputStream inStream = socket.getInputStream(); OutputStream outStream = socket.getOutputStream()) {
            Scanner inScanner = new Scanner(inStream).useDelimiter("\r\n");
            String str = inScanner.next();
            System.out.println(str);
            List<String> request = List.of(str.split(" "));
            if (request.size() != 3){
                sendWrongRequestAnswer(outStream);
                return;
            }
            Path filePath = Path.of(this.directory, request.get(1));
            System.out.println(filePath);
            boolean errorFlag = !Objects.equals(request.get(0), "GET")
                    || !(Files.exists(filePath) && !Files.isDirectory(filePath));
            if (errorFlag){
                sendWrongRequestAnswer(outStream);
                return;
            }
            byte[] fileBytes = Files.readAllBytes(filePath);
            sendAnswer(outStream, 200, "OK", fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                System.out.println("socket closed\n");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void sendAnswer(OutputStream outputStream, int statusCode, String statusText, byte[] fileBytes){
        PrintStream printer = new PrintStream(outputStream);
        printer.printf("HTTP/1.1 %s %s%n", statusCode, statusText);
        printer.printf("Content-Type: %s%n", "text/html");
        printer.printf("Content-Length: %s%n%n", fileBytes.length);
        try {
            outputStream.write(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendWrongRequestAnswer(OutputStream outStream) throws IOException {
        Path filePath = Path.of(this.directory, "error.html");
        byte[] fileBytes = Files.readAllBytes(filePath);
        sendAnswer(outStream, 404,"Error", fileBytes);
    }
}

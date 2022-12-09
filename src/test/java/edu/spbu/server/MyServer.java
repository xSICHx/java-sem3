package edu.spbu.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
    private final int port;
    private final String directory;

    private MyServer(int port, String directory) {
        this.port = port;
        this.directory = directory;
    }

    public void start(){
        try (ServerSocket server = new ServerSocket(port)){
            while (true) {
                Socket socket = server.accept();
                MyHandler thread = new MyHandler(socket, directory);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        String directory = "src/test/java/edu/spbu/server/files";
        new MyServer(port, directory).start();
    }
}

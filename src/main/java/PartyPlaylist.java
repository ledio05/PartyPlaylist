import server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;



public class PartyPlaylist {
    static Server server;
    static ServerSocket _sSocket = null;
    static final int _port = 10001;

    public static void main(String[] args) throws IOException {
        System.out.println("Hello World!");

//        public static Server server = new Server();
//        public static Battlefield battle = new Battlefield();

            System.out.println("srv: Starting server...");

            _sSocket = new ServerSocket(_port);
            System.out.println("srv: Server is running in port " + _port);

            // repeatedly wait for connections, and process
            // noinspection InfiniteLoopStatement
            while (true) {
                // connect to client
                Socket clientSocket = _sSocket.accept();
                System.out.println("srv: New client");
                server = new Server(clientSocket);
                server.readRequest();
                //close the client socket
                clientSocket.close();

            }
        }
}
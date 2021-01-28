package server;

import java.io.*;
import java.net.Socket;
import java.util.*;


public class Server{
    private BufferedReader _in;
    private BufferedWriter _out;
    private StringBuilder _messageSeparator = new StringBuilder();
    private Verb _myVerb = Verb.OTHER;
    private String _message;
    private String _payload;
    private String[] _command;
    private final Map<String, String> __header = new HashMap<>();
    private boolean _http_first_line = true;

    private final String[] _allowedReq = {"users", "sessions", "packages", "transactions", "cards", "deck", "stats", "score", "battles", "tradings", "deck?format=plain"};


    public Server() {

    }

    public Server(Socket clientSocket) throws IOException {
        this._in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this._out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    }


    protected void setMyVerb(String myVerb) {
        switch (myVerb) {
            case "GET" -> _myVerb = Verb.GET;
            case "POST" -> _myVerb = Verb.POST;
            case "PUT" -> _myVerb = Verb.PUT;
            case "DELETE" -> _myVerb = Verb.DELETE;
            default -> _myVerb = Verb.OTHER;
        }
    }

    private void separateMessage() {
        //separate message
        String[] request = _messageSeparator.toString().split(System.getProperty("line.separator"));
        _messageSeparator = new StringBuilder();
        boolean skip = true;
        for (String line : request) {
            if (!line.isEmpty()) {
                if (_http_first_line) {
                    //saving folder and version
                    String[] first_line = line.split(" ");
                    setMyVerb(first_line[0]);
                    _message = first_line[1];
//                    _version = first_line[2];
                    _http_first_line = false;
                } else {

                    //saving the header
                    if (line.contains(": ") && !line.contains("{")) {
                        String[] other_lines = line.split(": ");
                        __header.put(other_lines[0], other_lines[1]);
                    }
                    //saving the payload
                    else {
                        if (skip) {
                            skip = false;
                        } else {
                            _messageSeparator.append(line);
                            _messageSeparator.append("\r\n");
                        }

                    }
                }
            }
        }
        _payload = _messageSeparator.toString();
    }

    public void readRequest() throws IOException {
        //read and save request
        //save request
        //code to read and print headers
        String headerLine;
        while((headerLine = _in.readLine()).length() != 0){
            _messageSeparator.append(headerLine);
        }
        while(_in.ready()){
            _messageSeparator.append((char) _in.read());
        }
        separateMessage();
        int status = checkRequest();
        performRequest(status);


    }

    @SuppressWarnings("unused")
    private void showHeader() {
        System.out.println("header:");
        for (Map.Entry<String, String> entry : __header.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }


    private int checkRequest() {

        //check if command is supported
        if(_myVerb == Verb.OTHER) {
            System.out.println("srv: Request method not supported");
        } else {
            _command = _message.split("/");
            if(Arrays.asList(_allowedReq).contains(_command[1])) {
                if (_command[1].equals("users") && _myVerb == Verb.POST) {
                    return 1;
                }else if (_command[1].equals("sessions") && _myVerb == Verb.POST) {
                    return 2;
                }else if (_command[1].equals("packages") && _myVerb == Verb.POST) {
                    return 3;
                }else if(_command[1].equals("cards") && _myVerb == Verb.GET){
                    return 5;
                }else if(_command[1].equals("deck") && _myVerb == Verb.GET){
                    return 6;
                }else if(_command[1].equals("deck") && _myVerb == Verb.PUT){
                    return 7;
                }else if(_command[1].equals("deck?format=plain") && _myVerb == Verb.GET){
                    return 8;
                }else if(_command[1].equals("users") && _myVerb == Verb.GET){
                    return 9;
                }else if(_command[1].equals("users") && _myVerb == Verb.PUT){
                    return 10;
                }else if(_command[1].equals("stats") && _myVerb == Verb.GET){
                    return 11;
                }else if(_command[1].equals("score") && _myVerb == Verb.GET){
                    return 12;
                }else if(_command[1].equals("battles") && _myVerb == Verb.POST){
                    return 13;
                }
                if (_command.length == 3) {
                    if (_command[1].equals("transactions") && _command[2].equals("packages")) {
                        if (_myVerb == Verb.POST) {
                            return 4;
                        }
                    } else {
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    private void performRequest(int status) throws IOException {
        if (status == 0) {
            _out.write("HTTP/1.1 400\r\n");
            _out.write("Content-Type: text/html\r\n");
            _out.write("\r\n");
            _out.write("Bad request!\r\n");
        } else {
            _out.write("HTTP/1.1 200 OK\r\n");
            _out.write("Content-Type: text/html\r\n");
            _out.write("\r\n");
        }
        _out.flush();
    }


    public static void log(String msg) {
        File file = new File("log.txt");

        // creates the file
        try {
            file.createNewFile();
            // creates a FileWriter Object
            FileWriter writer = new FileWriter(file, true);

            // Writes the content to the file
            writer.write("logged: " + msg + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private String[] getUserInfoHeader() {
        if(__header.get("Authorization") != null){
            String[] token = __header.get("Authorization").split(" ");
            return token[1].split("-");
        }
        return null;
    }

}
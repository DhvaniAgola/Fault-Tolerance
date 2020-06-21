package rm;

import schema.Replica;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Logger;

public class ReplicaThread implements Runnable {
    private Thread thread;
    private Process process;
    private String command;
    private Logger logs;
    private Replica replica;
    private HashMap<String, Replica> replicaList;

    public ReplicaThread(String command, Logger logs, Replica replica, HashMap<String, Replica> replicaList) {
        this.command = command;
        this.logs = logs;
        this.replica = replica;
        this.replicaList = replicaList;
    }

    @Override
    public void run() {
        try {
            // start the process
            Process process = Runtime.getRuntime().exec(command.trim());
            // store it for reference
            replica.setProcess(process);
            replicaList.put(replica.getName(), replica);

            System.out.println("Process is alive?: " + process.isAlive());
            this.logs.info(replica.getName() + " is up and running. port: " + replica.getPort());

            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException ioException) {
            this.logs.warning("The manager could not start the " + replica.getName() + " server.\nMessage: " + ioException.getMessage());
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this, "Replica Process");
            thread.start();
        }
    }
}

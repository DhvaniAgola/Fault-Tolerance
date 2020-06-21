package implementation;

import rm.ReplicaManager;
import rm.ReplicaThread;
import schema.Manager;
import schema.Replica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RmOperations {
    private Logger logs;
    HashMap<String, Replica> replicaList = new HashMap<>();
    List<Manager> rmList = new ArrayList<>();

    // Initialize the replicaList
    public RmOperations(String replicas, Logger logs, String replicaManagerDetails) {
        this.logs = logs;

        String[] theReplica = replicas.split(";");
        for (String element : theReplica) {
            String[] item = element.split(",");
            Replica replica = new Replica(item[0], Integer.parseInt(item[1]), item[2]);
            replicaList.put(item[0], replica);
        }

        String[] theRm = replicas.split(";");
        for (String element : theRm) {
            String[] item = element.split(",");
            Manager manager = new Manager(item[0], Integer.parseInt(item[1]));
            rmList.add(manager);
        }

    }

    public void startReplicas() {
        for(Map.Entry<String, Replica> entry : this.replicaList.entrySet()){
            startReplica(entry.getKey());
        }
    }


    // Start a Replica
    public void startReplica(String name) {
        Replica replica = this.replicaList.get(name);

        String command = "java -cp \"G:\\DSProject\\bin\" " + "Replica1." + replica.getPath();
//        String command = "java -cp G:\\DSProject\\bin\\Replica1." + replica.getPath();
       
        System.out.println(command);

        ReplicaThread rThread = new ReplicaThread(command, logs, replica, replicaList);
        rThread.start();
    }

    public void incrementFailureCount(String name) {
        Replica replica = this.replicaList.getOrDefault(name, null);

        if (replica != null)
            replica.incrementFailure();
    }

    public boolean isFailureCritical(String name) {
        Replica replica = this.replicaList.getOrDefault(name, null);

        return (replica != null) && replica.failureCountCritical();
    }

    public void killReplica(String name) {
        Replica replica = this.replicaList.get(name);

        if (replica.getProcess().isAlive())
            replica.reset();
    }

    public int getPort(String name) {
        Replica replica = this.replicaList.get(name);

        return replica.getPort();
    }

    public List<Manager> getReplicaManagers() {
        return rmList;
    }
}

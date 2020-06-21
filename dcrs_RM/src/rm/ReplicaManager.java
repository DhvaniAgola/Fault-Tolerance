package rm;

import implementation.RmOperations;
import data.Data;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Logger;

public class ReplicaManager {

    public static void main(String[] args) throws ClassNotFoundException {
        RmOperations rmOps;
        String replicaDetails = "COMP,1111,Server1;SOEN,2222,Server2;INSE,3333,Server3";
        String replicaManagerDetails = "132.205.46.158,8000;132.205.46.159,8000;"; // enter IP addresses & Ports of different RMs
        Logger logs = Logger.getLogger("replica-manager");


        rmOps = new RmOperations(replicaDetails, logs, replicaManagerDetails);

        try {
            DatagramSocket udpSocket = new DatagramSocket(8000);
            byte[] incoming = new byte[10000];
            logs.info("The UDP server for replica manager is up and running on port 8000");

            // start all the replicas
            rmOps.startReplicas();

            while (true) {
                DatagramPacket packet = new DatagramPacket(incoming, incoming.length);
                try {
                    udpSocket.receive(packet);
                    Data data = (Data)deserialize(packet.getData());
                    
                    if (data.replicaNumber.equalsIgnoreCase("1")) {
                        UdpProc udpProc = new UdpProc(logs, udpSocket, packet, rmOps);
                        udpProc.start();
                    }
                } catch (IOException ioe) {
                    logs.warning("Exception thrown while receiving packet.\nMessage: " + ioe.getMessage());
                }

                if (udpSocket.isClosed())
                    break;
            }

        } catch (SocketException s) {
            logs.warning("Exception: " + s);
            s.printStackTrace();
        }
    }
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                return o.readObject();
            }
        }
    }
}

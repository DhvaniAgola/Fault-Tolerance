package rm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import data.Data;
import implementation.RmOperations;
import schema.Manager;

public class UdpProc implements Runnable {
    private Thread thread;
    private Logger logs;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private RmOperations rmOps;
    private String data;
    private String array[]={"2","3"};

    public UdpProc(Logger logs, DatagramSocket socket, DatagramPacket packet, RmOperations rmOps) {
        this.logs = logs;
        this.socket = socket;
        this.packet = packet;
        try {
			System.out.println("35 : "+((Data)deserialize(this.packet.getData())).department);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.rmOps = rmOps;
    }



    @Override
    public void run() {
        try {
            Data data = (Data) deserialize(packet.getData());

            byte[] responseMessage = null;
            byte[] reply = null;

            String message;
            int opid=data.operationID;
            switch (opid) {
                case 0:
                	System.out.println("Switch Case Failure case");
                    this.replicaFails(data.department);
                    break;
                case 1:
                	System.out.println("Switch Case Start Replica");
                    data = this.startReplica(data);
                    break;
                case 9:
                    reply = this.replicaRequestData();
                    if (reply.length != 0)
                        responseMessage = reply;
                    else
                        responseMessage = serialize("No data obtained from other Replica Manager");
                    break;
                case 10:
                	System.out.println("Switch Case get replica data");
                    reply = this.getReplicaData(data);
                    if (reply.length != 0)
                        responseMessage = reply;
                    else
                        responseMessage = serialize("No data received from Replica");
                    break;
                default:
                    responseMessage = serialize("Server Communication Error");
                    System.out.println("Operation not found!");
                    break;
            }
            if(opid==10){
            	System.out.println("OPERATIONID: " + data.operationID +": packet port : "+packet.getPort());
            	DatagramPacket response = new DatagramPacket(responseMessage, responseMessage.length,
                        this.packet.getAddress(), packet.getPort());
            	
            	socket.send(response);
            }

        } catch (SocketException | ClassNotFoundException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        }
    }

    // To check for software failures
    private void replicaFails(String name) throws IOException {
        this.rmOps.incrementFailureCount(name);
        

        if (this.rmOps.isFailureCritical(name)) {
            logs.info("Fixing the replica");
            System.out.println("Failure Critical, Fixing the replica");
            try {
                Data data = new Data("1", 8);
                byte[] message = serialize(data);
                DatagramSocket socket = new DatagramSocket();
                InetAddress inetAddress = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(message, message.length, inetAddress, this.rmOps.getPort(name));
                System.out.println("Fix req sent to replica");
                socket.send(packet);

                socket.receive(packet);
//                String response = (String) deserialize(packet.getData());
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private Data startReplica(Data data) {
        this.rmOps.startReplica(data.department);
        System.out.println("Replica Started");
        HashMap<String, HashMap<String, Integer>> courseList = new HashMap<>();
        HashMap<String, HashMap<String, ArrayList<String>>> studentList = new HashMap<>();
        byte[] byteBuffer = new byte[10000];
        Data inData = null;
        try {
            // new socket to keep track of everything
            DatagramSocket socket = new DatagramSocket();

            // make the packet
            data.courseList = courseList;
            data.studentList = studentList;
            data.operationID = 10;
           

            // make packet and send to all other RMs
            
            System.out.println("Sending data Req to other RMs");
            List<Manager> list = this.rmOps.getReplicaManagers();
            int i = 0;
            	for(Manager manager : list){
            		System.out.println("MANAGER IP: " + manager.getIpAddress());
                	data.replicaNumber = array[i];
                	byte[] outgoing = this.serialize(data);
                    DatagramPacket datagramPacket = new DatagramPacket(outgoing, outgoing.length, InetAddress.getByName("132.205.46.156"), manager.getUdpPort());
                    socket.send(datagramPacket);
                    System.out.println("Sent req");
                    i++;
                    break;
            	}
            

            socket.setSoTimeout(20000);
            
            while (true) {
                try {
                	byte[] pachhiInBufer = new byte[1000];
                	DatagramPacket navuPacket = new DatagramPacket(pachhiInBufer, pachhiInBufer.length);
                	System.out.println("Receive data from other RM");
                    socket.receive(navuPacket);
                    System.out.println("Received data");
                    inData = (Data) this.deserialize(navuPacket.getData());
                    inData.operationID = 99;

                    byte[] response = this.serialize(inData);
                    InetAddress inetAddress = InetAddress.getByName("localhost");
                    System.out.println("DEPARTMENT: " + inData.department);
                    System.out.println("PORT: " + this.rmOps.getPort(inData.department));
                    DatagramPacket dataResponse = new DatagramPacket(response, response.length, inetAddress, this.rmOps.getPort(inData.department));
                    System.out.println("send data back to replica");
                    socket.send(dataResponse);
                    break;
                } catch (SocketTimeoutException exception) {
                	exception.printStackTrace();
                    this.logs.info("Connections to Replica Manager timed out.");
                    break;
                } catch (ClassNotFoundException exception) {
                	exception.printStackTrace();
                    this.logs.warning("Could not parse incoming data from Replica Manager.\nMessage: " + exception.getMessage());
                }
            }
            socket.close();

        } catch (SocketException exception) {
        	exception.printStackTrace();
            this.logs.warning("Error connecting to other RMs\nMessage: " + exception.getMessage());
        } catch (IOException exception) {
        	exception.printStackTrace();
            this.logs.warning("Error encoding/parsing the packet.\nMessage: " + exception.getMessage());
        }
       return inData;
    }


    private byte[] getReplicaData(Data data) {
        byte[] byteBuffer = new byte[10000];
        try {
            // for incoming packets
            byte[] inBuffer = new byte[10000];
            DatagramPacket incoming = new DatagramPacket(inBuffer, inBuffer.length);

            // new socket to keep track of everything
            DatagramSocket socket = new DatagramSocket();

            // make packet and send to all other RMs
            Data data1 = (Data)deserialize(this.packet.getData());
            System.out.println("Get replica data from replica");
            System.out.println("199 : "+data1.department);
            byte[] outgoing=serialize(data1);

            InetAddress inetAddress = InetAddress.getByName("localhost");
            DatagramPacket datagramPacket = new DatagramPacket(outgoing, outgoing.length, inetAddress, this.rmOps.getPort(data.department));
            socket.send(datagramPacket);
            System.out.println("Req Sent for data");

            socket.setSoTimeout(20000);

            while (true) {
                try {
                    socket.receive(incoming);
                    System.out.println("Receive data req");

                    Data inData = (Data) this.deserialize(incoming.getData());

                    if (inData != null) {
                    	System.out.println("Returning data to RM");
                    	System.out.println("data1 : "+inData.courseList);
                    	System.out.println("data2 : "+inData.studentList);
                        return this.serialize(inData);
                    }
                } catch (SocketTimeoutException exception) {
                	exception.printStackTrace();
                    this.logs.info("Connections to Replica Manager timed out.");
                    break;
                } catch (ClassNotFoundException exception) {
                	exception.printStackTrace();
                    this.logs.warning("Could not parse incoming data from Replica Manager.\nMessage: " + exception.getMessage());
                }
            }
            socket.close();

        } catch (SocketException exception) {
        	exception.printStackTrace();
            this.logs.warning("Error connecting to other RMs\nMessage: " + exception.getMessage());
        } catch (IOException exception) {
        	exception.printStackTrace();
            this.logs.warning("Error encoding/parsing the packet.\nMessage: " + exception.getMessage());
        }catch (ClassNotFoundException exception) {
        	
        	exception.printStackTrace();
            this.logs.warning("Error encoding/parsing the packet.\nMessage: " + exception.getMessage());
        }
        return byteBuffer;
    }

    private byte[] replicaRequestData() {
        HashMap<String, HashMap<String, Integer>> courseList = new HashMap<>();
        HashMap<String, HashMap<String, ArrayList<String>>> studentList = new HashMap<>();
        byte[] byteBuffer = new byte[10000];
        try {
            // for incoming packets
            byte[] inBuffer = new byte[10000];
            DatagramPacket incoming = new DatagramPacket(inBuffer, inBuffer.length);

            // new socket to keep track of everything
            DatagramSocket socket = new DatagramSocket();

            // make the packet
            Data data = new Data(courseList, studentList, 10);

            // make packet and send to all other RMs
            byte[] outgoing = this.serialize(data);
            for (Manager manager : this.rmOps.getReplicaManagers()) {
                DatagramPacket datagramPacket = new DatagramPacket(outgoing, outgoing.length, manager.getIpAddress(), manager.getUdpPort());
                socket.send(datagramPacket);
            }

            socket.setSoTimeout(20000);

            while (true) {
                try {
                    socket.receive(incoming);

                    Data inData = (Data) this.deserialize(incoming.getData());

                    if (inData != null) {
                        return this.serialize(inData);
                    }
                } catch (SocketTimeoutException exception) {
                    this.logs.info("Connections to Replica Manager timed out.");
                    break;
                } catch (ClassNotFoundException exception) {
                    this.logs.warning("Could not parse incoming data from Replica Manager.\nMessage: " + exception.getMessage());
                }
            }
            socket.close();

        } catch (SocketException exception) {
            this.logs.warning("Error connecting to other RMs\nMessage: " + exception.getMessage());
        } catch (IOException exception) {
            this.logs.warning("Error encoding/parsing the packet.\nMessage: " + exception.getMessage());
        }
        return byteBuffer;
    }


    public void start() {
        // One in coming connection. Forking a thread.
        if (thread == null) {
            thread = new Thread(this, "Udp Process");
            thread.start();
        }
    }

    public byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                return o.readObject();
            }
        }
    }

//    Operation
//    0 - Software Failure
//    1- Crash Failure
//    9 - replicaRequestData
}

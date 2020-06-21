package Replica1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import Replica1.Impl;
import data.Data;

public class Server3
{
	
	public static void main(String[] args) throws ClassNotFoundException, SecurityException, IOException
	{
		Logger logs=Logger.getLogger("ServerINSE");
		FileHandler FH = new FileHandler("ServerINSE.log", true);
		logs.addHandler(FH);
		Impl impl = new Impl("INSE",logs);

		new Thread(() -> {
			try {
				UDP(impl);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}).start();
		
		MulticastSocket aSocket = null;
		String strReply = "";
		int operation = 0;
		boolean breply = false;
				try
		{
			aSocket = new MulticastSocket(3333);
			aSocket.joinGroup(InetAddress.getByName("230.1.1.5"));
			logs.info("Replica3 running...");
			while (true)
			{
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				Data data = (Data) deserialize(request.getData());
				data.ack=true;
				request=new DatagramPacket(serialize(data), serialize(data).length, request.getAddress(),request.getPort());
				aSocket.send(request);
				operation = data.operationID;
				byte[] responseMessage = new byte[10000];
				switch (operation)
				{

					case 1:
						System.out.println("sid : "+data.studentID+":cid : "+data.courseID+":sem:"+data.semester);
						strReply = impl.enrollCourse(data.studentID, data.courseID, data.semester);
						break;

					case 2:
						strReply = impl.dropCourse(data.studentID, data.courseID);
						break;

					case 3:
						strReply = impl.getClassSchedule(data.studentID);
						break;

					case 4:
						strReply = impl.addCourse(data.courseID, data.semester,data.capacity);
						break;

					case 5:
						strReply = impl.removeCourse(data.courseID, data.semester);
						break;

					case 6:
						strReply = impl.listCourseAvailability(data.semester);
						break;

					case 7:
						strReply = impl.swapCourse(data.studentID, data.oldCourseID, data.newCourseID);
						break;

					case 11:
						strReply = impl.enrollInOtherDepartment(data.studentID, data.courseID, data.semester);
						break;

					case 21:
						strReply = impl.dropFromOtherDepartment(data.studentID, data.courseID, data.semester);
						break;

					case 31:
						strReply = impl.sendListCourseAvailabilityToOtherDept(data.semester);
						break;

					case 41:
						int intreply = impl.getCapacityOfSpecificCourseOfOtherDept(data.courseID, data.semester);
						strReply = String.valueOf(intreply);
						System.out.println(strReply);
						break;

					case 51:
						breply = impl.onlyRemoveCourse(data.studentID, data.courseID, data.semester);
						strReply = String.valueOf(breply);
						System.out.println(strReply);
						break;
					case 61:
						breply = impl.onlyAddCourse(data.studentID, data.courseID, data.semester);
						strReply = String.valueOf(breply);
						System.out.println(strReply);
						break;
					case 10:
                        Data records = impl.sendData(data);
                        responseMessage = serialize(records);
                        break;
					case 99:
						impl.receiveData(data);
					default:
						System.out.println("default");
				}
				if(data.operationID == 10) {					
					DatagramPacket reply = new DatagramPacket(responseMessage, responseMessage.length, InetAddress.getByName("localhost"), 8000);
					aSocket.send(reply);// reply sent
				} else {
					strReply = "1#" + strReply;
					DatagramPacket reply = new DatagramPacket(strReply.getBytes(), strReply.length(), InetAddress.getByName(data.ipAddress), 1234);
					aSocket.send(reply);// reply sent
				}
				logs.info("Reply sent to FE");
			}
		}
		catch (SocketException e)
		{
			System.out.println("Socket: " + e.getMessage());
		}
		catch (IOException e)
		{
			System.out.println("IO: " + e.getMessage());
		}
		finally
		{
			if (aSocket != null)
				aSocket.close();
		}

	}
	private static void UDP(Impl impl) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		Logger logs = Logger.getLogger("ServerCOMP");
		

		DatagramSocket aSocket = null;
		String strReply =new String();
		int operation = 0;
		boolean breply = false;
		try
		{
			FileHandler FH = new FileHandler("ServerCOMP.log", true);
			logs.addHandler(FH);
			aSocket = new DatagramSocket(3030);

			logs.info("Replica1 running...");

			while (true)
			{
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				System.out.println("while..41");
				aSocket.receive(request);
				logs.info("while..received");
				Data data = (Data) deserialize(request.getData());
				logs.info("SeqID: " + data.sequenceID);
				operation=data.operationID;
				System.out.println("operation id : "+operation);
				
				switch (operation)
				{


					case 11:
						strReply = impl.enrollInOtherDepartment(data.studentID, data.courseID, data.semester);
						break;

					case 21:
						strReply = impl.dropFromOtherDepartment(data.studentID, data.courseID, data.semester);
						break;

					case 31:
						strReply = impl.sendListCourseAvailabilityToOtherDept(data.semester);
						break;

					case 41:
						int intreply = impl.getCapacityOfSpecificCourseOfOtherDept(data.courseID, data.semester);
						strReply = String.valueOf(intreply);
						System.out.println(strReply);
						break;

					case 51:
						breply = impl.onlyRemoveCourse(data.studentID, data.courseID, data.semester);
						strReply = String.valueOf(breply);
						System.out.println(strReply);
						break;
					case 61:
						breply = impl.onlyAddCourse(data.studentID, data.courseID, data.semester);
						strReply = String.valueOf(breply);
						System.out.println(strReply);
						break;

					

					default:
						System.out.println("default");
				}
				System.out.println("strReply 216 : "+strReply);
					DatagramPacket reply = new DatagramPacket(strReply.getBytes(), strReply.getBytes().length, request.getAddress(),request.getPort());
					aSocket.send(reply);// reply sent
				
//				strReply = "1#" + strReply;
//				DatagramPacket reply = new DatagramPacket(strReply.getBytes(), strReply.length(), InetAddress.getByName("localhost"), 1234);
//				aSocket.send(reply);// reply sent
			}
		}
		catch (SocketException e)
		{
			System.out.println("Socket: " + e.getMessage());
		}
		catch (IOException e)
		{
			System.out.println("IO: " + e.getMessage());
		}
		finally
		{
			if (aSocket != null)
				aSocket.close();
		}
	}


	public static byte[] serialize(Object obj) throws IOException
	{
		try (ByteArrayOutputStream b = new ByteArrayOutputStream())
		{
			try (ObjectOutputStream o = new ObjectOutputStream(b))
			{
				o.writeObject(obj);
			}
			return b.toByteArray();
		}
	}

	public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException
	{
		try (ByteArrayInputStream b = new ByteArrayInputStream(bytes))
		{
			try (ObjectInputStream o = new ObjectInputStream(b))
			{
				return o.readObject();
			}
		}
	}
}

package FE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.omg.CORBA.ORB;

import data.Data;
import modInter.IDLInterPOA;

public class Impl extends IDLInterPOA
{
	private ORB						orb;
	private int						count			= 0;
	static HashMap<String, String>	replicaPorts	= new HashMap<>();
	private List<String>			majority		= new ArrayList<>();

	public void setORB(ORB orb_val)
	{
		majority.add("1");
		majority.add("2");
		majority.add("3");
		
		orb = orb_val;
	}

	@Override
	synchronized public String receiveAndForwardRequest(String strCourseID,String strTerm, int intCapacity, String strUserID, String strStudentID,String strOldCourseID, String strNewCourseID, int intOperation, int intSequenceId, String strDepartment,String error, String replicaNumber,  boolean ack)
	{
		// TODO Auto-generated method stub

		count = 0;
		String strReturn = "";
		replicaPorts.put("1", "");
		replicaPorts.put("2", "");
		replicaPorts.put("3", "");
		try
		{
			DatagramSocket aSocket = new DatagramSocket();
			InetAddress aHost = InetAddress.getByName("localhost");
			
			Data data = new Data(strCourseID, strTerm, intCapacity, strUserID, strStudentID, strOldCourseID, strNewCourseID, intOperation, intSequenceId, strDepartment,error,replicaNumber,ack,Inet4Address.getLocalHost().getHostAddress());
			
			byte b[] = serialize(data);
			DatagramPacket request = new DatagramPacket(b, b.length, aHost, 1235);
			aSocket.send(request);

//			Runnable R1 = () -> {
				return waitAndDetectFailure(aSocket, strDepartment,intOperation);
//			};
//			Thread t1 = new Thread(R1, "Thread2");
//			t1.start();

		}
		catch (SocketException SE)
		{
			SE.getMessage();
		}
		catch (UnknownHostException UHE)
		{
			UHE.getMessage();
		}
		catch (IOException IOE)
		{
			IOE.getMessage();
		}
		//startReceiving();
		return strReturn;
	}

	private String waitAndDetectFailure(DatagramSocket aSocket, String strDepartment,int intOperation)
	{
		// TODO Auto-generated method stub
		String strReturnClient="";
		try
		{
			//aSocket.setSoTimeout(10000);
			long startTime=System.currentTimeMillis();
			while((System.currentTimeMillis()-startTime)<3000)
			{
				
			}
			String strReturn = "";
			
			boolean bFlag = true;
//System.out.println("88");
			DatagramPacket request;
			InetAddress aHost = InetAddress.getByName("localhost");
//			replicaPorts.forEach((key, value) -> System.out.println(key + ":" + value));
			for (String i : replicaPorts.keySet())
			{
				
				if (replicaPorts.get(i) == "")
				{
					System.out.println(i + " is crashed");
//					System.out.println("dept : "+strDepartment);
					Data data=new Data("", "", 0, "", "", "", "", 1, 0, strDepartment, "", i, false, "");
					request = new DatagramPacket(serialize(data), serialize(data).length, aHost, 8000);
					aSocket.send(request);
//					System.out.println("sent to 1 for CF");
					request = new DatagramPacket(serialize(data), serialize(data).length, InetAddress.getByName("132.205.46.158"), 8000);
					aSocket.send(request);
//					System.out.println("sent to 2 for CF");
					request = new DatagramPacket(serialize(data), serialize(data).length, InetAddress.getByName("132.205.46.159"), 8000);
					aSocket.send(request);
//					System.out.println("sent to 3 for CF");
					strReturnClient = replicaPorts.get(majority.get((Integer.parseInt(i) + 1) % 3));
					bFlag = false;
					break;
				}

			}
			if (bFlag)
			{
				if(intOperation==6)
				{
					
					String array1[]=replicaPorts.get("1").split(",");
					String array2[]=replicaPorts.get("2").split(",");
					String array3[]=replicaPorts.get("3").split(",");
					List<String> l1=Arrays.asList(array1);
					List<String> l2=Arrays.asList(array2);
					List<String> l3=Arrays.asList(array3);
					if(l1.size()==l2.size() && l1.containsAll(l2))
					{
						if(l1.size()==l3.size() && l1.containsAll(l3))
						{
							strReturn = replicaPorts.get(majority.get(0));
							System.out.println("All replicas gave same reply");
							strReturnClient = replicaPorts.get(majority.get(0));
						}
						else
						{
							System.out.println("Software failure in replica 3");
							strReturn = replicaPorts.get(majority.get(0));
							Data data=new Data("", "", 0, "", "", "", "", 0, 0, strDepartment, "", "3", false, "");
							request = new DatagramPacket((serialize(data)), (serialize(data)).length, InetAddress.getByName("132.205.46.159"), 8000);
							aSocket.send(request);
//							System.out.println("sending 3 rm for SF");
							strReturnClient = replicaPorts.get(majority.get((3 + 1) % 3));
						}
					}
					else
					{
						if(l1.size()==l3.size() && l1.containsAll(l3))
						{
							System.out.println("Software failure in replica 2");
							strReturn = replicaPorts.get(majority.get(0));
							Data data=new Data("", "", 0, "", "", "", "", 0, 0, strDepartment, "", "2", false, "");
							request = new DatagramPacket(serialize(data),(serialize(data)).length, InetAddress.getByName("132.205.46.158"), 8000);
							aSocket.send(request);
//							System.out.println("sending 2 rm for SF");
							strReturnClient = replicaPorts.get(majority.get((2 + 1) % 3));
						}
						else
						{
//							System.out.println("1st in error");
							System.out.println("Software failure in replica 1");
							strReturn = replicaPorts.get(majority.get(1));
							Data data=new Data("", "", 0, "", "", "", "", 0, 0, strDepartment, "", "1", false, "");
							request = new DatagramPacket((serialize(data)), serialize(data).length, aHost, 8000);
							aSocket.send(request);
							
							strReturnClient = replicaPorts.get(majority.get((1 + 1) % 3));
						}
					}
				}
				else{
				if (replicaPorts.get(majority.get(0)).equalsIgnoreCase(replicaPorts.get(majority.get(1))))                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
				{
					if (replicaPorts.get(majority.get(0)).equalsIgnoreCase(replicaPorts.get(majority.get(2))))
					{
						strReturn = replicaPorts.get(majority.get(0));
						System.out.println("All replicas gave same reply");
						strReturnClient = replicaPorts.get(majority.get(0));
					}
					else
					{
						System.out.println("Software failure in replica 3");
						strReturn = replicaPorts.get(majority.get(0));
						Data data=new Data("", "", 0, "", "", "", "", 0, 0, strDepartment, "", "3", false, "");
						request = new DatagramPacket((serialize(data)), (serialize(data)).length, InetAddress.getByName("132.205.46.159"), 8000);
						aSocket.send(request);
//						System.out.println("sending 3 rm for SF");
						strReturnClient = replicaPorts.get(majority.get((3 + 1) % 3));
					}
				}
				else
				{
					if (replicaPorts.get(majority.get(0)).equalsIgnoreCase(replicaPorts.get(majority.get(2))))
					{
						System.out.println("Software failure in replica 2");
						strReturn = replicaPorts.get(majority.get(0));
						Data data=new Data("", "", 0, "", "", "", "", 0, 0, strDepartment, "", "2", false, "");
						request = new DatagramPacket(serialize(data),(serialize(data)).length, InetAddress.getByName("132.205.46.158"), 8000);
						aSocket.send(request);
//						System.out.println("sending 2 rm for SF");
						strReturnClient = replicaPorts.get(majority.get((2 + 1) % 3));
					}
					else
					{
						System.out.println("Software failure in replica 1");
						strReturn = replicaPorts.get(majority.get(1));
						Data data=new Data("", "", 0, "", "", "", "", 0, 0, strDepartment, "", "1", false, "");
						request = new DatagramPacket((serialize(data)), serialize(data).length, aHost, 8000);
						aSocket.send(request);
//						System.out.println("sending 1 rm for SF");
						strReturnClient = replicaPorts.get(majority.get((1 + 1) % 3));
					}
				}
			}
			}
			
		}
		catch (Exception e)
		{
			e.getMessage();
		}
		return strReturnClient;
	}

	@Override
	synchronized public void startReceiving(String strReply, int port, String whichReplica)
	{
		// TODO Auto-generated method stub
		System.out.println(port + "start receiving : " + strReply);
		replicaPorts.put(whichReplica.trim(), strReply.trim());
//		replicaPorts.put("2", "");
//		replicaPorts.put("3", strReply);
		replicaPorts.forEach((key, value) -> System.out.println(" : " + key + ":" + value));
		count++;
	}

	synchronized public static byte[] serialize(Object obj) throws IOException
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

	synchronized public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException
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

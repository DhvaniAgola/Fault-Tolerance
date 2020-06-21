package Sequencer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

import data.Data;

public class Sequencer
{
	static int						sequenceNumber	= 0;
	static HashMap<Integer, Data>	mapSeqIDData	= new HashMap<>();

	public static void main(String[] args) throws ClassNotFoundException
	{

		// TODO Auto-generated method stub
		new Thread(() -> {
			multiCast();
		}).start();
		try
		{
			DatagramSocket aSocket = new DatagramSocket(1235);
			while (true)
			{

				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				Data data = (Data) deserialize(request.getData());
				sequenceNumber++;
				System.out.println("seqNo : "+sequenceNumber);
				data.sequenceID=sequenceNumber;
				mapSeqIDData.put(sequenceNumber, data);
				mapSeqIDData.forEach((key,value)->System.out.println("map : "+key +":"+value));
			}
		}
		catch (Exception e)
		{
			e.getMessage();
		}
		

	}

	private static void multiCast()
	{
		// TODO Auto-generated method stub
		System.out.println("Thread started");
		String strReply = "", strRequest = "";
		HashMap<String, Integer> mapDeptPort = new HashMap<>();
		mapDeptPort.put("COMP", 1111);
		mapDeptPort.put("SOEN", 2222);
		mapDeptPort.put("INSE", 3333);
		int port;
		boolean bStatus = true;
		System.out.println("62");
		int p=0;
		
		try
		{
			DatagramSocket aSocket = new DatagramSocket(1236);
			System.out.println("66");
			while (true)
			{
				System.out.print("");
				while (!mapSeqIDData.isEmpty())
				{
					mapSeqIDData.forEach((key,value)->System.out.println("in while " +key +":"+value));
					for (int i : mapSeqIDData.keySet())
					{

						L1 :for (int j = 0; j < 2; j++)
						{						
							System.out.println("76");
							
						InetAddress aHost = InetAddress.getByName("230.1.1.5");
						Data data = mapSeqIDData.get(i);
						byte b[] = serialize(data);
						System.out.println("Department: " + data.department);
						int outPort = mapDeptPort.get(data.department);
						System.out.println("OutPort: " + outPort);
						DatagramPacket send = new DatagramPacket(b, b.length, aHost, outPort);
						aSocket.send(send);
						System.out.println("send : "+j);
						long currentTime = System.currentTimeMillis();
						int count = 0;
						W1:while (true)
						{
							byte incoming[] = new byte[1000];
							DatagramPacket inPacket = new DatagramPacket(incoming, incoming.length);
							System.out.println("while2");
							try
							{
							aSocket.setSoTimeout(1000);
							
							aSocket.receive(inPacket);
							}
							catch(SocketException e)
							{
						e.printStackTrace();
							}
							
							data = (Data) deserialize(inPacket.getData());
							System.out.println(data.ack + "SeqID: " + data.sequenceID);
							if (data.ack)
							{
								count++;
							}
							if (count==1)
							{
								mapSeqIDData.remove(i);
								break W1;
							}
						}
						System.out.println("104 : count : "+count);
						if (count == 1)
						{
							break L1;
						}
}

					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
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

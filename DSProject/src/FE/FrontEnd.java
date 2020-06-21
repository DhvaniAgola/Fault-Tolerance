package FE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import modInter.IDLInter;
import modInter.IDLInterHelper;

public class FrontEnd
{
	public static void main(String[] args)
	{
		ORB orb = ORB.init(args, null);
		try
		{
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();
			Impl impl = new Impl();
			impl.setORB(orb);
			new Thread(() -> {
				UDP();
			}).start();
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(impl);
			IDLInter href = IDLInterHelper.narrow(ref);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			NameComponent path[] = ncRef.to_name("FE");
			ncRef.rebind(path, href);
			System.out.println("CORBA Running...");
			orb.run();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	private static void UDP()
	{
		// TODO Auto-generated method stub
		DatagramSocket aSocket = null;
		String strReply = "";
		HashMap<Integer, String> replicaPorts = new HashMap<>();
		replicaPorts.put(1010, "");
		replicaPorts.put(2020, "");
		replicaPorts.put(3030, "");
		Impl impl = new Impl();
		String reply = "";
		try
		{
			aSocket = new DatagramSocket(1234);
			System.out.println("UDP Running...");
			int count=0;
			while (true)
			{
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				count++;
//				System.out.println("count from replica : "+count);
				reply = (new String(request.getData())).trim();
//				System.out.println(reply);
//				System.out.println("BYTE LENGTH:"+request.getLength());
//				System.out.println("STRING LENGTH:"+reply.length());
//				System.out.println("RESPONSE: " + reply);
				String[] strarr=reply.split("#");
				String replicanumber=strarr[0];
				String replicareply="";
				if(strarr.length>1)
				{
					replicareply=strarr[1];
				}
				impl.startReceiving(replicareply, request.getPort(), replicanumber);
				//				impl.startReceiving("hi", 123);
				/*DatagramPacket reply = new DatagramPacket(strReply.getBytes(), strReply.length(), request.getAddress(), request.getPort());
				aSocket.send(reply);// reply sent
				*/ }
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
}

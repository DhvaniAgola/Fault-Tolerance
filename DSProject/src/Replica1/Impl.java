package Replica1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import data.Data;

public class Impl
{
	private HashMap<String, HashMap<String, Integer>>		mapTermCrs;
	private HashMap<String, HashMap<String, ArrayList<String>>>	mapStudent= new HashMap<>();
	private HashMap<String, Integer>						mapCrsCap		= new HashMap<>();
	ArrayList<String>											lstCourse		= new ArrayList<>();
	private HashMap<String, ArrayList<String>>					mapStuDetails;
	private String											server;
	private HashMap<String, Integer>						mapPorts		= new HashMap<>();
	private ArrayList<String>									lstOtherDept	= new ArrayList<>();
	static boolean											SWFailureFlag	= false;
	//private String											strOtherDeptResponse	= "";

	public Impl(String server, Logger log)
	{
		mapPorts.put("COMP", 1010);
		mapPorts.put("SOEN", 2020);
		mapPorts.put("INSE", 3030);
		this.mapTermCrs = new HashMap<>();
		this.mapStudent = new HashMap<>();
		this.server = server;
	}

	public synchronized String enrollCourse(String strStudentID, String strCourseID, String strTerm)
	{
		// TODO Auto-generated method stub
		this.mapTermCrs.forEach((key, value) -> System.out.println("this.mapTermCrs ::: " + key + " - " + value));
		String strReturn = "", strUDPReply = "";
		getOtherDepartmet(strStudentID.substring(0, 4));
		System.out.println("44");
		mapCrsCap = new HashMap<>();
		mapStuDetails = new HashMap<>();
		mapStuDetails = null;
		int intSetBit = 0;
		int availCap = 0;
		if(!lstOtherDept.contains(strCourseID.substring(0,4))){
		if(!mapTermCrs.containsKey(strTerm))
		{
			strReturn="Course is not available";
			return strReturn;
		}
		
		mapCrsCap = this.mapTermCrs.get(strTerm);
		}
		else
		{
			intSetBit=1;
		}
		lstCourse = new ArrayList<>();
		
		int intCount3 = 0, intCount2 = 0;
		boolean bStatus = false, bFlag = true;

		if (this.mapStudent.containsKey(strStudentID))
		{
			System.out.println("55:"+strStudentID);
			mapStuDetails = this.mapStudent.get(strStudentID);
			for (String h : mapStuDetails.keySet())
			{
				List<String> n = new ArrayList<>();
				if (mapStuDetails.get(h).contains(strCourseID))
				{
					bFlag = false;
					break;
				}
			}
		}

		if (bFlag)
		{
			System.out.println("69");
			if (mapStuDetails != null)
			{

				intCount3 = mapStuDetails.get(strTerm).size();
			}

			if (intCount3 < 3)
			{
				System.out.println("79");
				if (lstOtherDept.contains(strCourseID.substring(0, 4)))
				{
					intSetBit = 1;
					intCount2 = getCountOfOtherDeptCourses(strStudentID);
				}
				if (intCount2 < 2)
				{
					if (intSetBit == 1)
					{
						strUDPReply = sendUDPToOtherDepartment("", strCourseID, strTerm, strCourseID.substring(0, 4), 41);//courseid
						System.out.println("87 : " + strUDPReply);
						availCap = Integer.parseInt(strUDPReply.trim());
						System.out.println("88 : " + availCap);
						if (availCap == -1)
						{
							strReturn = "Course is not available";
							return strReturn;
						}
						else if (availCap == 0)
						{
							strReturn = "No seats available for the course";
							return strReturn;
						}
					}
					else
					{
						if (mapCrsCap.containsKey(strCourseID))
						{
							System.out.println("59");
							availCap = this.mapTermCrs.get(strTerm).get(strCourseID);
							if (availCap == 0)
							{
								strReturn = "No seats available for the course";
								return strReturn;
							}

						}
						else
						{
							strReturn = "Course is not available";
							return strReturn;
						}
					}

					System.out.println("87");
					if (intSetBit == 1)
					{
						System.out.println("90");
						strUDPReply = sendUDPToOtherDepartment(strStudentID, strCourseID, strTerm, strCourseID.substring(0, 4), 11);//courseid
						if (strUDPReply != "")
						{
							strReturn = strUDPReply;
							if (mapStuDetails.containsKey(strTerm))
								lstCourse = mapStuDetails.get(strTerm);
							lstCourse.add(strCourseID);
							mapStuDetails.put(strTerm, lstCourse);
							this.mapStudent.put(strStudentID, mapStuDetails);
						}
						else
							strReturn = "you are not enrolled due to some server error";
						return strReturn;
					}
					System.out.println("98");
					if (intCount3 != 0)
					{

						lstCourse = mapStuDetails.get(strTerm);
					}
					lstCourse.add(strCourseID);
					mapStuDetails = new HashMap<>();
					if (this.mapStudent.containsKey(strStudentID))
						mapStuDetails = this.mapStudent.get(strStudentID);
					mapStuDetails.put(strTerm, lstCourse);
					this.mapStudent.put(strStudentID, mapStuDetails);
					mapCrsCap = this.mapTermCrs.get(strTerm);
					mapCrsCap.put(strCourseID, mapCrsCap.get(strCourseID) - 1);
					this.mapTermCrs.put(strTerm, mapCrsCap);
					this.mapTermCrs.forEach((key, value) -> System.out.println("this.mapTermCrs ::: " + key + " - " + value));
					strReturn = "You have successfully enrolled in the course";
				}
				else
				{
					strReturn = "You have enrolled in 2 other department courses";
					return strReturn;
				}
			}
			else
			{
				strReturn = "You have enrolled in 3 courses in this term";
				return strReturn;
			}

		}
		else
		{
			strReturn = "you are already enrolled in the course";
			return strReturn;
		}
		this.mapStudent.forEach((key, value) -> System.out.println("mapStudent::" + key + " : " + value));
		return strReturn;
	}

	public String enrollInOtherDepartment(String strStudentID, String strCourseID, String strTerm)
	{
		mapStuDetails = new HashMap<>();

		mapCrsCap = new HashMap<>();
		lstOtherDept = new ArrayList<>();
		if (this.mapStudent.containsKey(strStudentID))
		{
			mapStuDetails = this.mapStudent.get(strStudentID);
			if (mapStuDetails.containsKey(strTerm))
				lstCourse = mapStuDetails.get(strTerm);
		}

		lstCourse.add(strCourseID);
		mapStuDetails.put(strTerm, lstCourse);
		this.mapStudent.put(strStudentID, mapStuDetails);
		mapCrsCap = this.mapTermCrs.get(strTerm);
		mapCrsCap.put(strCourseID, mapCrsCap.get(strCourseID) - 1);
		this.mapTermCrs.put(strTerm, mapCrsCap);
		return "You have successfully enrolled in the course";
	}

	public int getCapacityOfSpecificCourseOfOtherDept(String strCourseID, String strTerm)
	{
		int intReturn;
		System.out.println("strTerm : " + strTerm);
		this.mapTermCrs.forEach((key, value) -> System.out.println("mapTermCrs::" + key + " : " + value));
		if (this.mapTermCrs.containsKey(strTerm))
		{
			if (this.mapTermCrs.get(strTerm).containsKey(strCourseID))
				intReturn = this.mapTermCrs.get(strTerm).get(strCourseID);
			else
				intReturn = -1;
		}
		else
			intReturn = -1;
		return intReturn;
	}

	public String swapCourse(String strStudentID, String strOldCourseID, String strNewCourseID)
	{
		String strReturn = "", strResponse = "", strTerm = "";
		int intCount2 = 0, intAvailCap = 0;
		boolean bStatus = false, bFlag = false;
		mapCrsCap = new HashMap<>();
		mapStuDetails = new HashMap<>();
		mapStuDetails = this.mapStudent.get(strStudentID);
		lstCourse = new ArrayList<>();
		for (String s : mapStuDetails.keySet())
		{
			lstCourse = mapStuDetails.get(s);
			if (lstCourse.contains(strOldCourseID))
			{
				bFlag = true;
				strTerm = s;
				break;
			}

		}
		if (!bFlag)
		{
			strReturn = "You are not enrolled in the old course";
			return strReturn;
		}
		lstCourse = this.mapStudent.get(strStudentID).get(strTerm);
		if (lstCourse.contains(strNewCourseID))
		{
			strReturn = "you are already enrolled in the course";
			return strReturn;
		}
		if (lstOtherDept.contains(strNewCourseID.substring(0, 4)))
		{
			System.out.println("245");
			String str = sendUDPToOtherDepartment(null, strNewCourseID, strTerm, strNewCourseID.substring(0, 4), 41);
			intAvailCap = Integer.parseInt(str.trim());
		}
		else
		{
			if(this.mapTermCrs.containsKey(strTerm))
			{
				if(this.mapTermCrs.get(strTerm).containsKey(strNewCourseID))
				{
					intAvailCap = this.mapTermCrs.get(strTerm).get(strNewCourseID);
					
				}
				else
				{
					strReturn = "Course is not available";
					return strReturn;
				}
			}
			else
			{
				strReturn = "Course is not available";
				return strReturn;
			}
		}
		if (intAvailCap == 0)
		{
			strReturn = "No seats available in the course";
			return strReturn;
		}
		if (lstOtherDept.contains(strNewCourseID.substring(0, 4)))
		{
			System.out.println("260");
			if (!lstOtherDept.contains(strOldCourseID.substring(0, 4)))
				intCount2 = getCountOfOtherDeptCourses(strStudentID);
			if (intCount2 == 2)
			{
				strReturn = "you are already enrolled in 2 other department courses";
				return strReturn;
			}
		}
		if (lstOtherDept.contains(strOldCourseID.substring(0, 4)))
		{
			System.out.println("271");
			strResponse = sendUDPToOtherDepartment(strStudentID, strOldCourseID, strTerm, strOldCourseID.substring(0, 4), 21);//oldcourse
			onlyRemoveCourse(strStudentID, strOldCourseID, strTerm);
		}
		else
			strResponse = dropCourse(strStudentID, strOldCourseID);
		if (strResponse != "" || strResponse.contains("success"))
		{
			bStatus = false;
			if (lstOtherDept.contains(strNewCourseID.substring(0, 4)))
			{
				strResponse = sendUDPToOtherDepartment(strStudentID, strNewCourseID, strTerm, strNewCourseID.substring(0, 4), 11);//newcourse
				onlyAddCourse(strStudentID, strNewCourseID, strTerm);
			}
			else
				strResponse = enrollCourse(strStudentID, strNewCourseID, strTerm);
		}
		else
		{
			strReturn = "Problem in dropping old course";
			return strReturn;
		}
		if (strResponse != "" || strResponse.contains("success"))
		{
			strReturn = "Swap done successfully";
			return strReturn;
		}
		else
		{
			bStatus = false;
			if (lstOtherDept.contains(strOldCourseID.substring(0, 4)))
			{
				strResponse = sendUDPToOtherDepartment(strStudentID, strOldCourseID, strTerm, strOldCourseID.substring(0, 4), 11);//oldcourse
				onlyAddCourse(strStudentID, strOldCourseID, strTerm);
			}
			else
				strResponse = enrollCourse(strStudentID, strOldCourseID, strTerm);
			if (strResponse != "" || strResponse.contains("success"))
				strReturn = "Problem in Swapping the courses";
			else
				strReturn = "Due to some problem you are not enrolled in new course and also dropped from old course";
			return strReturn;
		}
	}

	public String getClassSchedule(String strStudentID)
	{
		String strReturn = "";
		mapStuDetails = new HashMap<>();
		System.out.println("350");
		if (this.mapStudent.containsKey(strStudentID))
		{
			mapStuDetails = this.mapStudent.get(strStudentID);
			for (String term : mapStuDetails.keySet())
			{
				lstCourse = mapStuDetails.get(term);
				for (int i = 0; i < lstCourse.size(); i++)
					strReturn += term + ":" + lstCourse.get(i) + ",";
			}
		}
		else
			strReturn = "You are not enrolled in any course";
		//		if (!SWFailureFlag)
		//			strReturn = strReturn + "wrong";
		if(strReturn=="")
			strReturn = "You are not enrolled in any course";
		return strReturn;
	}

	public String dropCourse(String strStudentID, String strCourseID)
	{
		String strReturn = "", strTerm = "", strUDPReply = "";
		boolean bFlag = false, bStatus = false;
		mapCrsCap = new HashMap<>();
		System.out.println("313");
		this.mapStudent.forEach((key, value) -> System.out.println("this.mapStudent::" + key + " : " + value));
		mapStuDetails = new HashMap<>();
		mapStuDetails = this.mapStudent.get(strStudentID);
		lstCourse = new ArrayList<>();
		for (String s : mapStuDetails.keySet())
		{
			lstCourse = mapStuDetails.get(s);
			if (lstCourse.contains(strCourseID))
			{
				bFlag = true;
				strTerm = s;
				break;
			}

		}

		if (!bFlag)
		{
			strReturn = "You are not enrolled in the course";
			return strReturn;
		}
		System.out.println("334");
		if (lstOtherDept.contains(strCourseID.substring(0, 4)))
		{
			System.out.println("337" + strCourseID.substring(0, 4));
			strUDPReply = sendUDPToOtherDepartment(strStudentID, strCourseID, strTerm, strCourseID.substring(0, 4), 21);//courseid
			if (strUDPReply != "")
			{
				System.out.println("341");
				strReturn = strUDPReply;
				lstCourse.remove(strCourseID);
				mapStuDetails.put(strTerm, lstCourse);
				this.mapStudent.put(strStudentID, mapStuDetails);
			}
			else
				strReturn = "you are not dropped from course due to some server error";
			return strReturn;
		}
		System.out.println("351");
		lstCourse.remove(strCourseID);
		mapStuDetails.put(strTerm, lstCourse);
		this.mapStudent.put(strStudentID, mapStuDetails);
		System.out.println("term:" + strTerm);
		mapCrsCap = this.mapTermCrs.get(strTerm);
		mapCrsCap.forEach((key, value) -> System.out.println("mapCrsCap::" + key + " : " + value));
		int p = mapCrsCap.get(strCourseID);
		mapCrsCap.put(strCourseID, p + 1);
		this.mapTermCrs.put(strTerm, mapCrsCap);
		strReturn = "you are successfully dropped the course";

		return strReturn;
	}

	public String dropFromOtherDepartment(String strStudentID, String strCourseID, String strTerm)
	{
		String strReturn = "";
		System.out.println("369");
		lstCourse = new ArrayList<>();
		mapStuDetails = new HashMap<>();
		mapCrsCap = new HashMap<>();
		mapStuDetails = this.mapStudent.get(strStudentID);
		lstCourse = this.mapStudent.get(strStudentID).get(strTerm);
		mapCrsCap = this.mapTermCrs.get(strTerm);
		lstCourse.remove(strCourseID);
		mapStuDetails.put(strTerm, lstCourse);
		this.mapStudent.put(strStudentID, mapStuDetails);
		mapCrsCap.put(strCourseID, mapCrsCap.get(strCourseID) + 1);
		this.mapTermCrs.put(strTerm, mapCrsCap);
		strReturn = "you are successfully dropped the course";
		return strReturn;
	}

	public String addCourse(String strCourseID, String strTerm, int intCapacity)
	{
		getOtherDepartmet(strCourseID.substring(0, 4));
		boolean bFlag = true;
		String strReturn = "";

		mapCrsCap = new HashMap<>();
		if (this.mapTermCrs.containsKey(strTerm))
		{
			if (!this.mapTermCrs.get(strTerm).containsKey(strCourseID))
			{
				mapCrsCap = this.mapTermCrs.get(strTerm);
				bFlag = false;
			}
		}
		else
			bFlag = false;

		if (!bFlag)
		{
			//mapDetails.put("Capacity", intCapacity);
			mapCrsCap.put(strCourseID, intCapacity);
			this.mapTermCrs.put(strTerm, mapCrsCap);
			strReturn = "You have successfully added the course";
			System.out.println("You have successfully added the course");
		}
		else
		{
			strReturn = "Course is already added";
			System.out.println("Course is already added");
		}

		this.mapTermCrs.forEach((key, value) -> System.out.println(key + " : " + value));
		//logger.info("Resporse from Server : " + strReturn + " :: ThreadName : " + strThreadName);
		return strReturn;
	}

	public boolean onlyAddCourse(String strStudentID, String strCourseID, String strTerm)
	{
		mapStuDetails = new HashMap<>();
		lstCourse = new ArrayList<>();
		if (this.mapStudent.containsKey(strStudentID))
		{
			mapStuDetails = this.mapStudent.get(strStudentID);
			if (mapStuDetails.containsKey(strTerm))
			{
				lstCourse = mapStuDetails.get(strTerm);

			}
		}
		lstCourse.add(strCourseID);
		mapStuDetails.put(strTerm, lstCourse);
		this.mapStudent.put(strStudentID, mapStuDetails);
		return true;
	}

	public String removeCourse(String strCourseID, String strTerm)
	{
		// TODO Auto-generated method stub
		getOtherDepartmet(this.server);
		mapCrsCap = new HashMap<>();
		lstCourse = new ArrayList<>();
		String strReturn = "", strResponse = "";
		boolean bStatus = false;
		if (this.mapTermCrs.get(strTerm).containsKey(strCourseID))
		{
			for (String s : this.mapStudent.keySet())
			{
				System.out.println("424 :" + s);
				mapStuDetails = new HashMap<>();
				mapStuDetails = this.mapStudent.get(s);
				if (mapStuDetails.containsKey(strTerm))
				{
					lstCourse = mapStuDetails.get(strTerm);
					if (lstCourse.contains(strCourseID))
					{
						//						if (lstOtherDept.contains(s.substring(0, 4)))
						//						{
						//							System.out.println("437");
						//							strResponse = sendUDPToOtherDepartment(s, strCourseID, strTerm, 2);
						//						}
						//						else
						//						lstCourse.remove(strCourseID);
						//						mapStuDetails.put(strTerm, lstCourse);
						//						this.mapStudent.put(s, mapStuDetails);
						bStatus = onlyRemoveCourse(s, strCourseID, strTerm);
						System.out.println("441");
						if (bStatus)
							strResponse = sendUDPToOtherDepartment(s, strCourseID, strTerm, s.substring(0, 4), 51);
						//						strResponse = dropCourse(s, strCourseID);
						System.out.println("strResponse : " + strResponse);
						if (!bStatus || strResponse.equalsIgnoreCase("false"))
						{
							System.out.println("448");
							strReturn = "there is some problem to remove course";
							return strReturn;
						}

					}
				}
			}
			mapCrsCap = this.mapTermCrs.get(strTerm);
			mapCrsCap.remove(strCourseID);
			this.mapTermCrs.put(strTerm, mapCrsCap);

			strReturn = "You have successfully remove the course";
		}
		else
			strReturn = "Course id not exist";
		this.mapTermCrs.forEach((key, value) -> System.out.println(key + " : " + value));
		return strReturn;
	}

	public boolean onlyRemoveCourse(String strStudentID, String strCourseID, String strTerm)
	{
		mapStuDetails = new HashMap<>();
		lstCourse = new ArrayList<>();
		if (this.mapStudent.containsKey(strStudentID))
		{
			mapStuDetails = this.mapStudent.get(strStudentID);
			if (mapStuDetails.containsKey(strTerm))
			{
				lstCourse = mapStuDetails.get(strTerm);
				if (lstCourse.contains(strCourseID))
				{
					lstCourse.remove(strCourseID);
					mapStuDetails.put(strTerm, lstCourse);
					this.mapStudent.put(strStudentID, mapStuDetails);
					return true;
				}
			}
		}
		return false;
	}

	public String listCourseAvailability(String strTerm)
	{
		// TODO Auto-generated method stub
		String strReturn = "";
		System.out.println("458 :" + strTerm);
		getOtherDepartmet(this.server);
		if (this.mapTermCrs.containsKey(strTerm.trim()))
		{
			if(!this.mapTermCrs.get(strTerm).isEmpty())
			{
				for(String s:this.mapTermCrs.get(strTerm).keySet())
				{
				strReturn+=s+":"+this.mapTermCrs.get(strTerm).get(s)+",";
				}
			}
		
		System.out.println("strReturn : "+strReturn);}
		for (int i = 0; i < lstOtherDept.size(); i++)
		{
			strReturn += (sendUDPToOtherDepartment(null, null, strTerm, lstOtherDept.get(i), 31)).trim();//lstOtherDept.get(i)
		}
		if(strReturn.trim()=="")
		{
			strReturn="No Courses found";
		}
		System.out.println("strReturn 603 : "+strReturn.trim()+".");
		return strReturn;
	}

	public String sendListCourseAvailabilityToOtherDept(String strTerm)
	{
		String strReturn = "";
		System.out.println("strTerm : " + strTerm);
		this.mapTermCrs.forEach((key,value)->System.out.println(key +":"+value));
		if (this.mapTermCrs.containsKey(strTerm.trim()))
		{
			System.out.println("599");
			if(this.mapTermCrs.get(strTerm).isEmpty())
				strReturn="";
			else
			{
				for(String s:this.mapTermCrs.get(strTerm).keySet())
				{
				strReturn+=s+":"+this.mapTermCrs.get(strTerm).get(s)+",";
				}
			}
		}		
		System.out.println("602");
		return strReturn;
	}

	public void getOtherDepartmet(String strSelfDept)
	{
		String dept[] = new String[] { "COMP", "SOEN", "INSE" };
		lstOtherDept.clear();
		int index = Arrays.asList(dept).indexOf(strSelfDept);
		for (int i = 0; i < dept.length - 1; i++)
			lstOtherDept.add(dept[++index % dept.length]);
	}

	public String sendUDPToOtherDepartment(String strStudentID, String strCourseID, String strTerm, String strTargetDept, int intOperation)
	{
		String strReturn = "",error="",replicaNumber="";
		boolean ack=false;
		try
		{
			DatagramSocket aSocket = new DatagramSocket();
			Data data = new Data(strCourseID, strTerm, 0, "", strStudentID, "", "", intOperation, 0, strTargetDept,error,replicaNumber,ack,"");
			byte b[] = serialize(data);
			InetAddress aHost = InetAddress.getByName("localhost");
			int serverPort = mapPorts.get(strTargetDept);
			DatagramPacket request = new DatagramPacket(b, b.length, aHost, serverPort);
			aSocket.send(request);
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			System.out.println("500 : " + new String(reply.getData()));
			strReturn = new String(reply.getData());
			return strReturn;
		}
		catch (SocketException SE)
		{
			SE.getMessage();
			return strReturn;
		}
		catch (UnknownHostException UHE)
		{
			UHE.getMessage();
			return strReturn;
		}
		catch (IOException IOE)
		{
			IOE.getMessage();
			return strReturn;
		}
	}

	public int getCountOfOtherDeptCourses(String strStudentID)
	{
		int intCount = 0;
		mapStuDetails = new HashMap<>();
		if (this.mapStudent.containsKey(strStudentID))
		{
			mapStuDetails = this.mapStudent.get(strStudentID);
			for (String p : mapStuDetails.keySet())
			{
				lstCourse = mapStuDetails.get(p);
				for (int i = 0; i < lstCourse.size(); i++)
				{
					if (lstCourse.get(i).matches("(" + lstOtherDept.get(0) + "|" + lstOtherDept.get(1) + ").*"))
						intCount++;
				}
			}
		}
		return intCount;
	}

	public void setSWFailureFlag()
	{
		SWFailureFlag = true;
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
	public Data sendData(Data data){
        // the records and studentlist are the two HashMaps
        data.courseList = this.mapTermCrs;
        data.studentList = this.mapStudent;
        data.operationID = 10;
        
        return data;
    }
	public void receiveData(Data data)
	{
		this.mapTermCrs=data.courseList;
		this.mapStudent=data.studentList;
	}

}

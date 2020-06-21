package Client;

import java.net.Inet4Address;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import modInter.IDLInter;
import modInter.IDLInterHelper;

public class Client
{
	static IDLInter impl;

	public static void main(String[] args)
	{
		ORB orb = ORB.init(args, null);
		try
		{
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			impl = IDLInterHelper.narrow(ncRef.resolve_str("FE"));
			//String strFinalReply = impl.enrollCourse("strID", " strStudentID", " strCourseID", " strTerm", 0, " strThreadName");
			//			bStatus = validate(intOperation, strUserID, strStudentID, strCourseID, strTerm, strDepartment, strN	ewCourseID, strOldCourseID, intSequenceId, intCapacity);

			String s=Inet4Address.getLocalHost().getHostAddress();
			System.out.println(s);
			
//			validate(4, "INSEA0001", "", "INSE6767", "FALL", "INSE", "", "", 0, 2);
//			validate(4, "SOENA0001", "", "SOEN6767", "FALL", "SOEN", "", "", 0, 2);
//			validate(1, "COMPA0001", "COMPS1001", "INSE6767", "FALL", "COMP", "", "", 0, 0);
//			validate(4, "SOENA0001", "", "SOEN0001", "SUMMER", "SOEN", "", "", 0, 2);
			
//			validate(1, "INSEA0001", "INSES1001", "INSE0001", "SUMMER", "INSE", "", "", 0, 0);
//			validate(1, "INSEA0001", "INSES1002", "INSE0001", "SUMMER", "INSE", "", "", 0, 0);
//			validate(1, "INSEA0001", "INSES1003", "INSE0001", "SUMMER", "INSE", "", "", 0, 0);
//			validate(1, "COMPA0001", "COMPS1002", "SOEN0001", "SUMMER", "COMP", "", "", 0, 0);
//			validate(3, "COMPA0001", "COMPS1001", "", "", "COMP", "", "", 0,0);
//			validate(7, "COMPA0001", "COMPS1001", "", "FALL", "COMP", "SOEN6767", "INSE6767", 0, 0);
			
			
//			validate(4, "INSEA0001", "", "INSE0002", "SUMMER", "INSE", "", "", 0, 2);
//			validate(4, "SOENA0001", "", "SOEN0002", "SUMMER", "SOEN", "", "", 0, 2);
//			validate(4, "INSEA0001", "", "INSE0003", "SUMMER", "INSE", "", "", 0, 2);
//			validate(4, "SOENA0001", "", "SOEN0003", "SUMMER", "SOEN", "", "", 0, 2);
//			validate(4, "INSEA0001", "", "INSE0004", "SUMMER", "INSE", "", "", 0, 2);
//			validate(4, "SOENA0001", "", "SOEN0004", "SUMMER", "SOEN", "", "", 0, 2);
			
//			validate(4, "COMPA0001", "", "COMP0001", "SUMMER", "COMP", "", "", 0, 2);
//			validate(5, "INSEA0001", "", "INSE0001", "SUMMER", "INSE", "", "", 0, 0);
//			
				
//				validate(2, "SOENA0001", "SOENS1002", "COMP0001", "SUMMER", "SOEN", "", "", 0, 0);
				
				
//				validate(6, "COMPA0001", "", "", "SUMMER", "COMP", "", "", 0, 0);
			
			
//			validate(4, "INSEA0001", "", "INSE0003", "SUMMER", "INSE", "", "", 0, 1);
			Runnable R1 = () -> {
				validate(1, "INSEA0001", "INSES1002", "INSE0003", "SUMMER", "INSE", "", "", 0, 0);
			};
			Runnable R2 = () -> {
				validate(1, "INSEA0001", "INSES1003", "INSE0003", "SUMMER", "INSE", "", "", 0, 0);
			};
			
			Thread t1 = new Thread(R1, "Thread1");
			Thread t2 = new Thread(R2, "Thread2");
			t1.start();
			t2.start();

		}
		catch (Exception e)
		{
			e.getMessage();
		}
	}

	public static synchronized void validate(int intOperation, String strUserID, String strStudentID, String strCourseID, String strTerm, String strDepartment, String strNewCourseID, String strOldCourseID, int intSequenceId, int intCapacity)
	{
		String strFinalReply = "";
		if (intOperation < 1 && intOperation > 7)
			return;
		else
		{
			if(intOperation>3 && intOperation<7)
			{
				if(strStudentID!="")
				{
					System.out.println("you don't have rights to perform this operation");
					return;
				}
			}
			else
			{
				System.out.println("Enter valid operation ID");
			}
		}
		if (strUserID == "" && strStudentID == "")
		{
		System.out.println("Please enter valid userid or studentid");
			return;
		
		}else
		{
			if (!(strUserID != "" && strUserID.substring(4, 5).equalsIgnoreCase("a")))
			{
				System.out.println("enter valid studentid");
				return;
			}
				
			else
			{
				if (!strUserID.substring(0, 4).equalsIgnoreCase(strDepartment))
				{
					System.out.println("You can send request to your own department");
					return;
				}
				}
			if ((strStudentID != "" && !strStudentID.substring(4, 5).equalsIgnoreCase("s")))
			{
			System.out.println("enter valid studentid");
				return;
			}
		}
		strFinalReply = impl.receiveAndForwardRequest(strCourseID, strTerm, intCapacity, strUserID, strStudentID, strOldCourseID, strNewCourseID, intOperation, intSequenceId, strDepartment,"","",false);
		System.out.println("Client : " + strFinalReply);
	}
}

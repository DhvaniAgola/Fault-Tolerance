package modInter;

/**
* modInter/IDLInterHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from IDL.idl
* Friday, November 30, 2018 8:16:08 PM EST
*/

public final class IDLInterHolder implements org.omg.CORBA.portable.Streamable
{
  public modInter.IDLInter value = null;

  public IDLInterHolder ()
  {
  }

  public IDLInterHolder (modInter.IDLInter initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = modInter.IDLInterHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    modInter.IDLInterHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return modInter.IDLInterHelper.type ();
  }

}

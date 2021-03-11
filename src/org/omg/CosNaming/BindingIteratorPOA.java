package org.omg.CosNaming;


/**
* org/omg/CosNaming/BindingIteratorPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../../src/share/classes/org/omg/CosNaming/nameservice.idl
* Wednesday, November 28, 2012 8:21:02 AM PST
*/


/**
   * The BindingIterator interface allows a client to iterate through
   * the bindings using the next_one or next_n operations.
   * 
   * The bindings iterator is obtained by using the <tt>list</tt>
   * method on the <tt>NamingContext</tt>. 
   * @see org.omg.CosNaming.NamingContext#list
   */
public abstract class BindingIteratorPOA extends org.omg.PortableServer.Servant
 implements org.omg.CosNaming.BindingIteratorOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("next_one", new java.lang.Integer (0));
    _methods.put ("next_n", new java.lang.Integer (1));
    _methods.put ("destroy", new java.lang.Integer (2));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {

  /**
     * This operation returns the next binding. If there are no more
     * bindings, false is returned.
     * 
     * @param b the returned binding
     */
       case 0:  // CosNaming/BindingIterator/next_one
       {
         org.omg.CosNaming.BindingHolder b = new org.omg.CosNaming.BindingHolder ();
         boolean $result = false;
         $result = this.next_one (b);
         out = $rh.createReply();
         out.write_boolean ($result);
         org.omg.CosNaming.BindingHelper.write (out, b.value);
         break;
       }


  /**
     * This operation returns at most the requested number of bindings.
     * 
     * @param how_many the maximum number of bindings tro return <p>
     * 
     * @param bl the returned bindings
     */
       case 1:  // CosNaming/BindingIterator/next_n
       {
         int how_many = in.read_ulong ();
         org.omg.CosNaming.BindingListHolder bl = new org.omg.CosNaming.BindingListHolder ();
         boolean $result = false;
         $result = this.next_n (how_many, bl);
         out = $rh.createReply();
         out.write_boolean ($result);
         org.omg.CosNaming.BindingListHelper.write (out, bl.value);
         break;
       }


  /**
     * This operation destroys the iterator.
     */
       case 2:  // CosNaming/BindingIterator/destroy
       {
         this.destroy ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:omg.org/CosNaming/BindingIterator:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public BindingIterator _this() 
  {
    return BindingIteratorHelper.narrow(
    super._this_object());
  }

  public BindingIterator _this(org.omg.CORBA.ORB orb) 
  {
    return BindingIteratorHelper.narrow(
    super._this_object(orb));
  }


} // class BindingIteratorPOA

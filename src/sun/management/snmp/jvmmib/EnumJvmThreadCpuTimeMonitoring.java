/*
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package sun.management.snmp.jvmmib;

//
// Generated by mibgen version 5.0 (06/02/03) when compiling JVM-MANAGEMENT-MIB.
//

// java imports
//
import java.io.Serializable;
import java.util.Hashtable;

// RI imports
//
import com.sun.jmx.snmp.Enumerated;

/**
 * The class is used for representing "JvmThreadCpuTimeMonitoring".
 */
public class EnumJvmThreadCpuTimeMonitoring extends Enumerated implements Serializable {

    protected static Hashtable<Integer, String> intTable =
            new Hashtable<Integer, String>();
    protected static Hashtable<String, Integer> stringTable =
            new Hashtable<String, Integer>();
    static  {
        intTable.put(new Integer(3), "enabled");
        intTable.put(new Integer(4), "disabled");
        intTable.put(new Integer(1), "unsupported");
        stringTable.put("enabled", new Integer(3));
        stringTable.put("disabled", new Integer(4));
        stringTable.put("unsupported", new Integer(1));
    }

    public EnumJvmThreadCpuTimeMonitoring(int valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }

    public EnumJvmThreadCpuTimeMonitoring(Integer valueIndex) throws IllegalArgumentException {
        super(valueIndex);
    }

    public EnumJvmThreadCpuTimeMonitoring() throws IllegalArgumentException {
        super();
    }

    public EnumJvmThreadCpuTimeMonitoring(String x) throws IllegalArgumentException {
        super(x);
    }

    protected Hashtable getIntTable() {
        return intTable ;
    }

    protected Hashtable getStringTable() {
        return stringTable ;
    }

}

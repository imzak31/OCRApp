package com.iq.logistics.common;

import java.util.UUID;

public class GlobalVariable {
    public static final String SAVED_IN_INFOSET = "saved_in_infoset";
    public static final String SAVED_IN_IP = "saved_in_ip";
    public static final String SAVED_IN_PORT = "saved_in_port";
    public static final String SAVED_IN_MAC = "saved_in_port";

    public static String SERVER_IP = "192.168.2.6"; // The SERVER_IP must be the same in server and client
    public static int PORT = 62222;

    public static String MAC_ADDRESS = null;
    public static final UUID myUUID = UUID.fromString("02b24490-f5f9-11e9-aaef-0800200c9a66");
}

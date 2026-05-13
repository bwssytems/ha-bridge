package com.bwssystems.HABridge;

public class EchoContext {
    private static String lastEchoIp = null;

    public static void setLastEchoIp(String ip) {
        lastEchoIp = ip;
    }

  public static String getLastEchoIp() {
        return lastEchoIp;
    }
}

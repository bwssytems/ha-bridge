package com.test;
 
import java.net.InetAddress;
import java.util.Arrays;
 
public class TestBean {
    public static void main(String[] args) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            System.out.println("Using InetAddress");
            System.out.println("Host Address: "+ address.getHostAddress());
            System.out.println("Host Name: "+ address.getHostName());
            System.out.println("Address List: "+ Arrays.toString(InetAddress.getAllByName(address.getHostName())));
            System.out.println("CanonicalHostName: "+ address.getCanonicalHostName());
            System.out.println("Address: "+ address.getAddress());
            System.out.println("LocalHost: "+ address.getLocalHost());
            System.out.println("LoopbackAddress: "+ address.getLoopbackAddress());
             
            String os = "os.name";
            String version = "os.version";
            String arch = "os.arch";
            System.out.println("Name of the OS: "+ System.getProperty(os));
            System.out.println("Version of the OS: "+ System.getProperty(version));
            System.out.println("Architecture of the OS: "+ System.getProperty(arch));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
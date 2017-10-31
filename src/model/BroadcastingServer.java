package model;

import java.net.*;

public class BroadcastingServer implements Runnable {
    private String hostname = "255.255.255.255";
    private int port = 2855;
    private InetAddress host;
    private DatagramSocket socket;
    DatagramPacket packet;
    public static boolean running = true;

    public BroadcastingServer() {
        running = true;
    }

    public void run() {
        try {
            host = InetAddress.getByName(hostname);
            //System.out.println(host);
            socket = new DatagramSocket(null);
            System.out.println("Started multicasting receiver");
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (running) {
            try {
                packet = new DatagramPacket(new byte[100], 100, host, port);
                long time = new java.util.Date().getTime();
                byte[] outBuffer = (time + "").getBytes();
                packet.setData(outBuffer);
                packet.setLength(outBuffer.length);
                socket.send(packet);
                System.out.println("Broadcasting packets started..");
                Thread.sleep(2000);
            } catch (Exception e) {
                System.out.println("Exception Occurred: " + e);
                e.printStackTrace();
            }
        }
        socket.close();
    }
}
package model;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ScrollPane;

import view.View;

import java.io.*;
import java.net.*;
import java.util.*;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

public class Sender implements Runnable {
    public static HashMap<InetAddress, ReceiverDetails> ipMapName = new HashMap<>();
    public static HashMap<InetAddress, String> ipMapDate = new HashMap<>();
    public static ScrollPane scrollPane = null;
    public static View view = null;

    public Sender(View view) {
        this.view = view;
        this.scrollPane = view.getScrollPane();
    }

    static class GetName implements Runnable {
        InetAddress address;

        public GetName(InetAddress address) {
            this.address = address;
        }

        public void run() {
            try (Socket clientSocket = new Socket(address, 5000)) {
                System.out.println("Connected to Server....");
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                String xml = dataInputStream.readUTF();

                SAXBuilder builder = new SAXBuilder();
                Document document = builder.build(new ByteArrayInputStream(xml.getBytes()));
                Element classElement = document.getRootElement();
                Element details = classElement.getChild("details");
                Element nameElement = details.getChild("name");
                Element deviceNameElement = details.getChild("device-name");
                Element imageElement = details.getChild("image");
                String name = nameElement.getText();
                String deviceName = deviceNameElement.getText();
                String image = imageElement.getText();
                System.out.println("name received " + name);
                System.out.println("Device Name received: " + deviceName);
                System.out.println("image received " + image);

                ipMapName.put(address, new ReceiverDetails(name, image, deviceName));
                // view.getScrollPane().setContent(getUpdatedGridPane());
            } catch (IOException ex) {
                System.out.println("Exception occurred in GetName: " + ex);
            } catch (JDOMException ex) {
                ex.printStackTrace();
            }
        }
    }

    static class RecieveDatagram implements Runnable {
        public static final int DEFAULT_PORT = 2855;
        private DatagramSocket socket;
        private DatagramPacket packet;

        public void run() {
            try {
                socket = new DatagramSocket(DEFAULT_PORT);
            } catch (Exception ex) {
                System.out.println("Problem creating socket on port: " + DEFAULT_PORT);
                return;
            }

            packet = new DatagramPacket(new byte[100], 100);

            while (true) {
                try {
                    System.out.println("Waiting for data packets .....");
                    socket.receive(packet);
                    //String stime = ipMapDate.get(packet.getAddress());
                    System.out.println("Packet received for " + packet.getAddress());
                    //long time = Long.parseLong( stime );
                    //System.out.println("packet receieved for " + stime + "adress: " + packet.getAddress());
                    long currentTime = new Date().getTime();
                    String current = currentTime + "";

                    //String scurrent = "" + (currentTime - 10000);
                    if ( true ) {
                        System.out.println("Got a new packet");
                        ipMapDate.put(packet.getAddress(), current);
                        System.out.println("value of ipMapName: " + ipMapName.get(packet.getAddress()));
                        if (ipMapName.get(packet.getAddress()) == null) {
                            Thread getName = new Thread(new GetName(packet.getAddress()));
                            getName.start();
                        }
                    }
                } catch (IOException ex) {
                    System.out.println("Exception in RecieveDatagram: " + ex);
                } catch (NullPointerException ex) {
                    System.out.println("Null Pointer exception occured.");
                }
            }
        }
    }

    public static class UpdateHash implements Runnable {
        public static boolean wait = false;

        public void run() {
            try {
                while (true) {
                    //Set<Map.Entry<InetAddress, ReceiverDetails>> detailsSet = ipMapName.entrySet();
                    try {
                        Set<Map.Entry<InetAddress, String>> timeSet = ipMapDate.entrySet();
                        String comparingTime = "" + (new Date().getTime() - 7000);
                        for (Map.Entry<InetAddress, String> me : timeSet) {
                            InetAddress address = me.getKey();
                            String currTime = me.getValue();
                            if (comparingTime.compareTo(currTime) > 0)
                                ipMapName.remove(address);
                        }
                        System.out.println("Printing ipMapName in UpdateHash: ");
                        Platform.runLater(() -> view.getScrollPane().setContent(getUpdatedGridPane()));
                    } catch (Exception ex) {
                        System.out.println("Exception while synchronising the Maps: " + ex);
                    }
                    Thread.sleep(5000);
                    while (wait) {
                        try {
                            Thread.sleep(700);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Exception while synchronising the Maps: " + e);
            }
        }

        public static void setWait(boolean setWait) {
            wait = setWait;
        }
    }

    private static void printIPMapName() {
        Set<Map.Entry<InetAddress, ReceiverDetails>> set = ipMapName.entrySet();
        //System.out.println("Printing the IPMapName");
        for (Map.Entry<InetAddress, ReceiverDetails> me : set) {
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
        }
    }

    public static GridPane getUpdatedGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setStyle("-fx-background-color: #bdbdbd;");
        gridPane.setVgap(10);

        final int[] i = new int[1];
        i[0] = 1;
        ipMapName.forEach((InetAddress ip, ReceiverDetails receiverDetails) -> {
            gridPane.add(view.createUserIcon(receiverDetails.getName(), receiverDetails.getComputerName(), ip, receiverDetails.getImage()), 0, i[0]++);
        });
        Label notificationLbl = new Label("\t\t\t\tUpdating receivers' list. \nPlease make sure Sender and Receiver are on same Local Area Network.");
        notificationLbl.setStyle("-fx-font-color: blue;" + "-fx-font-size: 20px;" + "-fx-font-weight: bold;" + "-fx-underline: false;");
        gridPane.add(notificationLbl, 0, i[0]);
        return gridPane;
    }

    public void run() {
        Thread recieveDatagram = new Thread(new RecieveDatagram());
        Thread updateHash = new Thread(new UpdateHash());
        updateHash.start();
        recieveDatagram.start();
        /*
        while( true ){
            try{
                //view.getScrollPane().setContent(getUpdatedGridPane());
                //View.updateGridPane();
                //Thread.sleep(5000);
            } catch (InterruptedException e){
                System.out.println("Sender thread was interrupted");
            }
        }*/
    }
}

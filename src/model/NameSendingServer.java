package model;

import java.io.*;
import java.net.*;
import java.util.*;
import org.jdom2.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class NameSendingServer implements Runnable {
    ServerSocket serverSocket;
    Socket clientSocket;
    String name = "";
    String xml;

    public static boolean running = false;

    public NameSendingServer() {
        running = true;
        try {
            FileInputStream nameInput = new FileInputStream(new File(System.getProperty("user.home") + "/Local Share/name.txt"));
            int ch;
            while ((ch = nameInput.read()) != -1)
                name += (char) ch;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.xml = createXML();
    }

    private String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else if (env.containsKey("HOSTNAME"))
            return env.get("HOSTNAME");
        else
            return "PC";
    }

    private String getImage() {
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        int imageId = random.nextInt(5) + 1;
        return "" + imageId + ".png";
    }

    public void run() {
        boolean timeout = false;
        try {
            try {
                serverSocket = new ServerSocket(5000);
                serverSocket.setSoTimeout(2000);
            } catch (BindException e) {
                System.out.println("NameSending server is already binded: " + e);
                //throw new IOException("Nothing to worry");
            }

            while (running) {
                //System.out.println("Waiting for new connection....");
                try {
                    timeout = false;
                    clientSocket = serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    timeout = true;
                }
                if (timeout == false) {
                    System.out.println("received connection from " + clientSocket.toString());
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    dataOutputStream.writeUTF(xml);
                }
            }
            System.out.println("Closing NameSendingServer: ");
            serverSocket.close();
        } catch (IOException ex) {
            System.out.println("Exception in NameSendingServer: " + ex);
        }
    }

    private String createXML() {
        Element classElement = new Element("receiver-details");
        Document document = new Document(classElement);
        Element details = new Element("details");
        Element name = new Element("name");
        name.setText(this.name);
        Element deviceName = new Element("device-name");
        deviceName.setText(getComputerName());
        Element image = new Element("image");
        image.setText(getImage());
        details.addContent(name);
        details.addContent(deviceName);
        details.addContent(image);
        document.getRootElement().setContent(details);
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        return outputter.outputString(document);
    }
}
package model;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import org.jdom2.*;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class SendFile extends Task<Long> {
    File file;
    String senderName = "Noobie";
    InetAddress address;
    Socket clientSocket;
    Label progressInfo;
    String details;

    public SendFile(File file, InetAddress address, Label progressInfo, String details) {
        this.progressInfo = progressInfo;
        this.details = details;
        this.file = file;
        this.address = address;
        FileInputStream nameStream = null;
        try {
            nameStream = new FileInputStream(new File(System.getProperty("user.home") + "/Local Share/name.txt"));
            Scanner scanner = new Scanner(nameStream);
            senderName = scanner.nextLine();
            nameStream.close();
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            clientSocket = new Socket(address, 5001);
        } catch (NoRouteToHostException ex) {
            Platform.runLater(() -> progressInfo.setText("Connection Interrupted"));
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected Long call() {
        long read = 0;
        try {
            int got;
            long fileSize;
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            FileInputStream fis = new FileInputStream(file);
            System.out.println(createXML());
            dos.writeUTF(createXML());
            fileSize = file.length();
            byte[] buffer = new byte[131072];

            while ((got = fis.read(buffer, 0, buffer.length)) > 0) {
                dos.write(buffer, 0, got);
                read += got;
                updateProgress(read, fileSize);
            }

            fis.close();
            dos.close();
            clientSocket.close();
        } catch (SocketException e) {
            details = details.replace("Sending To:", "File Dropped By:");
            Platform.runLater(() -> progressInfo.setText(details));
        } catch (IOException e) {
            System.out.println("IOException occured in SendFile: " + e);
        }
        //Sender.UpdateHash.setWait(false);
        details = details.replace("Sending To:", "Completed:");
        Platform.runLater(() -> progressInfo.setText(details));
        return read;
    }

    private String createXML() {
        Element classElement = new Element("file-details");
        Document document = new Document(classElement);
        Element details = new Element("details");
        Element senderNameElement = new Element("sender-name");
        senderNameElement.setText(senderName);
        Element fileNameElement = new Element("file-name");
        fileNameElement.setText(file.getName());
        Element fileSizeElement = new Element("file-size");
        fileSizeElement.setText("" + file.length());
        details.addContent(senderNameElement);
        details.addContent(fileNameElement);
        details.addContent(fileSizeElement);
        document.getRootElement().setContent(details);
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        return outputter.outputString(document);
    }
}


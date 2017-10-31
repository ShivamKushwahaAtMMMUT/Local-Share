package model;

import javafx.application.Platform;
import javafx.concurrent.Task;
import presenter.Presenter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

class SaveFile extends Task<Long> {
    Socket clientSocket;
    String userName;
    String fileName;
    String size;

    public SaveFile(Socket clientSocket) {
        this.clientSocket = clientSocket;
        System.out.println("A saveFile instantiated....................................................");
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, optionType);
        final JDialog dialog = pane.createDialog(parentComponent, title);
        dialog.setLocationRelativeTo(parentComponent);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setModal(true);
        dialog.setVisible(true);
        dialog.dispose();
        Object o = pane.getValue();
        if (o instanceof Integer) {
            return (Integer) o;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    @Override
    public Long call() {
        long progress = 0;
        try {
            System.out.println("call function is being called .......................................");
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            //Receiving the file fileName and the size in StringTokens
            String msgFromClient = dis.readUTF();

            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new ByteArrayInputStream(msgFromClient.getBytes()));
            Element classElement = document.getRootElement();
            Element details = classElement.getChild("details");
            userName = details.getChild("sender-name").getText();
            fileName = details.getChild("file-name").getText();
            size = details.getChild("file-size").getText();
            System.out.println("Username : -------------------------" + userName);
            System.out.println("File Name is: " + fileName);
            System.out.println("File Size is: " + size);

            int fileSize = Integer.parseInt(size);
            String message = "Sender: " + userName + "\nFile name: " + fileName + "\nSize: " + Math.round(fileSize / 1048576.0) + " Mb";
            System.out.println("Launching JOptionPane: --------------------------------------------------------- ");
            JDialog.setDefaultLookAndFeelDecorated(true);
            int reply = showConfirmDialog(null, message, "Incoming File", JOptionPane.YES_NO_OPTION);
            if (reply != JOptionPane.YES_OPTION) {
                dis.close();
                clientSocket.close();
                throw new IOException("File dropped");
            }

            String path = System.getProperty("user.home");
            File dir = new File(path + "/Downloads/Local Share");
            if (!dir.exists()) {
                dir.mkdir();
                System.out.println("Building the directory Local Share.");
            }
            File newFile = new File(path + "/Downloads/Local Share/" + fileName);
            if (newFile.exists()) {
                message = "File already exits. Do want to replace it?\n 'No' will Rename file and save it.";
                reply = showConfirmDialog(null, message, "Alert!!!", JOptionPane.YES_NO_OPTION);
                if (reply != JOptionPane.YES_OPTION) {
                    int count = 1;
                    while (true) {
                        newFile = new File(path + "/Downloads/Local Share/" + "(" + count + ")" + fileName);
                        if (newFile.exists()) {
                            count++;
                        } else
                            break;
                    }
                }
            }

            //OPening the fileInput Stream with the received fileName
            FileOutputStream fos = new FileOutputStream(newFile);
            //Buffer to receive file
            byte buffer[] = new byte[131072];
            long time = System.currentTimeMillis();
            int got = 0;
            int read = 0;
            int remaining = fileSize;
            while ((got = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                read += got;
                remaining -= got;
                System.out.print("\rgot " + (((float) read / fileSize) * 100) + " percent.");
                fos.write(buffer, 0, got);
                progress = read;
                updateProgress(progress, fileSize);
            }
            long ntime = System.currentTimeMillis();
            System.out.println("Time Taken: " + ((ntime - time) / 60000.0) + " min");
            fos.close();
            dis.close();
            clientSocket.close();
        } catch (IOException e) {
            updateProgress(1, 1);
            Presenter.progressIndicatorList.remove(Presenter.progressIndicatorList.size() - 1);
            Platform.runLater(() -> Presenter.updateProgressIndicators());
            JOptionPane.showMessageDialog(null, "FAILED TO RECEIVE: " +"\nFile: " + fileName + "\nFrom: " + userName);
            System.out.println("IOException occured in SaveFile class: " + e.getMessage());
        } catch (JDOMException ex) {
            ex.printStackTrace();
        }
        Platform.runLater(() -> Presenter.updateProgressIndicators());
        return progress;
    }
}
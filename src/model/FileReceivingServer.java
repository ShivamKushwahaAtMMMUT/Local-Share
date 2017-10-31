package model;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;
import presenter.Presenter;

import java.io.*;
import java.net.*;

public class FileReceivingServer implements Runnable {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    public static boolean running = true;

    public FileReceivingServer(int port) {
        running = true;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(2000);
        } catch (BindException e) {
            System.out.println("Bind Exception for FileSendingServer: ");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        boolean timeout = false;
        while (running) {
            try {
                ProgressIndicator progressIndicator = new ProgressIndicator();
                try{
                    timeout = false;
                    clientSocket = serverSocket.accept();
                } catch(SocketTimeoutException e){
                    timeout = true;
                }
                if( timeout == false){
                    Task<Long> saveFileTask = new SaveFile(clientSocket);
                    Thread saveFileThread = new Thread(saveFileTask);
                    saveFileThread.setDaemon(true);
                    saveFileThread.setPriority(Thread.MAX_PRIORITY);
                    progressIndicator.progressProperty().bind(saveFileTask.progressProperty());
                    saveFileThread.start();
                    Platform.runLater(() -> {
                        Presenter.addProgressIndicator(progressIndicator);
                        Presenter.updateProgressIndicators();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            System.out.println("Closing FileReceivingServer: ");
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
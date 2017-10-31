package view;

import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import model.Sender;
import model.SendFile;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class View extends BorderPane {
    public static List<StackPane> progressBarList = new ArrayList<>();
    ScrollPane scrollPane = new ScrollPane();
    File file = null;
    Node root;
    Scene scene;
    static String[] colors = {"#2196f3", "#d500f9", "#f50057", "#76ff03", "#84ffff", "#004d40", "#cddc39", "#ff5722", "#616161", "#ffff00", "#2196f3", "#d500f9", "#f50057", "#76ff03", "#84ffff", "#004d40", "#cddc39", "#ff5722", "#616161", "#ffff00"};
    static int colorCount;
    Button progressBtnGlobal;

    //Create footer view.
    Label footerLbl = new Label("\u00a9 Prominent Group");

    public View(File newFile, Scene scene, Node root) {
        this.scene = scene;
        this.root = root;
        file = newFile;
        createLayout();
    }

    private void createLayout() {
        //Create header view.
        //Exit Button
        Circle closeCircle = new Circle();
        Circle closeCircleOuter = new Circle();
        closeCircle.setId("closeCircle");
        closeCircleOuter.setId("closeCircleOuter");
        ImagePattern closeBtnImagePattern = new ImagePattern(new Image("resources/icons/closeBtn.png"), 0, 0, 1, 1, true);
        closeCircle.setFill(closeBtnImagePattern);
        closeCircle.radiusProperty().set(15);
        closeCircleOuter.radiusProperty().set(25);
        StackPane closeContainer = new StackPane(closeCircleOuter, closeCircle);
        //Logout Button
        Circle logoutCircle = new Circle();
        Circle logoutCircleOuter = new Circle();
        logoutCircle.setId("logoutCircle");
        logoutCircleOuter.setId("logoutCircleOuter");
        ImagePattern logoutBtnImagePattern = new ImagePattern(new Image("resources/icons/logoutBtn.png"), 0, 0, 1, 1, true);
        logoutCircle.setFill(logoutBtnImagePattern);
        logoutCircle.radiusProperty().set(15);
        logoutCircleOuter.radiusProperty().set(25);
        //Add event Handler for logout Button.
        EventHandler logoutEventHandler = e -> {
            scene.setRoot((Parent) root);
            Sender.UpdateHash.setWait(false);
            progressBtnGlobal.setText("Progress");
            progressBtnGlobal.setUserData(0);
        };
        logoutCircle.addEventHandler(MouseEvent.MOUSE_CLICKED, logoutEventHandler);
        StackPane logoutContainer = new StackPane(logoutCircleOuter, logoutCircle);
        //Create Choose Button
        Button chooseBtn = new Button("Choose\n   File");
        chooseBtn.setTextFill(Paint.valueOf("#e8eaf6"));
        chooseBtn.setMinHeight(100);
        chooseBtn.setMinWidth(100);
        chooseBtn.setStyle("-fx-background-color: #03a9f4;" + "-fx-background-radius: 2000px;");
        Circle bottomCircle = new Circle();
        bottomCircle.setRadius(43.333);
        bottomCircle.setStyle("-fx-fill: #6200ea;" + "-fx-stroke: #29b6f6;" + "-fx-stroke-width: 5px;");
        Circle middleCircle = new Circle();
        middleCircle.setRadius(33.333);
        middleCircle.setFill(Paint.valueOf("#76ff03"));
        StackPane container = new StackPane();
        container.getChildren().addAll(bottomCircle, middleCircle, chooseBtn);
        //Add event handler for close Button.
        EventHandler closeEventHandler = e -> Platform.exit();
        closeCircle.addEventHandler(MouseEvent.MOUSE_CLICKED, closeEventHandler);
        //Create Header Container.
        HBox headerLayout = new HBox(241, logoutContainer, container, closeContainer);

        //Create Center view
        Button sendToAndroidBtn = new Button("Send to Android");
        //sendToAndroidBtn.setStyle("-fx-font-size: 14px;");
        //Handle event for send to Android button
        sendToAndroidBtn.setOnAction(e -> {
            if (file != null) {
                String addressString = "192.168.43.1";
                try {
                    InetAddress hotspotAddress = InetAddress.getByName(addressString);
                    Socket clientSocket = new Socket(hotspotAddress, 5000);
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

                    updateProgressBarList(name, deviceName, hotspotAddress);
                    showProgresses();
                    progressBtnGlobal.setText("Users       ");
                    progressBtnGlobal.setUserData(1);
                    System.out.println("Sending file: " + file.getName());
                } catch (UnknownHostException e1) {
                    footerLbl.setText("Mobile may be using different subnet");
                } catch (IOException ex) {
                    footerLbl.setText("Connect PC to receiver's hotspot");
                } catch (JDOMException e1) {
                    e1.printStackTrace();
                }
            } else {
                footerLbl.setText("Select a file to send");
            }
        });
        footerLbl.setStyle("-fx-underline: false;");
        TextField ipFld = new TextField();
        ipFld.setPromptText("Valid IPv4 address");
        ipFld.setVisible(false);
        Button sendBtn = new Button("       Send       ");
        sendBtn.setVisible(false);
        Button sendViaIpButton = new Button("Send Using IP");
        //sendViaIpButton.setStyle("-fx-font-size: 16px;");
        sendViaIpButton.setOnAction(e -> {
            sendViaIpButton.setVisible(false);
            sendViaIpButton.setDisable(true);
            sendViaIpButton.setMouseTransparent(true);
            ipFld.setVisible(true);
            sendBtn.setVisible(true);
            ipFld.requestFocus();
        });
        sendBtn.setOnAction(e -> {
            if (file != null) {
                if (ipFld.getText().trim().length() >= 7) {
                    try {
                        InetAddress inetAddress = InetAddress.getByName(ipFld.getText());
                        if (!inetAddress.isSiteLocalAddress()) {
                            throw new UnknownHostException("Entered IP address is not valid.");
                        }
                        Socket clientSocket = new Socket();
                        clientSocket.connect(new InetSocketAddress(inetAddress, 5000), 2500);
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

                        updateProgressBarList(name, deviceName, inetAddress);
                        showProgresses();
                        progressBtnGlobal.setText("Users       ");
                        progressBtnGlobal.setUserData(1);
                        System.out.println("Sending file: " + file.getName());
                    } catch (UnknownHostException e1) {
                        footerLbl.setText("Please Enter valid IPv4 address.");
                    } catch (IOException ex) {
                        footerLbl.setText("User is offline.");
                    } catch (JDOMException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    footerLbl.setText("Invalid IPv4 Address.");
                }
            } else {
                footerLbl.setText("Select a file to send.");
            }
        });
        StackPane buttonPane = new StackPane(sendBtn, sendViaIpButton);
        HBox sendViaIpBox = new HBox(ipFld, buttonPane);
        HBox ipBox = new HBox(70, sendToAndroidBtn, sendViaIpBox);
        scrollPane.setContent(new GridPane());
        scrollPane.setMinViewportHeight(280);
        scrollPane.setMinViewportWidth(668);
        scrollPane.setMaxHeight(280);
        scrollPane.setMaxWidth(668);
        Label notificationLbl = new Label("Updating receiver's list. \nPlease make sure that \nSender and Receiver are on same \nLocal area network.");
        VBox centerLayout = new VBox(10, ipBox, scrollPane);

        //Create Footer
        Button progressBtn = new Button("Progress");
        HBox progressBtnBox = new HBox(progressBtn);
        progressBtn.setUserData(0);
        progressBtnBox.setAlignment(Pos.CENTER_RIGHT);
        progressBtnGlobal = progressBtn;
        HBox.setHgrow(progressBtnBox, Priority.ALWAYS);
        HBox bottomBox = new HBox(footerLbl, progressBtnBox);
        bottomBox.setPadding(new Insets(3));
        progressBtn.setOnAction(e -> {
            if ((Integer) progressBtn.getUserData() == 0) {
                Sender.UpdateHash.setWait(true);
                progressBtn.setText("Users       ");
                progressBtn.setUserData(1);
                showProgresses();
            } else {
                Sender.UpdateHash.setWait(false);
                progressBtn.setText("Progress");
                progressBtn.setUserData(0);
            }
        });

        //Add Event Handler for Choose Button.
        chooseBtn.setOnAction(e -> {
            ScaleTransition transition1 = new ScaleTransition(Duration.seconds(0.20), chooseBtn);
            transition1.setToX(0.6);
            transition1.setToY(0.6);
            ScaleTransition transition2 = new ScaleTransition(Duration.seconds(0.20), middleCircle);
            transition2.setToX(1.5);
            transition2.setToY(1.5);
            SequentialTransition sTransition = new SequentialTransition(transition1, transition2);
            sTransition.setAutoReverse(true);
            sTransition.setCycleCount(2);
            sTransition.play();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a file to send");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));
            File selectedFile = fileChooser.showOpenDialog(new Stage());
            if (selectedFile != null) {
                this.file = selectedFile;
                if (file.getName().length() > 36)
                    footerLbl.setText("Selected file: " + file.getName().substring(0, 36) + "...");
                else
                    footerLbl.setText("Selected file: " + file.getName());

            }
        });

        this.setTop(headerLayout);
        this.setCenter(centerLayout);
        this.setBottom(bottomBox);
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public Node createUserIcon(String name, String computerName, InetAddress ip, String image) {
        //Create a task for Sending file.
        Label nameLbl = new Label(name + " [" + computerName + "]");
        nameLbl.setMouseTransparent(true);
        nameLbl.setStyle("-fx-text-fill: #212121;" + "-fx-font-size: 30px;" + "-fx-font-weight: bold;");
        ImageView icon = new ImageView(new Image("resources/faces/" + image));
        icon.fitWidthProperty().set(50);
        icon.fitHeightProperty().set(50);
        icon.setMouseTransparent(true);
        HBox container = new HBox(10, icon, nameLbl);
        container.setStyle("-fx-background-color: #b3e5fc;" + "-fx-background-radius: 5px;" + "-fx-border-color: #6200ea;" + "-fx-border-width: 2px;" + "-fx-border-radius: 5px;");
        container.setMinSize(667, 55);
        container.setMaxSize(667, 55);
        container.setUserData(ip);
        container.setOnMouseClicked(e -> {
            if (file != null) {
                progressBtnGlobal.setText("Users       ");
                progressBtnGlobal.setUserData(1);
                updateProgressBarList(name, computerName, (InetAddress) container.getUserData());
                showProgresses();
                System.out.println("Sending file: " + file.getName());
            } else {
                footerLbl.setText("Select a file to send.");
            }
        });
        return container;
    }

    public StackPane getProgressContainer(String name, String computerName, InetAddress inetAddress) {
        StackPane progressContainer = new StackPane();
        progressContainer.setMinSize(667, 55);
        progressContainer.setMaxSize(667, 55);
        String details = "Sending To: " + name + " [" + computerName + "]\nFile: " + file.getName();
        Label progressInfo = new Label(details);
        progressInfo.setMouseTransparent(true);
        progressInfo.setStyle("-fx-text-fill: #76ff03;" + "-fx-underline: false;");
        progressInfo.setMinSize(667, 55);
        progressInfo.setMaxSize(667, 55);
        ProgressBar fileProgress = new ProgressBar();
        fileProgress.setMouseTransparent(true);
        fileProgress.setMinSize(667, 60);
        fileProgress.setMaxSize(667, 60);
        Task<Long> sendTask = new SendFile(file, inetAddress, progressInfo, details);
        Thread sendThread = new Thread(sendTask);
        sendThread.setDaemon(true);
        sendThread.start();
        //Make update hash wait
        Sender.UpdateHash.setWait(true);
        fileProgress.progressProperty().bind(sendTask.progressProperty());
        progressContainer.getChildren().addAll(fileProgress, progressInfo);
        return progressContainer;
    }

    public void updateProgressBarList(String name, String computerName, InetAddress inetAddress) {
        StackPane progressContainer = getProgressContainer(name, computerName, inetAddress);
        progressBarList.add(progressContainer);
    }

    public void showProgresses() {
        VBox progressBarBox = new VBox(10);
        progressBarList.forEach((StackPane progressContainer) -> {
            progressBarBox.getChildren().add(progressContainer);
        });
        scrollPane.setContent(progressBarBox);
    }
}

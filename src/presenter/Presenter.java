package presenter;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.BroadcastingServer;
import model.FileReceivingServer;
import model.NameSendingServer;
import model.Sender;
import view.View;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Presenter extends Application {
    public static List<ProgressIndicator> progressIndicatorList = new ArrayList<>();
    static Pane receiverRoot = new Pane();
    static String[] colors = {"#2196f3", "#d500f9", "#f50057", "#76ff03", "#84ffff", "#004d40", "#cddc39", "#ff5722", "#616161", "#ffff00", "#2196f3", "#d500f9", "#f50057", "#76ff03", "#84ffff", "#004d40", "#cddc39", "#ff5722", "#616161", "#ffff00"};
    Stage stage;
    double dragOffsetX;
    double dragOffsetY;
    //String name;
    //String image;
    File file = null;
    View view;
    Pane root = new Pane();
    Scene scene = new Scene(root, 700, 525);
    Label headerLabel = new Label("Local Share");

    public static void main(String[] args) {
        Application.launch(args);
    }

    public static void updateProgressIndicators() {
        StackPane container = new StackPane();
        container.layoutXProperty().bind(receiverRoot.widthProperty().divide(2).subtract(173));
        container.layoutYProperty().bind(receiverRoot.heightProperty().divide(2).subtract(180));
        final int[] count = new int[1];
        count[0] = 0;
        progressIndicatorList.forEach((ProgressIndicator indicator) -> {
            if (indicator.getProgress() < 1.0) {
                indicator.setMinSize(380 - (count[0] * 60), 380 - (count[0] * 60));
                indicator.setMaxSize(380 - (count[0] * 60), 380 - (count[0] * 60));
                indicator.setStyle("-fx-progress-color: " + colors[count[0] % 20] + ";");
                container.getChildren().add(indicator);
                count[0]++;
            }
        });
        receiverRoot.getChildren().remove(3);
        receiverRoot.getChildren().add(container);
    }

    public static void addProgressIndicator(ProgressIndicator indicator) {
        progressIndicatorList.add(indicator);
    }

    @Override
    public void init() {
        headerLabel.setStyle("-fx-underline: false;" + "-fx-font-size: 30px;" + "-fx-font-style: italic;");
        headerLabel.setFont(Font.font("Arial Black"));
        File initialDirectories = new File(System.getProperty("user.home") + "/Local Share");
        if (!initialDirectories.exists())
            initialDirectories.mkdir();
        createReceiverLayout();
    }

    @Override
    public void start(Stage primaryStage) throws NullPointerException {

        stage = primaryStage;
        view = new View(file, scene, root);
        Label footerLbl = new Label("\u00a9 Prominent Group");
        footerLbl.setOnMouseClicked(event -> getHostServices().showDocument("http://localshare.github.io"));
        footerLbl.setCursor(Cursor.HAND);
        footerLbl.setStyle("-fx-text-fill: #76ff03;");
        footerLbl.setLayoutX(10);
        footerLbl.setLayoutY(480);
        Label warningLbl = new Label("File is saved to Local Share in Downloads");
        warningLbl.setStyle("-fx-text-fill: #ff6f00;");
        warningLbl.setLayoutX(290);
        warningLbl.setLayoutY(480);

        //Create Box for Sending name confirmation
        HBox sendBox = new HBox(15);
        Label sendNameLbl = new Label("Name:");
        sendNameLbl.setMinSize(60, 50);
        TextField sendingNameFld = new TextField();
        sendingNameFld.setMinSize(280, 50);
        sendingNameFld.setPromptText("Min 4 chars long");
        Button confirmSendBtn = new Button("OK");
        confirmSendBtn.setMinSize(60, 60);
        sendBox.getChildren().addAll(sendNameLbl, sendingNameFld, confirmSendBtn);
        sendBox.setLayoutX(120);
        sendBox.setLayoutY(200);
        sendNameLbl.setVisible(false);
        sendingNameFld.setVisible(false);
        confirmSendBtn.setVisible(false);

        //Handle event for Confirm send Button
        confirmSendBtn.setOnAction(e -> {
            try {
                //String localIpAddress = InetAddress.getLocalHost().getHostAddress();
                String localIpAddress = getIP();
                if (localIpAddress.equals("127.0.0.1") || localIpAddress.equals("127.0.0.2")) {
                    warningLbl.setText("PC is not connected to any network");
                } else {
                    String name = sendingNameFld.getText().trim();
                    if (name.length() >= 4 && name.length() <= 20) {
                        //Write new name to file
                        File nameFile = new File(System.getProperty("user.home") + "/Local Share/name.txt");
                        try {
                            FileOutputStream out = new FileOutputStream(nameFile);
                            out.write(name.getBytes());
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                        Thread senderThread = new Thread(new Sender(view));
                        senderThread.setDaemon(true);
                        senderThread.start();
                        scene.setRoot(view);
                    } else {
                        sendingNameFld.setText(name);
                        warningLbl.setText("Name must be 4 to 20 characters long");
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        });

        //Create send Button
        Button sendBtn = new Button("Send");
        sendBtn.setTextFill(Paint.valueOf("#e8eaf6"));
        sendBtn.setMinHeight(130);
        sendBtn.setMinWidth(130);
        sendBtn.setStyle("-fx-background-color: #03a9f4;" + "-fx-background-radius: 2000px;");
        Circle sendBottomCircle = new Circle();
        sendBottomCircle.setRadius(60);
        sendBottomCircle.setStyle("-fx-fill: #6200ea;" + "-fx-stroke: #29b6f6;" + "-fx-stroke-width: 5px;");
        Circle sendMiddleCircle = new Circle();
        sendMiddleCircle.setRadius(42);
        sendMiddleCircle.setFill(Paint.valueOf("#76ff03"));
        StackPane sendContainer = new StackPane();
        sendContainer.getChildren().addAll(sendBottomCircle, sendMiddleCircle, sendBtn);
        sendContainer.layoutXProperty().set(280);
        sendContainer.layoutYProperty().set(60);

        //Create Box for Receiving name confirmation
        HBox receiveBox = new HBox(15);
        Label receiveNameLbl = new Label("Name:");
        receiveNameLbl.setMinSize(60, 50);
        TextField receivingNameFld = new TextField();
        receivingNameFld.setMinSize(280, 50);
        receivingNameFld.setPromptText("Min 4 chars long");
        Button confirmReceiveBtn = new Button("OK");
        confirmReceiveBtn.setMinSize(60, 60);
        receiveBox.getChildren().addAll(receiveNameLbl, receivingNameFld, confirmReceiveBtn);
        receiveBox.setLayoutX(120);
        receiveBox.setLayoutY(405);
        receiveNameLbl.setVisible(false);
        receivingNameFld.setVisible(false);
        confirmReceiveBtn.setVisible(false);

        //Handle event for confirm Receive Button
        confirmReceiveBtn.setOnAction(e -> {
            try {
                //String localIpAddress = InetAddress.getLocalHost().getHostAddress();
                String localIpAddress = getIP();
                if (localIpAddress.equals("127.0.0.1") || localIpAddress.equals("127.0.0.2")) {
                    warningLbl.setText("PC is not connected to any network");
                } else {
                    String name = receivingNameFld.getText().trim();
                    if (name.length() >= 4 && name.length() <= 20) {
                        //Write new name to file
                        File nameFile = new File(System.getProperty("user.home") + "/Local Share/name.txt");
                        try {
                            FileOutputStream out = new FileOutputStream(nameFile);
                            out.write(name.getBytes());
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        scene.setRoot(receiverRoot);

                        //Start Receiver ThreadPool
                        Thread nameSendingServer = new Thread(new NameSendingServer());
                        Thread fileReceivingServer = new Thread(new FileReceivingServer(5001));
                        Thread broadcastingServer = new Thread(new BroadcastingServer());
                        broadcastingServer.setDaemon(true);
                        fileReceivingServer.setDaemon(true);
                        nameSendingServer.setDaemon(true);
                        broadcastingServer.start();
                        nameSendingServer.start();
                        fileReceivingServer.start();
                    } else {
                        receivingNameFld.setText(name);
                        warningLbl.setText("Name must be 4 to 20 characters long");
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        //Create receive button
        Button recieveBtn = new Button("Receive");
        recieveBtn.setTextFill(Paint.valueOf("#e8eaf6"));
        recieveBtn.setMinHeight(130);
        recieveBtn.setMinWidth(130);
        recieveBtn.setStyle("-fx-background-color: #03a9f4;" + "-fx-background-radius: 2000px;");
        Circle receiveBottomCircle = new Circle();
        receiveBottomCircle.setRadius(60);
        receiveBottomCircle.setStyle("-fx-fill: #6200ea;" + "-fx-stroke: #29b6f6;" + "-fx-stroke-width: 5px;");
        Circle receiveMiddleCircle = new Circle();
        receiveMiddleCircle.setRadius(42);
        receiveMiddleCircle.setFill(Paint.valueOf("#76ff03"));
        StackPane revceiveContainer = new StackPane();
        revceiveContainer.getChildren().addAll(receiveBottomCircle, receiveMiddleCircle, recieveBtn);
        revceiveContainer.layoutXProperty().set(280);
        revceiveContainer.layoutYProperty().set(270);

        //Handle event for send button
        sendBtn.setOnAction(e -> {
            //Animate Button
            ScaleTransition transition1 = new ScaleTransition(Duration.seconds(0.20), sendBtn);
            transition1.setToX(0.6);
            transition1.setToY(0.6);
            ScaleTransition transition2 = new ScaleTransition(Duration.seconds(0.20), sendMiddleCircle);
            transition2.setToX(1.5);
            transition2.setToY(1.5);
            SequentialTransition sTransition = new SequentialTransition(transition1, transition2);
            sTransition.setAutoReverse(true);
            sTransition.setCycleCount(2);
            sTransition.play();

            receiveNameLbl.setVisible(false);
            receivingNameFld.setVisible(false);
            confirmReceiveBtn.setVisible(false);

            sendNameLbl.setVisible(true);
            sendingNameFld.setVisible(true);
            confirmSendBtn.setVisible(true);

            recieveBtn.setMouseTransparent(false);
            sendBtn.setMouseTransparent(true);

            //Check for Name file
            File nameFile = new File(System.getProperty("user.home") + "/Local Share/name.txt");
            if (!nameFile.exists()) {
                try {
                    nameFile.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                FileInputStream raw = new FileInputStream(nameFile);
                Reader nameReader = new InputStreamReader(raw);
                String name = "";
                int ch;
                while ((ch = nameReader.read()) != -1) {
                    name = name + (char) ch;
                }
                sendingNameFld.setText(name);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Handle event for Receive Button
        recieveBtn.setOnAction(e -> {
            //Animate Button
            ScaleTransition transition1 = new ScaleTransition(Duration.seconds(0.20), recieveBtn);
            transition1.setToX(0.6);
            transition1.setToY(0.6);
            ScaleTransition transition2 = new ScaleTransition(Duration.seconds(0.20), receiveMiddleCircle);
            transition2.setToX(1.5);
            transition2.setToY(1.5);
            SequentialTransition sTransition = new SequentialTransition(transition1, transition2);
            sTransition.setAutoReverse(true);
            sTransition.setCycleCount(2);
            sTransition.play();

            sendNameLbl.setVisible(false);
            sendingNameFld.setVisible(false);
            confirmSendBtn.setVisible(false);

            receiveNameLbl.setVisible(true);
            receivingNameFld.setVisible(true);
            confirmReceiveBtn.setVisible(true);

            sendBtn.setMouseTransparent(false);
            recieveBtn.setMouseTransparent(true);

            try {
                //String localIpAddress = InetAddress.getLocalHost().getHostAddress();
                String localIpAddress = getIP();
                warningLbl.setText("Sender can send using IP: " + localIpAddress);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //Create new Name file if does not exist
            File nameFile = new File(System.getProperty("user.home") + "/Local Share/name.txt");
            if (!nameFile.exists()) {
                try {
                    nameFile.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            //Check for Name file
            try {
                FileInputStream raw = new FileInputStream(nameFile);
                Reader nameReader = new InputStreamReader(raw);
                String name = "";
                int ch;
                while ((ch = nameReader.read()) != -1) {
                    name = name + (char) ch;
                }
                receivingNameFld.setText(name);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file to send.");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("All Files", "*.*"));

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
        closeContainer.layoutXProperty().bind(scene.widthProperty().subtract(50));

        //Logout Button
        Circle logoutCircle = new Circle();
        Circle logoutCircleOuter = new Circle();
        logoutCircle.setId("logoutCircle");
        logoutCircleOuter.setId("logoutCircleOuter");
        ImagePattern logoutBtnImagePattern = new ImagePattern(new Image("resources/icons/logoutBtn.png"), 0, 0, 1, 1, true);
        logoutCircle.setFill(logoutBtnImagePattern);
        logoutCircle.radiusProperty().set(15);
        logoutCircleOuter.radiusProperty().set(25);
        StackPane logoutContainer = new StackPane(logoutCircleOuter, logoutCircle);

        //Layout the Nodes in scene
        headerLabel.layoutXProperty().set(280);
        headerLabel.layoutYProperty().set(15);

        //Handle event for close Button.
        EventHandler closeBtnEvent = e -> Platform.exit();
        closeCircle.addEventHandler(MouseEvent.MOUSE_CLICKED, closeBtnEvent);

        root.getChildren().addAll(logoutContainer, headerLabel, closeContainer, sendContainer, sendBox, revceiveContainer, receiveBox, footerLbl, warningLbl);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Local Share");
        primaryStage.getIcons().add(new Image("resources/icons/Local Share Logo.png"));
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.show();

        scene.setOnMousePressed(e -> handleMousePressed(e));
        scene.setOnMouseDragged(e -> handleMouseDragged(e));
    }

    private void handleMousePressed(MouseEvent e) {
        this.dragOffsetX = e.getScreenX() - stage.getX();
        this.dragOffsetY = e.getScreenY() - stage.getY();
    }

    private void handleMouseDragged(MouseEvent e) {
        stage.setX(e.getScreenX() - this.dragOffsetX);
        stage.setY(e.getScreenY() - this.dragOffsetY);
    }

    public void createReceiverLayout() {
        receiverRoot.setStyle("-fx-background-image: url(\"resources/images/ReceiverBackground.jpg\");" + "-fx-background-position: center;" + "-fx-background-size: cover;");
        //Generate Layout for Receiver
        Circle circle1 = new Circle();
        circle1.setId("circle");
        circle1.setRadius(70);
        circle1.setFill(Paint.valueOf("transparent"));
        Circle circle2 = new Circle();
        circle2.setId("circle");
        circle2.setRadius(40);
        circle2.setFill(Paint.valueOf("transparent"));
        Circle circle3 = new Circle();
        circle3.setId("circle");
        circle3.setRadius(20);
        circle3.setFill(Paint.valueOf("transparent"));
        Circle circle4 = new Circle();
        circle4.setId("circle");
        circle4.setRadius(70);
        circle4.setFill(new ImagePattern(new Image("resources/icons/antenna.png"), 0, 0, 1, 1, true));
        StackPane container = new StackPane();
        container.layoutXProperty().bind(receiverRoot.widthProperty().divide(2).subtract(53));
        container.layoutYProperty().bind(receiverRoot.heightProperty().divide(2).subtract(70));
        container.getChildren().addAll(circle1, circle2, circle3, circle4);

        //Animate Receiver
        ScaleTransition circle1Transition1 = new ScaleTransition(Duration.seconds(3), circle1);
        ScaleTransition circle2Transition1 = new ScaleTransition(Duration.seconds(3), circle2);
        ScaleTransition circle3Transition1 = new ScaleTransition(Duration.seconds(3), circle3);
        circle1Transition1.setAutoReverse(true);
        circle1Transition1.setCycleCount(2);
        circle2Transition1.setAutoReverse(true);
        circle2Transition1.setCycleCount(2);
        circle3Transition1.setAutoReverse(true);
        circle3Transition1.setCycleCount(2);
        circle1Transition1.setByX(2.2);
        circle1Transition1.setByY(2.2);
        circle2Transition1.setByX(4.0);
        circle2Transition1.setByY(4.0);
        circle3Transition1.setByX(7);
        circle3Transition1.setByY(7);
        FadeTransition circle1Transition2 = new FadeTransition(Duration.seconds(3), circle1);
        FadeTransition circle2Transition2 = new FadeTransition(Duration.seconds(3), circle2);
        FadeTransition circle3Transition2 = new FadeTransition(Duration.seconds(3), circle3);
        circle1Transition2.setFromValue(1.0);
        circle1Transition2.setToValue(0.0);
        circle2Transition2.setFromValue(1.0);
        circle2Transition2.setToValue(0.0);
        circle3Transition2.setFromValue(1.0);
        circle3Transition2.setToValue(0.0);
        ParallelTransition transition = new ParallelTransition(circle1Transition1, circle2Transition1, circle3Transition1, circle1Transition2, circle2Transition2, circle3Transition2);
        transition.setCycleCount(Timeline.INDEFINITE);
        transition.play();

        receiverRoot.getChildren().add(container);

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
        closeContainer.layoutXProperty().bind(scene.widthProperty().subtract(50));

        //Logout Button
        Circle logoutCircle = new Circle();
        Circle logoutCircleOuter = new Circle();
        logoutCircle.setId("logoutCircle");
        logoutCircleOuter.setId("logoutCircleOuter");
        ImagePattern logoutBtnImagePattern = new ImagePattern(new Image("resources/icons/logoutBtn.png"), 0, 0, 1, 1, true);
        logoutCircle.setFill(logoutBtnImagePattern);
        logoutCircle.radiusProperty().set(15);
        logoutCircleOuter.radiusProperty().set(25);
        StackPane logoutContainer = new StackPane(logoutCircleOuter, logoutCircle);

        //Layout the Nodes in scene
        headerLabel.layoutXProperty().set(280);
        headerLabel.layoutYProperty().set(15);

        //Handle event for logout button.
        EventHandler logoutBtnEvent = logoutEvent -> {
            scene.setRoot(root);
            BroadcastingServer.running = false;
            NameSendingServer.running = false;
            FileReceivingServer.running = false;
        };
        logoutCircle.addEventHandler(MouseEvent.MOUSE_CLICKED, logoutBtnEvent);

        //Handle event for close Button.
        EventHandler closeBtnEvent = closeEvent -> Platform.exit();
        closeCircle.addEventHandler(MouseEvent.MOUSE_CLICKED, closeBtnEvent);
        receiverRoot.getChildren().addAll(logoutContainer, closeContainer, new StackPane());

        scene.getStylesheets().add("resources/stylesheet/sceneStyler.css");
    }

    public String getIP() {

        try {
            Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface inet = (NetworkInterface) networkInterfaces.nextElement();
                Enumeration address = inet.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress) address.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
        return "127.0.0.1";
    }

}

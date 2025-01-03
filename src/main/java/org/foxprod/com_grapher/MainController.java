package org.foxprod.com_grapher;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.io.InputStream;

public class MainController {
    public TextArea mainTextArea;
    public Button startButton;
    public ComboBox portsDropdown;
    public Button stopButton;
    private int speed,
                highByte,
                lowByte;
    private SerialPort portIn;
    @FXML
    public void initialize() {
        getPorts();
    }

    private void getPorts(){
        ObservableList<String> portsList = FXCollections.observableArrayList();

        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            String path = port.getSystemPortPath();
            portsList.add(path.substring(4));
        }

        portsDropdown.setItems(portsList);
        portsDropdown.setValue(portsList.getFirst());
    }

    @FXML
    private void onStartPressed(){
        stopButton.setDisable(false);
        startButton.setDisable(true);
        portsDropdown.setDisable(true);
        mainTextArea.clear();

        portIn = SerialPort.getCommPort(portsDropdown.getItems().getFirst().toString());
        portIn.setBaudRate(115200);

        portIn.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        if (portIn.openPort()){
            mainTextArea.appendText("Port opened successfully\n");
            startButton.setDisable(true);
        }
        else {
            mainTextArea.appendText("ERROR: PORT NOT OPENED!");
            return;
        }

        InputStream in = portIn.getInputStream();

//        new Thread(() -> {
//            try {
//                while (true) {
//                    int data = in.read();
//                    String text = String.valueOf((char) data);
//                    Platform.runLater(() -> mainTextArea.appendText(text));
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    public void onStopPressed(ActionEvent actionEvent) {
        stopButton.setDisable(true);
        startButton.setDisable(false);
        portsDropdown.setDisable(false);

        if (portIn.closePort()){
            mainTextArea.appendText("Port closed successfully\n");
        }
        else {
            mainTextArea.appendText("ERROR: PORT NOT CLOSED!\n");
        }
    }
}
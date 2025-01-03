package org.foxprod.com_grapher;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.io.InputStream;

public class MainController {
    public TextArea mainTextArea;
    public Button startButton;
    public ComboBox portsDropdown;
    public Button stopButton;
    public TextField speedField;
    private int speed,
                highByte,
                lowByte;

    private SerialPort portIn;

    public volatile boolean readPort = true;
    public volatile boolean error = false;

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
        readPort = true;
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

        new Thread(() -> {
            try {
                while (readPort) {
                    int data = in.read();
                    if (data == 255) {
                        data = in.read();
                        if (data == 79){
                            data = in.read();
                            if (data == 83){speedField.setText("SLOW");}
                            else if (data == 70){speedField.setText("FAST");}
                            else {error = true;}
                            if (!error){
                                data = in.read();
                                mainTextArea.appendText(data + ".");
                                data = in.read();
                                mainTextArea.appendText(data + "\n");
                            }
                        }
                        else {error = true;}
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onStopPressed(ActionEvent actionEvent) {
        readPort = false;
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
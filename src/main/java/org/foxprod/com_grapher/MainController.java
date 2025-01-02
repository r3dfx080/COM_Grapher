package org.foxprod.com_grapher;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.io.InputStream;

public class MainController {
    public TextArea mainTextArea;
    public Button startButton;
    private int speed,
                highByte,
                lowByte;
    @FXML
    private void onStartPressed() throws IOException {
        mainTextArea.clear();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            System.out.println(port.getDescriptivePortName());
        }
        SerialPort portIn = SerialPort.getCommPort("COM4");
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
                while (true) {
                    int data = in.read();
                    String text = String.valueOf((char) data);
                    Platform.runLater(() -> mainTextArea.appendText(text));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
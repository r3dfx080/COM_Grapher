package org.foxprod.com_grapher;

import com.fazecast.jSerialComm.SerialPort;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainController {
    public TextArea mainTextArea;
    public Button startButton;
    public ComboBox<String> portsDropdown;
    public Button stopButton;
    public TextField speedField;
    public NumberAxis chartNumber;
    public CategoryAxis chartCategory;
    public BarChart<String, Number> chart;
    public Label readingLabel;
    public Label speedLabel;
    public Label dBLabel;

    private SerialPort portIn;
    private Thread readerThread;

    public static volatile boolean readPort = true;
    public volatile boolean error = false;


    @FXML
    public void initialize() {
        // gathering data on available ports, reported by OS
        getPorts();
        dBLabel.setVisible(false);
        chart.getYAxis().setLabel("dBA");
        chart.getXAxis().setLabel("Seconds");
    }

    public static class ComReader implements Runnable {
        private final SerialPort portIn;
        private final TextArea mainTextArea;
        private final TextField speedField;
        private final BarChart<String, Number> chart;
        private final Deque<Float> readingsQueue = new ArrayDeque<>(20);
        private final Label readingLabel;
        private final Label speedLabel;

        public ComReader(SerialPort portIn, TextArea mainTextArea, TextField speedField, BarChart<String, Number> chart, Label readingLabel, Label speedLabel) {
            this.portIn = portIn;
            this.mainTextArea = mainTextArea;
            this.speedField = speedField;
            this.chart = chart;
            this.readingLabel = readingLabel;
            this.speedLabel = speedLabel;
        }

        @Override
        public void run() {
            // opening port
            if (portIn.openPort()) {
                appendText(mainTextArea, "Port opened successfully\n");
            } else {
                appendText(mainTextArea, "ERROR: Port not opened\n");
                return;
            }

            try (InputStream in = portIn.getInputStream()) {
                int intPart, fracPart;
                float tempReading;

                while (MainController.readPort) {
                    int data = in.read();
                    // synchronizing by reading service byte (FF)
                    if (data == 255) {
                        data = in.read();
                        // checking status ('O'/'E')
                        if (data == 79) {
                            data = in.read();
                            // retrieving speed information ('S'/'F')
                            if (data == 83) {
                                updateSpeedLabel(true);
                            } else if (data == 70) {
                                updateSpeedLabel(false);
                            } else {
                                appendText(mainTextArea, "ERROR: Invalid speed byte\n");
                                return;
                            }
                            // retrieving 2 bytes (integer + fractional part)
                            intPart = in.read();
                            appendText(mainTextArea, intPart + ".");

                            fracPart = in.read();
                            appendText(mainTextArea, fracPart + "\n");

                            tempReading = intPart + (float) fracPart / 100;

                            updateReadingLabel(tempReading);

                            updateChart(tempReading);
                        } else {
                            appendText(mainTextArea, "ERROR: Invalid status byte\n");
                        }
                    }
                }
            } catch (IOException e) {
                portIn.closePort();
                e.printStackTrace();
            }
        }

        private void updateChart(float newReading) {
            // maintain a fixed size of 20 readings in the queue
            if (readingsQueue.size() >= 20) {
                readingsQueue.poll();
            }
            readingsQueue.offer(newReading);

            javafx.application.Platform.runLater(() -> {
                // get the first (and only) series from the chart or create it if not present
                BarChart.Series<String, Number> series;
                if (chart.getData().isEmpty()) {
                    series = new BarChart.Series<>();
                    chart.getData().add(series);
                } else {
                    series = chart.getData().getFirst();
                }

                // update the series data
                series.getData().clear(); // Clear old data to avoid duplicates
                int index = -20;
                String color;
                for (float reading : readingsQueue) {
                    XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(index), reading);
                    series.getData().add(data);
                    data.getNode().setStyle("-fx-bar-fill: rgba(0,102,255,0.85);");

//                    if (reading > 90) {color = "red";}
//                    else if (reading < 90 && reading > 60) {color = "yellow";}
//                    else {color = "green";}
//
//                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                    index++;
                }
            });
        }

        private void updateSpeedLabel(boolean speed){
            javafx.application.Platform.runLater(() -> {
                speedLabel.setText(speed?"SLOW":"FAST");
            });
        }

        private void updateReadingLabel(float reading) {
            javafx.application.Platform.runLater(() -> {
                String color;
                if (reading > 90) {color = "red";}
                else if ((reading < 90) && (reading > 60)) {color = "orange";}
                else {color = "green";}
                readingLabel.setStyle("-fx-text-fill: " + color + ";");
                readingLabel.setText(String.format("%.1f", reading));
            });
        }

        private void appendText(TextArea textArea, String text) {
            javafx.application.Platform.runLater(() -> textArea.appendText(text));
        }
    }


    private void getPorts(){
        ObservableList<String> portsList = FXCollections.observableArrayList();

        SerialPort[] ports = SerialPort.getCommPorts();

        // making a list of available ports
        for (SerialPort port : ports) {
            String path = port.getSystemPortPath();
            portsList.add(path.substring(4));
        }

        // filling up dropdown with ports
        portsDropdown.setItems(portsList);
        if(!portsList.isEmpty()){
            portsDropdown.setValue(portsList.getFirst());
        }
    }

    @FXML
    private void onStartPressed(){
        speedLabel.setVisible(true);
        dBLabel.setVisible(true);
        readingLabel.setVisible(true);
        readPort = true;
        stopButton.setDisable(false);
        startButton.setDisable(true);
        portsDropdown.setDisable(true);
        mainTextArea.clear();

        // getting port descriptor & setting baud rate/infinite timeout
        portIn = SerialPort.getCommPort(portsDropdown.getValue());
        portIn.setBaudRate(115200);
        portIn.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        if (!portsDropdown.getItems().isEmpty()) {
            ComReader comReader = new ComReader(portIn, mainTextArea, speedField, chart, readingLabel, speedLabel);
            readerThread = new Thread(comReader);
            readerThread.start();
        }
    }

    public void onStopPressed(ActionEvent actionEvent) throws InterruptedException {
        speedLabel.setVisible(false);
        dBLabel.setVisible(false);
        readingLabel.setVisible(false);
        readPort = false;
        stopButton.setDisable(true);
        startButton.setDisable(false);
        portsDropdown.setDisable(false);

        // closing port
        if (portIn.closePort()){
            mainTextArea.appendText("Port closed successfully\n");
        }
        else {
            mainTextArea.appendText("ERROR: PORT NOT CLOSED!\n");
        }
    }
}
module org.foxprod.com_grapher {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fazecast.jSerialComm;

    opens org.foxprod.com_grapher to javafx.fxml;
    exports org.foxprod.com_grapher;
}
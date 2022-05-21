module com.example.gbchat1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.logging.log4j;


    opens com.example.gbchat1 to javafx.fxml;
    exports com.example.gbchat1;

}
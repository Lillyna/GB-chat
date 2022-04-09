module com.example.gbchat1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.gbchat1 to javafx.fxml;
    exports com.example.gbchat1;
}
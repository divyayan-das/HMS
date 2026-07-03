module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.hotel to javafx.graphics;
    opens com.hotel.controller to javafx.fxml;
    opens com.hotel.model to javafx.base;

    exports com.hotel;
    exports com.hotel.controller;
    exports com.hotel.model;
    exports com.hotel.service;
}

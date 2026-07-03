package com.hotel;

import com.hotel.controller.MainController;
import com.hotel.service.HotelService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private HotelService hotelService;
    private MainController mainController;

    @Override
    public void start(Stage primaryStage) throws IOException {
        hotelService = new HotelService();

        FXMLLoader loader = new FXMLLoader(
    getClass().getResource("/com/hotel/fxml/MainView.fxml")
);
        loader.setControllerFactory(type -> {
            if (type == MainController.class) {
                mainController = new MainController(hotelService);
                return mainController;
            }
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Unable to create controller: " + type.getName(), e);
            }
        });

        Parent root = loader.load();
        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(
            getClass().getResource("/com/hotel/css/hotel.css").toExternalForm()
        );

        scene.widthProperty().addListener((obs, o, n) -> {
            double w = n.doubleValue();
            if (mainController != null) {
                mainController.setSidebarWidth(Math.max(170, Math.min(210, w * 0.17)));
            }
        });

        primaryStage.setTitle("HotelPro — Management System");
        primaryStage.getIcons().add(new Image(
            getClass().getResourceAsStream("/images/logo.png")
        ));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(720);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (hotelService != null) hotelService.saveAll();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

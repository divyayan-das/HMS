package com.hotel.controller;

import com.hotel.service.HotelService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    private final HotelService hotelService;

    @FXML private BorderPane mainLayout;
    @FXML private ScrollPane contentScroll;
    @FXML private StackPane contentWrapper;
    @FXML private StackPane sidebar;
    @FXML private StackPane sidebarBackground; 
    @FXML private VBox sidebarContent; 
    @FXML private ImageView sidebarBgView;
    @FXML private ImageView logoView;
    @FXML private Button dashboardButton;
    @FXML private Button roomsButton;
    @FXML private Button customersButton;
    @FXML private Button bookingsButton;
    @FXML private Button billingButton;

    private DashboardController dashboardController;
    private RoomController roomController;
    private CustomerController customerController;
    private BookingController bookingController;
    private BillingController billingController;

    private Button activeNavButton = null;

    public MainController(HotelService hotelService) {
        this.hotelService = hotelService;
        initControllers();
    }

    @FXML
    private void initialize() {
        if (sidebarBgView != null && sidebar != null) {
            sidebarBgView.fitWidthProperty().bind(sidebar.widthProperty());
            sidebarBgView.fitHeightProperty().bind(sidebar.heightProperty());
        }

        setActiveNav(dashboardButton);
        showPage("Dashboard");
    }

    private void initControllers() {
        dashboardController = new DashboardController(hotelService);
        roomController      = new RoomController(hotelService);
        customerController  = new CustomerController(hotelService);
        bookingController   = new BookingController(hotelService, roomController, customerController);
        billingController   = new BillingController(hotelService, bookingController);
    }

    @FXML private void showDashboard() { setActiveNav(dashboardButton); showPage("Dashboard"); }
    @FXML private void showRooms()     { setActiveNav(roomsButton);     showPage("Rooms"); }
    @FXML private void showCustomers() { setActiveNav(customersButton); showPage("Customers"); }
    @FXML private void showBookings()  { setActiveNav(bookingsButton);  showPage("Bookings"); }
    @FXML private void showBilling()   { setActiveNav(billingButton);   showPage("Billing"); }

    private void setActiveNav(Button btn) {
        if (btn == null) return;
        if (activeNavButton != null) activeNavButton.getStyleClass().remove("nav-button-active");
        if (!btn.getStyleClass().contains("nav-button-active")) btn.getStyleClass().add("nav-button-active");
        activeNavButton = btn;
    }

    private void showPage(String page) {
        if (contentWrapper == null) return;

        contentWrapper.getChildren().clear();
        VBox view = switch (page) {
            case "Dashboard" -> dashboardController.buildView();
            case "Rooms"     -> roomController.buildView();
            case "Customers" -> customerController.buildView();
            case "Bookings"  -> bookingController.buildView();
            case "Billing"   -> billingController.buildView();
            default           -> dashboardController.buildView();
        };

        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        StackPane.setAlignment(view, Pos.TOP_LEFT);
        contentWrapper.getChildren().add(view);
    }

    public void setSidebarWidth(double w) {
        if (sidebar != null) {
            sidebar.setPrefWidth(w);
            sidebar.setMinWidth(w);
            sidebar.setMaxWidth(w);
        }
    }
}

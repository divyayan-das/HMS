package com.hotel.controller;

import com.hotel.service.HotelService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class DashboardController {

    private final HotelService hotelService;

    public DashboardController(HotelService hotelService) { this.hotelService = hotelService; }

    public VBox buildView() {
        VBox view = new VBox(18);
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);

        // Title
        VBox titleBox = buildTitle("Dashboard Overview", "Real-time hotel statistics");
        HBox statsRow = buildStatsRow();
        HBox lowerRow = new HBox(16);
        lowerRow.getChildren().addAll(buildRoomSummaryCard(), buildQuickInfoCard());
        HBox.setHgrow(lowerRow.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(lowerRow.getChildren().get(1), Priority.ALWAYS);
        VBox.setVgrow(lowerRow, Priority.ALWAYS);

        view.getChildren().addAll(titleBox, statsRow, lowerRow);
        return view;
    }

    private HBox buildStatsRow() {
        long totalRooms    = hotelService.getAllRooms().size();
        long available     = hotelService.getAvailableRooms().size();
        long occupied      = hotelService.getOccupiedRoomsCount();
        long customers     = hotelService.getAllCustomers().size();
        long bookings      = hotelService.getTotalBookingsCount();
        double revenue     = hotelService.getTotalRevenue();

        HBox row = new HBox(12);
        row.getChildren().addAll(
            card("🏨", "" + totalRooms,                 "Total Rooms",    "stat-card-gold"),
            card("✓",  "" + available,                  "Available",      "stat-card-green"),
            card("●",  "" + occupied,                   "Occupied",       "stat-card-red"),
            card("◉",  "" + customers,                  "Guests",         "stat-card-gold"),
            card("▣",  "" + bookings,                   "Bookings",       "stat-card-amber"),
            card("◆",  String.format("₹%.0f", revenue), "Revenue",        "stat-card-green")
        );
        row.getChildren().forEach(c -> HBox.setHgrow(c, Priority.ALWAYS));
        return row;
    }

    private VBox card(String icon, String val, String lbl, String cls) {
        VBox c = new VBox(4);
        c.getStyleClass().addAll("stat-card", cls);
        c.setAlignment(Pos.CENTER_LEFT);
        Label ic = new Label(icon); ic.setStyle("-fx-font-size: 20px; -fx-text-fill: #C9A84C;");
        Label vl = new Label(val);  vl.getStyleClass().add("stat-value");
        Label lb = new Label(lbl);  lb.getStyleClass().add("stat-label");
        c.getChildren().addAll(ic, vl, lb);
        return c;
    }

    private VBox buildRoomSummaryCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("content-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label title = new Label("Room Type Summary");
        title.getStyleClass().add("card-title");

        long s = hotelService.getAllRooms().stream().filter(r -> r.getRoomType().name().equals("SINGLE")).count();
        long d = hotelService.getAllRooms().stream().filter(r -> r.getRoomType().name().equals("DOUBLE")).count();
        long x = hotelService.getAllRooms().stream().filter(r -> r.getRoomType().name().equals("DELUXE")).count();

        VBox rows = new VBox(10);
        rows.getChildren().addAll(
            infoRow("SINGLE Rooms", s + " rooms  ·  ₹80–₹85/night"),
            infoRow("DOUBLE Rooms", d + " rooms  ·  ₹120–₹130/night"),
            infoRow("DELUXE Rooms", x + " rooms  ·  ₹200/night")
        );
        card.getChildren().addAll(title, rows);
        return card;
    }

    private VBox buildQuickInfoCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("content-card");
        VBox.setVgrow(card, Priority.ALWAYS);

        Label title = new Label("Quick Info");
        title.getStyleClass().add("card-title");

        String[][] infos = {
            {"Booking Policy", "Full payment on check-in"},
            {"Tax Rate",       "10% on all bookings"},
            {"Check-in",       "2:00 PM"},
            {"Check-out",      "11:00 AM"},
            {"Cancellation",   "Free cancellation anytime"},
            {"Storage",        "Auto-saved to CSV files"}
        };
        VBox rows = new VBox(10);
        for (String[] i : infos) rows.getChildren().add(infoRow(i[0], i[1]));
        card.getChildren().addAll(title, rows);
        return card;
    }

    private HBox infoRow(String key, String val) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label k = new Label(key + ":"); k.getStyleClass().add("info-key");
        Label v = new Label(val);       v.getStyleClass().add("info-val");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        row.getChildren().addAll(k, sp, v);
        return row;
    }

    static VBox buildTitle(String title, String subtitle) {
        VBox box = new VBox(3);
        Label t = new Label(title); t.getStyleClass().add("page-title");
        Label s = new Label(subtitle); s.getStyleClass().add("page-subtitle");
        Region div = new Region();
        div.getStyleClass().add("title-divider");
        div.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(t, s, div);
        return box;
    }
}

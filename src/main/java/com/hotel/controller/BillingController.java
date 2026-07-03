package com.hotel.controller;

import com.hotel.service.HotelService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class BillingController {

    private final HotelService hotelService;
    private final BookingController bookingController;

    private TextArea billArea;
    private Label statusLabel, revenueLabel;
    private ComboBox<String> bookingCombo;

    public BillingController(HotelService hs, BookingController bc) {
        this.hotelService = hs; this.bookingController = bc;
    }

    public VBox buildView() {
        VBox view = new VBox(14);
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);

        VBox titleBox  = DashboardController.buildTitle("Billing Management", "Generate invoices and track revenue");
        HBox topRow    = buildTopRow();
        VBox invoiceCard = buildInvoiceCard();
        VBox.setVgrow(invoiceCard, Priority.ALWAYS);

        view.getChildren().addAll(titleBox, topRow, invoiceCard);
        return view;
    }

    private HBox buildTopRow() {
        HBox row = new HBox(14);
        row.setMaxWidth(Double.MAX_VALUE);

        // Revenue card
        VBox revCard = new VBox(6);
        revCard.getStyleClass().add("content-card");
        revCard.getStyleClass().add("revenue-box");
        revCard.setMinWidth(200);
        Label revTitle = new Label("Total Revenue");
        revTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #7A6A45; -fx-font-weight: bold;");
        revenueLabel = new Label("₹" + String.format("%.2f", hotelService.getTotalRevenue()));
        revenueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #C9A84C;");
        Label revNote = new Label("from checked-out bookings");
        revNote.setStyle("-fx-font-size: 11px; -fx-text-fill: #4A3A20;");
        revCard.getChildren().addAll(revTitle, revenueLabel, revNote);

        // Controls card
        VBox ctrlCard = new VBox(10);
        ctrlCard.getStyleClass().add("form-card");
        HBox.setHgrow(ctrlCard, Priority.ALWAYS);

        Label ctrlTitle = new Label("Generate Invoice");
        ctrlTitle.getStyleClass().add("card-title");

        HBox controls = new HBox(10); controls.setAlignment(Pos.CENTER_LEFT);
        bookingCombo = new ComboBox<>();
        bookingCombo.setPromptText("Choose booking...");
        bookingCombo.getStyleClass().add("combo-box");
        bookingCombo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(bookingCombo, Priority.ALWAYS);
        refreshBookingCombo();

        Button genBtn = new Button("Generate Invoice"); genBtn.getStyleClass().add("btn-primary");
        Button refBtn = new Button("↻ Refresh");        refBtn.getStyleClass().add("btn-secondary");
        Button clrBtn = new Button("Clear");             clrBtn.getStyleClass().add("btn-secondary");
        genBtn.setOnAction(e -> handleGenerateBill());
        refBtn.setOnAction(e -> { refreshBookingCombo(); refreshRevenue(); showStatus("✅ Refreshed.",true); });
        clrBtn.setOnAction(e -> { billArea.clear(); statusLabel.setText(""); });
        controls.getChildren().addAll(bookingCombo, genBtn, refBtn, clrBtn);

        // Billing info
        HBox infoRow = new HBox(20); infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.getStyleClass().add("billing-info-box");
        String[] items = {"Tax Rate: 10%", "Tax on subtotal", "Grand total on invoice", "Auto-saved on checkout"};
        for (String it : items) {
            Label l = new Label("◆  " + it); l.setStyle("-fx-font-size: 11px; -fx-text-fill: #6A5A35;");
            infoRow.getChildren().add(l);
        }

        statusLabel = new Label(""); statusLabel.setWrapText(true); statusLabel.setMaxWidth(Double.MAX_VALUE);
        ctrlCard.getChildren().addAll(ctrlTitle, controls, infoRow, statusLabel);

        row.getChildren().addAll(revCard, ctrlCard);
        return row;
    }

    private VBox buildInvoiceCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("content-card");
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox header = new HBox(10); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Invoice Preview"); title.getStyleClass().add("card-title");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label hint = new Label("Select a booking and click Generate Invoice");
        hint.setStyle("-fx-text-fill: #3A3020; -fx-font-size: 11px;");
        header.getChildren().addAll(title, sp, hint);

        billArea = new TextArea();
        billArea.setEditable(false);
        billArea.getStyleClass().add("bill-area");
        billArea.setPromptText("Invoice will appear here...");
        billArea.setWrapText(false);
        VBox.setVgrow(billArea, Priority.ALWAYS);
        billArea.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(header, billArea);
        return card;
    }

    private void refreshBookingCombo() {
        bookingCombo.getItems().clear();
        hotelService.getAllBookings().forEach(b ->
            bookingCombo.getItems().add("Booking #" + b.getBookingId() + " — " + b.getCustomerName() + " [" + b.getStatus() + "]")
        );
    }

    private void refreshRevenue() {
        revenueLabel.setText("₹" + String.format("%.2f", hotelService.getTotalRevenue()));
    }

    private void handleGenerateBill() {
        String sel = bookingCombo.getValue();
        if (sel == null) { showStatus("❌ Please select a booking.", false); return; }
        try {
            int id = Integer.parseInt(sel.split("#")[1].split(" ")[0].trim());
            String bill = hotelService.generateBill(id);
            if (bill != null) { billArea.setText(bill); showStatus("✅ Invoice generated for Booking #" + id, true); }
            else showStatus("❌ Could not find booking.", false);
        } catch (Exception e) { showStatus("❌ Error generating bill.", false); }
    }

    private void showStatus(String m, boolean s) {
        statusLabel.setText(m);
        statusLabel.getStyleClass().removeAll("label-error","label-success");
        statusLabel.getStyleClass().add(s ? "label-success" : "label-error");
    }
}

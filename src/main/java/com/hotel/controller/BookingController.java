package com.hotel.controller;

import com.hotel.model.Booking;
import com.hotel.service.HotelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;

public class BookingController {

    private final HotelService hotelService;
    private final RoomController roomController;
    private final CustomerController customerController;

    private TableView<Booking> bookingTable;
    private ObservableList<Booking> bookingData;
    private Label statusLabel;
    private ComboBox<String> customerCombo, roomCombo;
    private DatePicker checkInPicker, checkOutPicker;

    public BookingController(HotelService hs, RoomController rc, CustomerController cc) {
        this.hotelService = hs; this.roomController = rc; this.customerController = cc;
    }

    public VBox buildView() {
        VBox view = new VBox(14);
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);

        VBox titleBox  = DashboardController.buildTitle("Booking & Checkout", "Book rooms and manage guest stays");
        VBox formCard  = buildFormCard();
        VBox tableCard = buildTableCard();
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        view.getChildren().addAll(titleBox, formCard, tableCard);
        return view;
    }

    private VBox buildFormCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("form-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label cardTitle = new Label("New Booking");
        cardTitle.getStyleClass().add("card-title");

        // Row 1: booking fields
        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(6);
        form.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints c1=pct(22), c2=pct(22), c3=pct(16), c4=pct(16), c5=pct(24);
        form.getColumnConstraints().addAll(c1,c2,c3,c4,c5);

        customerCombo = combo("Select guest");
        roomCombo     = combo("Select room");
        refreshCustomerOptions(); refreshRoomOptions();
        checkInPicker  = new DatePicker(LocalDate.now());
        checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        checkInPicker.getStyleClass().add("date-picker");  checkInPicker.setMaxWidth(Double.MAX_VALUE);
        checkOutPicker.getStyleClass().add("date-picker"); checkOutPicker.setMaxWidth(Double.MAX_VALUE);

        form.add(lbl("Guest:"),        0,0); form.add(customerCombo, 0,1);
        form.add(lbl("Room:"),         1,0); form.add(roomCombo,     1,1);
        form.add(lbl("Check-In:"),     2,0); form.add(checkInPicker, 2,1);
        form.add(lbl("Check-Out:"),    3,0); form.add(checkOutPicker,3,1);

        // Buttons column
        VBox btnCol = new VBox(6);
        btnCol.setAlignment(Pos.BOTTOM_LEFT);
        Button bookBtn  = btn("Book Room",        "btn-success");
        Button refBtn   = btn("↻ Refresh",        "btn-secondary");
        bookBtn.setMaxWidth(Double.MAX_VALUE); refBtn.setMaxWidth(Double.MAX_VALUE);
        bookBtn.setOnAction(e -> handleBookRoom());
        refBtn.setOnAction(e  -> { refreshCustomerOptions(); refreshRoomOptions(); showStatus("✅ Refreshed.",true); });
        btnCol.getChildren().addAll(bookBtn, refBtn);
        form.add(btnCol, 4, 0, 1, 2);

        // Row 2: action buttons
        HBox actionRow = new HBox(10); actionRow.setAlignment(Pos.CENTER_LEFT);
        Label actLbl = new Label("Actions on selected booking:"); actLbl.getStyleClass().add("section-header");
        Button coBtn = btn("Check Out Selected","btn-warning");
        Button caBtn = btn("Cancel Booking",    "btn-danger");
        coBtn.setOnAction(e -> handleCheckout());
        caBtn.setOnAction(e -> handleCancelBooking());
        actionRow.getChildren().addAll(actLbl, coBtn, caBtn);

        statusLabel = new Label(""); statusLabel.setWrapText(true); statusLabel.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().addAll(cardTitle, form, actionRow, statusLabel);
        return card;
    }

    private VBox buildTableCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("content-card");
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox header = new HBox(10); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("All Bookings"); title.getStyleClass().add("card-title");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button refBtn = btn("↻ Refresh","btn-secondary"); refBtn.setOnAction(e->refreshTable());
        header.getChildren().addAll(title, sp, refBtn);

        bookingTable = new TableView<>();
        bookingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(bookingTable, Priority.ALWAYS);
        bookingTable.setMaxWidth(Double.MAX_VALUE);

        TableColumn<Booking,Integer>   idCol  = col("ID",       "bookingId");
        TableColumn<Booking,String>    cstCol = col("Guest",    "customerName");
        TableColumn<Booking,Integer>   rmCol  = col("Room",     "roomNumber");
        TableColumn<Booking,LocalDate> ciCol  = col("Check-In", "checkInDate");
        TableColumn<Booking,LocalDate> coCol  = col("Check-Out","checkOutDate");

        TableColumn<Booking,Double> totCol = col("Total (₹)","totalAmount");
        totCol.setCellFactory(c->new TableCell<>(){
            protected void updateItem(Double v,boolean e){super.updateItem(v,e);setText(e||v==null?null:String.format("₹%.2f",v));}
        });

        TableColumn<Booking,Booking.BookingStatus> stCol = col("Status","status");
        stCol.setCellFactory(c->new TableCell<>(){
            protected void updateItem(Booking.BookingStatus s,boolean e){
                super.updateItem(s,e);
                if(e||s==null){setText(null);return;}
                String clr = switch(s){
                    case ACTIVE      -> "-fx-text-fill: #81C784; -fx-font-weight: bold;";
                    case CHECKED_OUT -> "-fx-text-fill: #6A5A35; -fx-font-weight: bold;";
                    case CANCELLED   -> "-fx-text-fill: #E57373; -fx-font-weight: bold;";
                };
                setStyle(clr); setText(s.name().replace("_"," "));
            }
        });

        bookingTable.getColumns().addAll(idCol,cstCol,rmCol,ciCol,coCol,totCol,stCol);
        bookingData = FXCollections.observableArrayList(hotelService.getAllBookings());
        bookingTable.setItems(bookingData);

        card.getChildren().addAll(header, bookingTable);
        return card;
    }

    private void refreshCustomerOptions(){
        if(customerCombo==null) return;
        customerCombo.getItems().clear();
        hotelService.getAllCustomers().forEach(c->customerCombo.getItems().add(c.getCustomerId()+" - "+c.getName()));
    }
    private void refreshRoomOptions(){
        if(roomCombo==null) return;
        roomCombo.getItems().clear();
        hotelService.getAvailableRooms().forEach(r->roomCombo.getItems().add(r.getRoomNumber()+" ["+r.getRoomType()+"] ₹"+r.getPricePerDay()));
    }

    private void handleBookRoom() {
        String cs=customerCombo.getValue(), rs=roomCombo.getValue();
        LocalDate ci=checkInPicker.getValue(), co=checkOutPicker.getValue();
        if(cs==null||rs==null||ci==null||co==null){showStatus("❌ All fields required.",false);return;}
        if(!co.isAfter(ci)){showStatus("❌ Check-out must be after check-in.",false);return;}
        int cid=Integer.parseInt(cs.split(" - ")[0].trim());
        int rid=Integer.parseInt(rs.split(" ")[0].trim());
        Booking b = hotelService.bookRoom(cid,rid,ci,co);
        if(b!=null){refreshTable();refreshCustomerOptions();refreshRoomOptions();roomController.refreshTable();showStatus("✅ Booking #"+b.getBookingId()+" confirmed!",true);}
        else showStatus("❌ Booking failed. Room may be occupied.",false);
    }

    private void handleCheckout() {
        Booking sel = bookingTable.getSelectionModel().getSelectedItem();
        if(sel==null){showStatus("❌ Select a booking first.",false);return;}
        if(sel.getStatus()!=Booking.BookingStatus.ACTIVE){showStatus("❌ Only active bookings can be checked out.",false);return;}
        Alert a=new Alert(Alert.AlertType.CONFIRMATION,"Check out Booking #"+sel.getBookingId()+"?\nTotal: ₹"+String.format("%.2f",sel.getTotalAmount()),ButtonType.YES,ButtonType.NO);
        a.setTitle("Confirm Checkout"); a.setHeaderText(null);
        a.showAndWait().ifPresent(b->{if(b==ButtonType.YES){Booking co=hotelService.checkout(sel.getBookingId());if(co!=null){refreshTable();refreshCustomerOptions();refreshRoomOptions();roomController.refreshTable();showStatus("✅ Checked out! Bill: ₹"+String.format("%.2f",co.getTotalAmount()),true);}}});
    }

    private void handleCancelBooking() {
        Booking sel = bookingTable.getSelectionModel().getSelectedItem();
        if(sel==null){showStatus("❌ Select a booking first.",false);return;}
        if(sel.getStatus()!=Booking.BookingStatus.ACTIVE){showStatus("❌ Only active bookings can be cancelled.",false);return;}
        Alert a=new Alert(Alert.AlertType.CONFIRMATION,"Cancel Booking #"+sel.getBookingId()+"?",ButtonType.YES,ButtonType.NO);
        a.setTitle("Confirm"); a.setHeaderText(null);
        a.showAndWait().ifPresent(b->{if(b==ButtonType.YES){hotelService.cancelBooking(sel.getBookingId());refreshTable();refreshCustomerOptions();refreshRoomOptions();roomController.refreshTable();showStatus("✅ Booking cancelled.",true);}});
    }

    public void refreshTable(){ if(bookingData!=null) bookingData.setAll(hotelService.getAllBookings()); }

    private ComboBox<String> combo(String prompt){ ComboBox<String> c=new ComboBox<>();c.setPromptText(prompt);c.getStyleClass().add("combo-box");c.setMaxWidth(Double.MAX_VALUE);return c; }
    private void showStatus(String m,boolean s){statusLabel.setText(m);statusLabel.getStyleClass().removeAll("label-error","label-success");statusLabel.getStyleClass().add(s?"label-success":"label-error");}
    private Label lbl(String t){Label l=new Label(t);l.getStyleClass().add("form-label");return l;}
    private Button btn(String t,String cls){Button b=new Button(t);b.getStyleClass().add(cls);return b;}
    private ColumnConstraints pct(double p){ColumnConstraints c=new ColumnConstraints();c.setPercentWidth(p);c.setHgrow(Priority.ALWAYS);return c;}
    private <S,T> TableColumn<S,T> col(String t,String prop){TableColumn<S,T> c=new TableColumn<>(t);c.setCellValueFactory(new PropertyValueFactory<>(prop));return c;}
}

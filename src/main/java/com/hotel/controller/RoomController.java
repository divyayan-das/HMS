package com.hotel.controller;

import com.hotel.model.Room;
import com.hotel.service.HotelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class RoomController {

    private final HotelService hotelService;
    private TableView<Room> roomTable;
    private ObservableList<Room> roomData;
    private Label statusLabel;
    private TextField roomNumberField, priceField;
    private ComboBox<String> roomTypeCombo, filterCombo;

    public RoomController(HotelService hotelService) { this.hotelService = hotelService; }

    public VBox buildView() {
        VBox view = new VBox(14);
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);

        VBox titleBox = DashboardController.buildTitle("Room Management", "Add, view and manage hotel rooms");
        VBox formCard = buildFormCard();
        VBox tableCard = buildTableCard();
        VBox.setVgrow(tableCard, Priority.ALWAYS);

        view.getChildren().addAll(titleBox, formCard, tableCard);
        return view;
    }

    private VBox buildFormCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("form-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label cardTitle = new Label("Add New Room");
        cardTitle.getStyleClass().add("card-title");

        // Horizontal form using GridPane
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(6);
        form.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(20); c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(20); c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(20); c3.setHgrow(Priority.ALWAYS);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(40); c4.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2, c3, c4);

        roomNumberField = field("e.g. 301");
        roomTypeCombo   = new ComboBox<>(FXCollections.observableArrayList("SINGLE", "DOUBLE", "DELUXE"));
        roomTypeCombo.setPromptText("Select type");
        roomTypeCombo.getStyleClass().add("combo-box");
        roomTypeCombo.setMaxWidth(Double.MAX_VALUE);
        priceField = field("e.g. 120.00");

        form.add(lbl("Room Number:"), 0, 0); form.add(roomNumberField, 0, 1);
        form.add(lbl("Room Type:"),   1, 0); form.add(roomTypeCombo,   1, 1);
        form.add(lbl("Price/Day (₹):"),2,0); form.add(priceField,      2, 1);

        // Action buttons column
        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.BOTTOM_LEFT);
        Button addBtn = btn("Add Room",      "btn-primary");
        Button clrBtn = btn("Clear",         "btn-secondary");
        Button delBtn = btn("Delete Selected","btn-danger");
        addBtn.setOnAction(e -> handleAddRoom());
        clrBtn.setOnAction(e -> clearForm());
        delBtn.setOnAction(e -> handleDeleteRoom());
        btnRow.getChildren().addAll(addBtn, clrBtn, delBtn);
        form.add(btnRow, 3, 1);

        statusLabel = new Label("");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(cardTitle, form, statusLabel);
        return card;
    }

    private VBox buildTableCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("content-card");
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("All Rooms"); title.getStyleClass().add("card-title");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label flbl = new Label("Filter:"); flbl.getStyleClass().add("form-label");
        filterCombo = new ComboBox<>(FXCollections.observableArrayList("All","AVAILABLE","OCCUPIED"));
        filterCombo.setValue("All");
        filterCombo.getStyleClass().add("combo-box");
        filterCombo.setOnAction(e -> applyFilter());
        Button refBtn = btn("↻ Refresh", "btn-secondary");
        refBtn.setOnAction(e -> { filterCombo.setValue("All"); refreshTable(); });
        header.getChildren().addAll(title, sp, flbl, filterCombo, refBtn);

        roomTable = new TableView<>();
        roomTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(roomTable, Priority.ALWAYS);
        roomTable.setMaxWidth(Double.MAX_VALUE);

        TableColumn<Room,Integer>      numCol  = col("Room #",    "roomNumber");
        TableColumn<Room,Room.RoomType>typeCol = col("Type",      "roomType");
        TableColumn<Room,Double>       priceCol= col("Price/Day", "pricePerDay");
        priceCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Double p, boolean e) {
                super.updateItem(p, e);
                setText(e||p==null ? null : String.format("₹%.2f", p));
            }
        });
        TableColumn<Room,Room.RoomStatus> statusCol = col("Status","status");
        statusCol.setCellFactory(c -> new TableCell<>() {
            protected void updateItem(Room.RoomStatus s, boolean e) {
                super.updateItem(s, e);
                if (e||s==null){setText(null);setGraphic(null);return;}
                Label b = new Label(s.name());
                b.getStyleClass().add(s==Room.RoomStatus.AVAILABLE?"badge-available":"badge-occupied");
                setGraphic(b); setText(null);
            }
        });
        roomTable.getColumns().addAll(numCol, typeCol, priceCol, statusCol);
        roomData = FXCollections.observableArrayList(hotelService.getAllRooms());
        roomTable.setItems(roomData);

        card.getChildren().addAll(header, roomTable);
        return card;
    }

    private void handleAddRoom() {
        try {
            String n = roomNumberField.getText().trim(), t = roomTypeCombo.getValue(), p = priceField.getText().trim();
            if (n.isEmpty()||t==null||p.isEmpty()){showStatus("❌ All fields required.",false);return;}
            double pv = Double.parseDouble(p);
            if (pv<=0){showStatus("❌ Price must be positive.",false);return;}
            Room room = new Room(Integer.parseInt(n), Room.RoomType.valueOf(t), pv);
            if (hotelService.addRoom(room)){refreshTable();clearForm();showStatus("✅ Room "+n+" added!",true);}
            else showStatus("❌ Room "+n+" already exists.",false);
        } catch (NumberFormatException ex){showStatus("❌ Invalid number format.",false);}
    }

    private void handleDeleteRoom() {
        Room sel = roomTable.getSelectionModel().getSelectedItem();
        if (sel==null){showStatus("❌ Select a room first.",false);return;}
        if (!sel.isAvailable()){showStatus("❌ Cannot delete occupied room.",false);return;}
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Delete Room #"+sel.getRoomNumber()+"?",ButtonType.YES,ButtonType.NO);
        a.setTitle("Confirm"); a.setHeaderText(null);
        a.showAndWait().ifPresent(b->{if(b==ButtonType.YES){hotelService.deleteRoom(sel.getRoomNumber());refreshTable();showStatus("✅ Room deleted.",true);}});
    }

    private void applyFilter() {
        String f = filterCombo.getValue();
        if (f==null||f.equals("All")) roomData.setAll(hotelService.getAllRooms());
        else roomData.setAll(hotelService.getAllRooms().stream().filter(r->r.getStatus()==Room.RoomStatus.valueOf(f)).toList());
    }

    public void refreshTable() {
        if (roomData!=null){ String f=filterCombo!=null?filterCombo.getValue():"All"; if(f==null||f.equals("All")) roomData.setAll(hotelService.getAllRooms()); else applyFilter(); }
    }

    private void clearForm(){roomNumberField.clear();roomTypeCombo.setValue(null);priceField.clear();}
    private void showStatus(String m,boolean s){statusLabel.setText(m);statusLabel.getStyleClass().removeAll("label-error","label-success");statusLabel.getStyleClass().add(s?"label-success":"label-error");}
    private TextField field(String p){TextField f=new TextField();f.setPromptText(p);f.getStyleClass().add("text-field");f.setMaxWidth(Double.MAX_VALUE);return f;}
    private Label lbl(String t){Label l=new Label(t);l.getStyleClass().add("form-label");return l;}
    private Button btn(String t,String cls){Button b=new Button(t);b.getStyleClass().add(cls);return b;}
    private <S,T> TableColumn<S,T> col(String t,String prop){TableColumn<S,T> c=new TableColumn<>(t);c.setCellValueFactory(new PropertyValueFactory<>(prop));return c;}
}

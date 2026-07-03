package com.hotel.controller;

import com.hotel.model.Customer;
import com.hotel.service.HotelService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class CustomerController {

    private final HotelService hotelService;
    private TableView<Customer> customerTable;
    private ObservableList<Customer> customerData;
    private Label statusLabel;
    private TextField nameField, contactField, emailField;

    public CustomerController(HotelService hotelService) { this.hotelService = hotelService; }

    public VBox buildView() {
        VBox view = new VBox(14);
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);

        VBox titleBox = DashboardController.buildTitle("Customer Management", "Register and manage hotel guests");
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

        Label cardTitle = new Label("Register New Guest");
        cardTitle.getStyleClass().add("card-title");

        GridPane form = new GridPane();
        form.setHgap(14); form.setVgap(6);
        form.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints c1=new ColumnConstraints(); c1.setPercentWidth(22); c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2=new ColumnConstraints(); c2.setPercentWidth(22); c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3=new ColumnConstraints(); c3.setPercentWidth(22); c3.setHgrow(Priority.ALWAYS);
        ColumnConstraints c4=new ColumnConstraints(); c4.setPercentWidth(34); c4.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1,c2,c3,c4);

        nameField    = field("Full name");
        contactField = field("Phone number");
        emailField   = field("Email address");

        form.add(lbl("Full Name:"),  0,0); form.add(nameField,    0,1);
        form.add(lbl("Contact No:"), 1,0); form.add(contactField, 1,1);
        form.add(lbl("Email:"),      2,0); form.add(emailField,   2,1);

        HBox btnRow = new HBox(8); btnRow.setAlignment(Pos.BOTTOM_LEFT);
        Button regBtn = btn("Register",       "btn-primary");
        Button clrBtn = btn("Clear",          "btn-secondary");
        Button delBtn = btn("Delete Selected","btn-danger");
        regBtn.setOnAction(e -> handleAddCustomer());
        clrBtn.setOnAction(e -> clearForm());
        delBtn.setOnAction(e -> handleDeleteCustomer());
        btnRow.getChildren().addAll(regBtn, clrBtn, delBtn);
        form.add(btnRow, 3, 1);

        statusLabel = new Label(""); statusLabel.setWrapText(true); statusLabel.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().addAll(cardTitle, form, statusLabel);
        return card;
    }

    private VBox buildTableCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("content-card");
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(card, Priority.ALWAYS);

        HBox header = new HBox(10); header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Registered Guests"); title.getStyleClass().add("card-title");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Button refBtn = btn("↻ Refresh","btn-secondary"); refBtn.setOnAction(e->refreshTable());
        header.getChildren().addAll(title, sp, refBtn);

        customerTable = new TableView<>();
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(customerTable, Priority.ALWAYS);
        customerTable.setMaxWidth(Double.MAX_VALUE);

        TableColumn<Customer,Integer> idCol   = col("ID",      "customerId");
        TableColumn<Customer,String>  nameCol = col("Name",    "name");
        TableColumn<Customer,String>  cntCol  = col("Contact", "contactNumber");
        TableColumn<Customer,String>  emlCol  = col("Email",   "email");
        customerTable.getColumns().addAll(idCol, nameCol, cntCol, emlCol);
        customerData = FXCollections.observableArrayList(hotelService.getAllCustomers());
        customerTable.setItems(customerData);

        card.getChildren().addAll(header, customerTable);
        return card;
    }

    private void handleAddCustomer() {
        String n=nameField.getText().trim(), c=contactField.getText().trim(), e=emailField.getText().trim();
        if(n.isEmpty()||c.isEmpty()||e.isEmpty()){showStatus("❌ All fields required.",false);return;}
        if(!c.matches("\\d{7,15}")){showStatus("❌ Contact must be 7-15 digits.",false);return;}
        if(!e.contains("@")){showStatus("❌ Enter a valid email.",false);return;}
        Customer cu = hotelService.addCustomer(n,c,e);
        refreshTable(); clearForm(); showStatus("✅ Guest #"+cu.getCustomerId()+" registered!",true);
    }

    private void handleDeleteCustomer() {
        Customer sel = customerTable.getSelectionModel().getSelectedItem();
        if(sel==null){showStatus("❌ Select a guest first.",false);return;}
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,"Delete guest: "+sel.getName()+"?",ButtonType.YES,ButtonType.NO);
        a.setTitle("Confirm"); a.setHeaderText(null);
        a.showAndWait().ifPresent(b->{
            if(b==ButtonType.YES){
                if(hotelService.deleteCustomer(sel.getCustomerId())){refreshTable();showStatus("✅ Guest deleted.",true);}
                else showStatus("❌ Cannot delete: active booking exists.",false);
            }
        });
    }

    public void refreshTable(){ if(customerData!=null) customerData.setAll(hotelService.getAllCustomers()); }
    public ObservableList<Customer> getCustomerData(){ return customerData; }
    private void clearForm(){ nameField.clear(); contactField.clear(); emailField.clear(); }
    private void showStatus(String m,boolean s){statusLabel.setText(m);statusLabel.getStyleClass().removeAll("label-error","label-success");statusLabel.getStyleClass().add(s?"label-success":"label-error");}
    private TextField field(String p){TextField f=new TextField();f.setPromptText(p);f.getStyleClass().add("text-field");f.setMaxWidth(Double.MAX_VALUE);return f;}
    private Label lbl(String t){Label l=new Label(t);l.getStyleClass().add("form-label");return l;}
    private Button btn(String t,String cls){Button b=new Button(t);b.getStyleClass().add(cls);return b;}
    private <S,T> TableColumn<S,T> col(String t,String prop){TableColumn<S,T> c=new TableColumn<>(t);c.setCellValueFactory(new PropertyValueFactory<>(prop));return c;}
}

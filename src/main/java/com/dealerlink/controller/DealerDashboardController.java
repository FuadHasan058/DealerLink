package com.dealerlink.controller;

import com.dealerlink.App;
import com.dealerlink.dao.DeliveryDAO;
import com.dealerlink.dao.InventoryDAO;
import com.dealerlink.dao.OrderDAO;
import com.dealerlink.dao.ProductDAO;
import com.dealerlink.dao.QuotationDAO;
import com.dealerlink.dao.RequestDAO;
import com.dealerlink.model.InventoryItem;
import com.dealerlink.model.Order;
import com.dealerlink.model.ProductRequest;
import com.dealerlink.model.Quotation;
import com.dealerlink.model.User;
import com.dealerlink.service.WeatherService;
import com.dealerlink.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class DealerDashboardController {

    @FXML private Label headerLabel;
    @FXML private Label weatherStatusLabel;

    // Inventory Tab - Replaced ComboBox with TextFields
    @FXML private TextField invProductNameField;
    @FXML private TextField invUnitField;
    @FXML private TextField invQtyField;
    @FXML private TextField invPriceField;
    @FXML private Label invFeedbackLabel;

    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, Integer> colInvId;
    @FXML private TableColumn<InventoryItem, String> colInvProduct;
    @FXML private TableColumn<InventoryItem, String> colInvUnit;
    @FXML private TableColumn<InventoryItem, Integer> colInvQty;
    @FXML private TableColumn<InventoryItem, Double> colInvPrice;
    @FXML private TableColumn<InventoryItem, String> colInvUpdated;

    // Open Requests Tab
    @FXML private TableView<ProductRequest> requestsTable;
    @FXML private TableColumn<ProductRequest, Integer> colReqId;
    @FXML private TableColumn<ProductRequest, String> colReqShop;
    @FXML private TableColumn<ProductRequest, String> colReqCity;
    @FXML private TableColumn<ProductRequest, String> colReqProduct;
    @FXML private TableColumn<ProductRequest, Integer> colReqQty;
    @FXML private TableColumn<ProductRequest, String> colReqNotes;
    @FXML private TableColumn<ProductRequest, String> colReqStatus;

    @FXML private TextField quoteUnitRateField;
    @FXML private TextField quoteDaysField;
    @FXML private TextArea quoteMsgArea;
    @FXML private Label quoteFeedbackLabel;

    // Orders & Deliveries Tab
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colOrderId;
    @FXML private TableColumn<Order, String> colOrderShop;
    @FXML private TableColumn<Order, String> colOrderProduct;
    @FXML private TableColumn<Order, Double> colOrderTotal;
    @FXML private TableColumn<Order, String> colOrderStatus;
    @FXML private TableColumn<Order, String> colOrderDeliveryStatus;
    @FXML private TableColumn<Order, String> colOrderLocation;
    @FXML private TableColumn<Order, String> colOrderWeather;

    @FXML private ComboBox<String> deliveryStatusComboBox;
    @FXML private TextField deliveryLocationField;
    @FXML private TextField deliveryEtaField;
    @FXML private Label orderFeedbackLabel;

    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final QuotationDAO quotationDAO = new QuotationDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            headerLabel.setText("Dealer Portal: " + user.getName() + " (" + user.getCity() + ")");
            fetchWeather(user.getCity());
        }

        deliveryStatusComboBox.setItems(FXCollections.observableArrayList("PREPARING", "SHIPPED", "DELIVERED", "CANCELLED"));

        setupTableColumns();
        loadInventory();
        loadRequests();
        loadOrders();
    }

    private void setupTableColumns() {
        colInvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colInvProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colInvUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colInvQty.setCellValueFactory(new PropertyValueFactory<>("quantityAvailable"));
        colInvPrice.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colInvUpdated.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));

        colReqId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReqShop.setCellValueFactory(new PropertyValueFactory<>("shopName"));
        colReqCity.setCellValueFactory(new PropertyValueFactory<>("shopCity"));
        colReqProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colReqQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colReqNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderShop.setCellValueFactory(new PropertyValueFactory<>("shopName"));
        colOrderProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colOrderDeliveryStatus.setCellValueFactory(new PropertyValueFactory<>("deliveryStatus"));
        colOrderLocation.setCellValueFactory(new PropertyValueFactory<>("deliveryLocation"));
        colOrderWeather.setCellValueFactory(new PropertyValueFactory<>("weatherNote"));
    }

    private void fetchWeather(String city) {
        SessionManager.executor().execute(() -> {
            WeatherService.WeatherInfo info = WeatherService.getWeatherForCity(city);
            Platform.runLater(() -> weatherStatusLabel.setText(info.toString()));
        });
    }

    @FXML
    private void handleUpsertInventory(ActionEvent event) {
        String productName = invProductNameField.getText().trim();
        String unit = invUnitField.getText().trim();
        String qtyText = invQtyField.getText().trim();
        String priceText = invPriceField.getText().trim();

        if (productName.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            invFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            invFeedbackLabel.setText("Please enter product name, quantity, and price.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            double price = Double.parseDouble(priceText);
            User user = SessionManager.getCurrentUser();

            SessionManager.executor().execute(() -> {
                try {
                    int productId = productDAO.findOrCreateProduct(productName, unit.isEmpty() ? "units" : unit);
                    inventoryDAO.upsert(user.getId(), productId, qty, price);

                    Platform.runLater(() -> {
                        invFeedbackLabel.setStyle("-fx-text-fill: #16a34a;");
                        invFeedbackLabel.setText("Inventory updated successfully!");
                        invProductNameField.clear();
                        invUnitField.clear();
                        invQtyField.clear();
                        invPriceField.clear();
                        loadInventory();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        invFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                        invFeedbackLabel.setText("Error: " + e.getMessage());
                    });
                }
            });
        } catch (NumberFormatException e) {
            invFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            invFeedbackLabel.setText("Invalid numeric value for stock or price.");
        }
    }

    private void loadInventory() {
        User user = SessionManager.getCurrentUser();
        SessionManager.executor().execute(() -> {
            try {
                List<InventoryItem> items = inventoryDAO.getInventoryByDealer(user.getId());
                Platform.runLater(() -> inventoryTable.setItems(FXCollections.observableArrayList(items)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadRequests() {
        SessionManager.executor().execute(() -> {
            try {
                List<ProductRequest> requests = requestDAO.getOpenRequests();
                Platform.runLater(() -> requestsTable.setItems(FXCollections.observableArrayList(requests)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleSubmitQuotation(ActionEvent event) {
        ProductRequest selectedReq = requestsTable.getSelectionModel().getSelectedItem();
        String rateText = quoteUnitRateField.getText().trim();
        String daysText = quoteDaysField.getText().trim();
        String msg = quoteMsgArea.getText().trim();

        if (selectedReq == null || rateText.isEmpty() || daysText.isEmpty()) {
            quoteFeedbackLabel.setText("Select a request and enter unit rate and delivery days.");
            return;
        }

        try {
            double rate = Double.parseDouble(rateText);
            int days = Integer.parseInt(daysText);
            double total = rate * selectedReq.getQuantity();
            User user = SessionManager.getCurrentUser();

            Quotation q = new Quotation();
            q.setRequestId(selectedReq.getId());
            q.setDealerId(user.getId());
            q.setPricePerUnit(rate);
            q.setTotalPrice(total);
            q.setEstimatedDeliveryDays(days);
            q.setMessage(msg);

            SessionManager.executor().execute(() -> {
                try {
                    quotationDAO.createQuotation(q);
                    requestDAO.updateStatus(selectedReq.getId(), "QUOTED");

                    Platform.runLater(() -> {
                        quoteFeedbackLabel.setStyle("-fx-text-fill: #16a34a;");
                        quoteFeedbackLabel.setText("Quotation sent successfully!");
                        quoteUnitRateField.clear();
                        quoteDaysField.clear();
                        quoteMsgArea.clear();
                        loadRequests();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        quoteFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                        quoteFeedbackLabel.setText("Error submitting quotation: " + e.getMessage());
                    });
                }
            });
        } catch (NumberFormatException e) {
            quoteFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            quoteFeedbackLabel.setText("Please enter valid numbers for unit rate and days.");
        }
    }

    private void loadOrders() {
        User user = SessionManager.getCurrentUser();
        SessionManager.executor().execute(() -> {
            try {
                List<Order> orders = orderDAO.getOrdersByDealer(user.getId());
                Platform.runLater(() -> ordersTable.setItems(FXCollections.observableArrayList(orders)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleUpdateDelivery(ActionEvent event) {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        String status = deliveryStatusComboBox.getValue();
        String location = deliveryLocationField.getText().trim();
        String eta = deliveryEtaField.getText().trim();

        if (selectedOrder == null || status == null) {
            orderFeedbackLabel.setText("Select an order and choose status.");
            return;
        }

        SessionManager.executor().execute(() -> {
            try {
                String weatherNote = "N/A";
                if (!location.isEmpty()) {
                    WeatherService.WeatherInfo info = WeatherService.getWeatherForCity(location);
                    if (info.success) {
                        weatherNote = info.condition + " (" + info.temperatureC + "°C)";
                    }
                }

                deliveryDAO.updateStatusAndLocation(selectedOrder.getId(), status, location, weatherNote);
                if (!eta.isEmpty()) {
                    deliveryDAO.updateEstimatedArrival(selectedOrder.getId(), eta);
                }
                orderDAO.updateStatus(selectedOrder.getId(), status);

                Platform.runLater(() -> {
                    orderFeedbackLabel.setStyle("-fx-text-fill: #16a34a;");
                    orderFeedbackLabel.setText("Order & shipment updated!");
                    loadOrders();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    orderFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                    orderFeedbackLabel.setText("Error: " + e.getMessage());
                });
            }
        });
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadInventory();
        loadRequests();
        loadOrders();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();
        try {
            App.switchScene("/com/dealerlink/fxml/Login.fxml", "DealerLink - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
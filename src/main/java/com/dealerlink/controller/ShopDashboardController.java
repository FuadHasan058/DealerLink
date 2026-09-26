package com.dealerlink.controller;

import com.dealerlink.App;
import com.dealerlink.dao.DeliveryDAO;
import com.dealerlink.dao.OrderDAO;
import com.dealerlink.dao.ProductDAO;
import com.dealerlink.dao.QuotationDAO;
import com.dealerlink.dao.RequestDAO;
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

public class ShopDashboardController {

    @FXML private Label headerLabel;
    @FXML private Label weatherStatusLabel;

    // Request Creation
    @FXML private TextField productNameField;
    @FXML private TextField unitField;
    @FXML private TextField quantityField;
    @FXML private TextArea notesArea;
    @FXML private Label requestFeedbackLabel;

    // Requests Table
    @FXML private TableView<ProductRequest> myRequestsTable;
    @FXML private TableColumn<ProductRequest, Integer> colReqId;
    @FXML private TableColumn<ProductRequest, String> colReqProduct;
    @FXML private TableColumn<ProductRequest, String> colReqUnit;
    @FXML private TableColumn<ProductRequest, Integer> colReqQty;
    @FXML private TableColumn<ProductRequest, String> colReqStatus;
    @FXML private TableColumn<ProductRequest, String> colReqDate;

    // Quotations Table
    @FXML private TableView<Quotation> quotationsTable;
    @FXML private TableColumn<Quotation, Integer> colQuoteId;
    @FXML private TableColumn<Quotation, String> colQuoteDealer;
    @FXML private TableColumn<Quotation, String> colQuoteCity;
    @FXML private TableColumn<Quotation, Double> colQuotePriceUnit;
    @FXML private TableColumn<Quotation, Double> colQuoteTotal;
    @FXML private TableColumn<Quotation, Integer> colQuoteDays;
    @FXML private TableColumn<Quotation, String> colQuoteWeather;
    @FXML private TableColumn<Quotation, String> colQuoteStatus;
    @FXML private Label quoteFeedbackLabel;

    // Orders Table
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colOrderId;
    @FXML private TableColumn<Order, String> colOrderProduct;
    @FXML private TableColumn<Order, String> colOrderUnit;
    @FXML private TableColumn<Order, String> colOrderDealer;
    @FXML private TableColumn<Order, Double> colOrderTotal;
    @FXML private TableColumn<Order, String> colOrderStatus;
    @FXML private TableColumn<Order, String> colOrderDeliveryStatus;
    @FXML private TableColumn<Order, String> colOrderLocation;
    @FXML private TableColumn<Order, String> colOrderWeather;

    private final ProductDAO productDAO = new ProductDAO();
    private final RequestDAO requestDAO = new RequestDAO();
    private final QuotationDAO quotationDAO = new QuotationDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final DeliveryDAO deliveryDAO = new DeliveryDAO();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            headerLabel.setText("Shop Owner: " + user.getName() + " (" + user.getCity() + ")");
            fetchCityWeather(user.getCity());
        }

        setupTableColumns();
        loadMyRequests();
        loadOrders();

        myRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, selectedReq) -> {
            if (selectedReq != null) {
                loadQuotationsForRequest(selectedReq.getId());
            }
        });
    }

    private void setupTableColumns() {
        colReqId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReqProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colReqUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colReqQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colReqStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colReqDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colQuoteId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colQuoteDealer.setCellValueFactory(new PropertyValueFactory<>("dealerName"));
        colQuoteCity.setCellValueFactory(new PropertyValueFactory<>("dealerCity"));
        colQuotePriceUnit.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        colQuoteTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colQuoteDays.setCellValueFactory(new PropertyValueFactory<>("estimatedDeliveryDays"));
        colQuoteWeather.setCellValueFactory(new PropertyValueFactory<>("weatherNote"));
        colQuoteStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colOrderUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colOrderDealer.setCellValueFactory(new PropertyValueFactory<>("dealerName"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colOrderDeliveryStatus.setCellValueFactory(new PropertyValueFactory<>("deliveryStatus"));
        colOrderLocation.setCellValueFactory(new PropertyValueFactory<>("deliveryLocation"));
        colOrderWeather.setCellValueFactory(new PropertyValueFactory<>("weatherNote"));
    }

    private void fetchCityWeather(String city) {
        SessionManager.executor().execute(() -> {
            WeatherService.WeatherInfo info = WeatherService.getWeatherForCity(city);
            Platform.runLater(() -> weatherStatusLabel.setText(info.toString()));
        });
    }

    @FXML
    private void handleCreateRequest(ActionEvent event) {
        String productName = productNameField.getText().trim();
        String unit = unitField.getText().trim();
        String qtyText = quantityField.getText().trim();
        String notes = notesArea.getText().trim();

        if (productName.isEmpty() || qtyText.isEmpty()) {
            requestFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            requestFeedbackLabel.setText("Please enter product name and quantity.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            if (qty <= 0) throw new NumberFormatException();

            User user = SessionManager.getCurrentUser();

            SessionManager.executor().execute(() -> {
                try {
                    int productId = productDAO.findOrCreateProduct(productName, unit.isEmpty() ? "units" : unit);

                    ProductRequest req = new ProductRequest();
                    req.setShopId(user.getId());
                    req.setProductId(productId);
                    req.setQuantity(qty);
                    req.setNotes(notes);

                    requestDAO.createRequest(req);

                    Platform.runLater(() -> {
                        requestFeedbackLabel.setStyle("-fx-text-fill: #16a34a;");
                        requestFeedbackLabel.setText("Request submitted successfully!");
                        productNameField.clear();
                        unitField.clear();
                        quantityField.clear();
                        notesArea.clear();
                        loadMyRequests();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        requestFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                        requestFeedbackLabel.setText("Error: " + e.getMessage());
                    });
                }
            });
        } catch (NumberFormatException e) {
            requestFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
            requestFeedbackLabel.setText("Quantity must be a valid positive number.");
        }
    }

    private void loadMyRequests() {
        User user = SessionManager.getCurrentUser();
        SessionManager.executor().execute(() -> {
            try {
                List<ProductRequest> requests = requestDAO.getRequestsByShop(user.getId());
                Platform.runLater(() -> myRequestsTable.setItems(FXCollections.observableArrayList(requests)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadQuotationsForRequest(int requestId) {
        SessionManager.executor().execute(() -> {
            try {
                List<Quotation> quotes = quotationDAO.getQuotationsForRequest(requestId);
                for (Quotation q : quotes) {
                    if (q.getDealerCity() != null && !q.getDealerCity().isEmpty()) {
                        WeatherService.WeatherInfo info = WeatherService.getWeatherForCity(q.getDealerCity());
                        q.setWeatherNote(info.success ? info.condition + " (" + info.temperatureC + "°C)" : "N/A");
                    }
                }
                Platform.runLater(() -> quotationsTable.setItems(FXCollections.observableArrayList(quotes)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleAcceptQuotation(ActionEvent event) {
        Quotation selected = quotationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            quoteFeedbackLabel.setText("Select a quotation to accept.");
            return;
        }

        if (!"PENDING".equalsIgnoreCase(selected.getStatus())) {
            quoteFeedbackLabel.setText("Only pending quotations can be accepted.");
            return;
        }

        User user = SessionManager.getCurrentUser();
        SessionManager.executor().execute(() -> {
            try {
                quotationDAO.updateStatus(selected.getId(), "ACCEPTED");
                quotationDAO.rejectOtherQuotations(selected.getRequestId(), selected.getId());
                requestDAO.updateStatus(selected.getRequestId(), "ORDERED");

                int orderId = orderDAO.createOrder(
                        selected.getId(),
                        selected.getRequestId(),
                        user.getId(),
                        selected.getDealerId(),
                        selected.getTotalPrice()
                );

                if (orderId > 0) {
                    deliveryDAO.createForOrder(orderId);
                }

                Platform.runLater(() -> {
                    quoteFeedbackLabel.setStyle("-fx-text-fill: #16a34a;");
                    quoteFeedbackLabel.setText("Quotation accepted! Order placed.");
                    loadMyRequests();
                    loadQuotationsForRequest(selected.getRequestId());
                    loadOrders();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    quoteFeedbackLabel.setStyle("-fx-text-fill: #dc2626;");
                    quoteFeedbackLabel.setText("Error placing order: " + e.getMessage());
                });
            }
        });
    }

    private void loadOrders() {
        User user = SessionManager.getCurrentUser();
        SessionManager.executor().execute(() -> {
            try {
                List<Order> orders = orderDAO.getOrdersByShop(user.getId());
                Platform.runLater(() -> ordersTable.setItems(FXCollections.observableArrayList(orders)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadMyRequests();
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
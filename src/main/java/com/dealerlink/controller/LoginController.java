package com.dealerlink.controller;

import com.dealerlink.App;
import com.dealerlink.dao.UserDAO;
import com.dealerlink.model.User;
import com.dealerlink.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    // Sign In Fields
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private ComboBox<String> loginRoleComboBox;
    @FXML private Label loginStatusLabel;

    // Register Fields
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regNameField;
    @FXML private TextField regPhoneField;
    @FXML private TextField regAddressField;
    @FXML private TextField regCityField;
    @FXML private ComboBox<String> regRoleComboBox;
    @FXML private Label regStatusLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        loginRoleComboBox.setItems(FXCollections.observableArrayList("SHOP", "DEALER"));
        loginRoleComboBox.setValue("SHOP");

        regRoleComboBox.setItems(FXCollections.observableArrayList("SHOP", "DEALER"));
        regRoleComboBox.setValue("SHOP");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText();
        String role = loginRoleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            loginStatusLabel.setText("Please enter all login credentials.");
            return;
        }

        loginStatusLabel.setStyle("-fx-text-fill: #2563eb;");
        loginStatusLabel.setText("Authenticating...");

        SessionManager.executor().execute(() -> {
            try {
                var userOpt = userDAO.authenticate(username, password, role);
                Platform.runLater(() -> {
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        SessionManager.setCurrentUser(user);
                        try {
                            if ("SHOP".equalsIgnoreCase(user.getRole())) {
                                App.switchScene("/com/dealerlink/fxml/ShopDashboard.fxml", "DealerLink - Shop Owner Dashboard");
                            } else {
                                App.switchScene("/com/dealerlink/fxml/DealerDashboard.fxml", "DealerLink - Dealer Dashboard");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            loginStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                            loginStatusLabel.setText("Failed to load view: " + e.getMessage());
                        }
                    } else {
                        loginStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                        loginStatusLabel.setText("Invalid username, password, or role selection.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                    loginStatusLabel.setText("Database error: " + e.getMessage());
                });
            }
        });
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String name = regNameField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String address = regAddressField.getText().trim();
        String city = regCityField.getText().trim();
        String role = regRoleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || city.isEmpty()) {
            regStatusLabel.setStyle("-fx-text-fill: #dc2626;");
            regStatusLabel.setText("Username, password, name, and city are required.");
            return;
        }

        SessionManager.executor().execute(() -> {
            try {
                if (userDAO.usernameExists(username)) {
                    Platform.runLater(() -> {
                        regStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                        regStatusLabel.setText("Username already exists.");
                    });
                    return;
                }

                User newUser = new User(0, username, "", role, name, phone, address, city, "");
                int newId = userDAO.register(newUser, password);

                Platform.runLater(() -> {
                    if (newId > 0) {
                        regStatusLabel.setStyle("-fx-text-fill: #16a34a;");
                        regStatusLabel.setText("Registration successful! You can now log in.");
                        clearRegisterFields();
                    } else {
                        regStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                        regStatusLabel.setText("Failed to register user.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    regStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                    regStatusLabel.setText("Error: " + e.getMessage());
                });
            }
        });
    }

    private void clearRegisterFields() {
        regUsernameField.clear();
        regPasswordField.clear();
        regNameField.clear();
        regPhoneField.clear();
        regAddressField.clear();
        regCityField.clear();
    }
}
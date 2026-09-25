package com.dealerlink.util;

import com.dealerlink.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Holds the logged-in user and the shared thread pools used across the app so that
 * database access and REST/weather calls never block the JavaFX Application Thread.
 */
public class SessionManager {

    private static User currentUser;

    // General-purpose pool for one-off background jobs (DB queries, REST calls).
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread t = new Thread(runnable, "dealerlink-worker");
        t.setDaemon(true);
        return t;
    });

    // Scheduled pool used for periodic polling (e.g. auto-refreshing order/delivery status).
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(2, runnable -> {
        Thread t = new Thread(runnable, "dealerlink-scheduler");
        t.setDaemon(true);
        return t;
    });

    public static ExecutorService executor() {
        return EXECUTOR;
    }

    public static ScheduledExecutorService scheduler() {
        return SCHEDULER;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
        SCHEDULER.shutdownNow();
    }
}
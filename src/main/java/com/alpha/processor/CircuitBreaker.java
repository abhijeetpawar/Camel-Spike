package com.alpha.processor;

import com.alpha.exceptions.CircuitBreakerTripException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spi.ExecutorServiceManager;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CircuitBreaker implements Processor {
    private final int backOff;
    private final int tripLimit;
    private volatile boolean isOpen;
    private ScheduledExecutorService scheduler;

    public CircuitBreaker(int tripLimit, int backOff) {
        this.isOpen = false;
        this.tripLimit = tripLimit;
        this.backOff = backOff;
    }

    @Override
    public synchronized void process(Exchange exchange) throws Exception {
        if (isOpen) {
            throw new CircuitBreakerTripException();
        } else if ((int) exchange.getIn().getHeader("PENDING_COUNT") >= tripLimit) {
            isOpen = true;

            final CamelContext camelContext = exchange.getContext();
            final String routeId = exchange.getFromRouteId();
            final ExecutorServiceManager executorServiceManager = camelContext.getExecutorServiceManager();

            final ScheduledExecutorService scheduler = getScheduler(executorServiceManager);

            executorServiceManager.newThread("Open Circuit", () -> {
                try {
                    camelContext.suspendRoute(routeId);
                    System.out.println("\n\n*** ROUTE SUSPENDED ***\n\n");

                    scheduler.schedule(() -> {
                        try {
                            camelContext.resumeRoute(routeId);
                            isOpen = false;
                            System.out.println("\n\n*** ROUTE RESUMED ***\n\n");
                        } catch (Exception e) {
                            System.out.println("------------ PROBLEM IN RESUMING -------------");
                        }
                    }, backOff, TimeUnit.SECONDS);

                } catch (Exception e) {
                    System.out.println("------------ PROBLEM IN SUSPENDING -------------");
                }
            }).start();

            throw new CircuitBreakerTripException();
        }
    }

    private ScheduledExecutorService getScheduler(ExecutorServiceManager executorServiceManager) {
        if (this.scheduler == null) {
            this.scheduler = executorServiceManager.newScheduledThreadPool(this, "Close Circuit", 1);
        }
        return this.scheduler;
    }
}


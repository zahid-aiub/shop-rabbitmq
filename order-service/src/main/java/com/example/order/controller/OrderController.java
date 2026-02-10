package com.example.order.controller;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing orders in the event-driven system")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/test")
    public String testApi() {
        return "Test from Order service ...";
    }

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order and publishes OrderCreatedEvent to RabbitMQ for asynchronous processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "Order creation request with customer ID and items") @RequestBody CreateOrderRequest request) {
        log.info("Received create order request for customer: {}", request.getCustomerId());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieves a list of all orders in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = @Content(schema = @Schema(implementation = OrderResponse.class)))
    })
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        log.info("Received get all orders request");
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID", required = true) @PathVariable Long orderId) {
        log.info("Received get order request for ID: {}", orderId);
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }
}

package com.epam.store.controller.rest;

import com.epam.store.entity.Order;
import com.epam.store.entity.OrderCard;
import com.epam.store.service.OrderService;
import javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "orders")
public class OrderController {
    private OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> findAll() {
        return new ResponseEntity<>(orderService.findAll(), HttpStatus.OK);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Order> findById(@PathVariable(name = "id") Long orderId) throws NotFoundException {
        Optional<Order> orderResult = orderService.findById(orderId);
        if (orderResult.isPresent()) {
            return new ResponseEntity<>(orderResult.get(), HttpStatus.OK);
        } else {
            throw new NotFoundException("order with id = " + orderId + " not exist");
        }
    }

    @GetMapping(path = "/account/{id}")
    public ResponseEntity<List<Order>> findByAccountId(@PathVariable(name = "id") Long accountId) throws NotFoundException {
        List<Order> orders = orderService.findAllByAccountId(accountId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping
    public void createOrder(@RequestBody Order order) {
        orderService.save(order);
    }

    @PutMapping("/{id}")
    public void updateOrder(@RequestBody Order order, @PathVariable Long id) throws NotFoundException {
        if (orderService.findById(id).isPresent()) {
            orderService.save(order);
        } else {
            throw new NotFoundException("order with id = " + id + " not exist");
        }
    }

    @PostMapping("/{id}/add")
    public void addOrderCard(@RequestBody OrderCard orderCard, @PathVariable Long id) throws NotFoundException {
        if (orderService.findById(id).isPresent()) {
            orderService.saveOrderCard(id, orderCard);
        } else {
            throw new NotFoundException("order with id = " + id + " not exist");
        }
    }

    @GetMapping("/{orderId}/delete/{orderCardId}")
    public void deleteOrderCard(@PathVariable Long orderId, @PathVariable Long orderCardId) throws NotFoundException {
        if (orderService.findById(orderId).isPresent()) {
            orderService.deleteOrderCard(orderId, orderCardId);
        } else {
            throw new NotFoundException("order with id = " + orderId + " not exist");
        }
    }

    @DeleteMapping(path = "/{id}")
    public void deleteOrder(@PathVariable(name = "id") Long orderId) throws NotFoundException {
        Optional<Order> orderResult = orderService.findById(orderId);
        if (orderResult.isPresent()) {
            orderService.deleteById(orderId);
        } else {
            throw new NotFoundException("order with id = " + orderId + " not exist");
        }
    }
}

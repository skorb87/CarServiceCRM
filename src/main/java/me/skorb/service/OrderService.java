package me.skorb.service;

import me.skorb.entity.Customer;
import me.skorb.entity.Order;
import me.skorb.repository.OrderRepository;

import java.util.List;

public class OrderService {

    private final OrderRepository orderRepository = OrderRepository.getInstance();

    public int createOrder(Order order) {
        return orderRepository.save(order);
    }

    public Order getOrderById(int id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public void updateOrder(Order order) {
        orderRepository.update(order);
    }

    public void deleteOrder(Order order) {
        orderRepository.delete(order.getId());
    }

    public List<Order> getOrdersByCustomer(Customer customer) {
        return orderRepository.findByCustomerId(customer.getId());
    }

    public void savePhotoPath(int orderId, String photoPath) {
        orderRepository.savePhotoPath(orderId, photoPath);
    }

    public List<String> getPhotoPaths(int orderId) {
        return orderRepository.getPhotoPaths(orderId);
    }

    public void deletePhotoPath(String photoPath) {
        orderRepository.deletePhotoPath(photoPath);
    }

    public int getOrdersCountByStatus(String orderStatus) {
        return orderRepository.getOrdersCountByStatus(orderStatus);
    }

}

package com.lastmile.domain.service;

import com.lastmile.domain.model.BulkLoad;
import com.lastmile.domain.model.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class OrderDomainService {
    public ValidationResult validateOrders(List<Order> orders, Set<String> existingTrackingCodes){
        List<Order> validOrders = new ArrayList<>();
        List<BulkLoad.LoadError> errors = new ArrayList<>();

        for (int i = 0; i < orders.size(); i++){
            Order order = orders.get(i);
            int rowNumber = i + 2;

            List<String> rowErrors = validateSingleOrder(order, existingTrackingCodes);

            if(rowErrors.isEmpty()){
                validOrders.add(order);
            } else {
                errors.add(BulkLoad.LoadError.builder()
                        .rowNumber(rowNumber)
                        .errorDescription(String.join(", ", rowErrors))
                        .problematicValue(order.getExternalTrackingCode())
                        .build());
            }
        }

        return new ValidationResult(validOrders, errors);
    }

    private List<String> validateSingleOrder(Order order, Set<String> existingTrackingCodes){
        List<String> errors = new ArrayList<>();

        if (order.getRecipientName() == null || order.getRecipientName().isBlank()) {
            errors.add("Recipient name is required");
        }
        if (order.getAddressText() == null || order.getAddressText().isBlank()) {
            errors.add("Delivery address is required");
        }
        if (order.getWeightKg() == null || order.getWeightKg() <= 0) {
            errors.add("Weight must be greater than zero");
        }
        if (order.getVolumeCm3() == null || order.getVolumeCm3() <= 0) {
            errors.add("Volume must be greater than zero");
        }
        if (order.getExternalTrackingCode() != null
                && existingTrackingCodes.contains(order.getExternalTrackingCode())) {
            errors.add("Duplicate external tracking code: " + order.getExternalTrackingCode());
        }

        return errors;
    }

    public List<Order> processFailedOrdersForRescheduling(List<Order> failedOrders){
        Predicate<Order> isReschedulable = Order::canBeRescheduled;
        Function<Order, Order> reschedule = Order::reschedule;

        return failedOrders.stream()
                .filter(isReschedulable)
                .map(reschedule)
                .collect(Collectors.toList());
    }

    public String generateTrackingCode(){
        int year = LocalDateTime.now().getYear();
        String uniquePart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return "PKG-" + year + "-" + uniquePart;
    }

    public record ValidationResult(
            List<Order> validOrders,
            List<BulkLoad.LoadError> errors
    ){
        public boolean hasErrors(){
            return !errors.isEmpty();
        }

        public int totalProcessed(){
            return validOrders.size() + errors.size();
        }
    }
}

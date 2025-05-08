package com.nhattung.orderservice.repository;

import com.nhattung.orderservice.dto.MonthlyOrderStatsDto;
import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.dto.OrderStatusStatsDto;
import com.nhattung.orderservice.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {


    List<Order> findByUserId(String userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus = 'ORDER_COMPLETED'")
    BigDecimal getTotalRevenue();

    // Daily revenue data for the last 30 days
    @Query(value = "SELECT DATE(o.order_date) as date, SUM(o.total_amount) as revenue\n" +
            "\t\t\tFROM ms_order_db.orders o\n" +
            "            WHERE o.order_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 30 DAY) and o.order_status = 'ORDER_COMPLETED'\n" +
            "            GROUP BY DATE(o.order_date)\n" +
            "            ORDER BY DATE(o.order_date)", nativeQuery = true)
    List<Map<String, Object>> getDailyRevenue();

    // Weekly revenue data for the last 12 weeks
    @Query(value = "SELECT CONCAT('Week ', WEEK(o.order_date)) as date,\n" +
            "            SUM(o.total_amount) as revenue\n" +
            "            FROM ms_order_db.orders o\n" +
            "            WHERE o.order_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 WEEK) and o.order_status = 'ORDER_COMPLETED'\n" +
            "            GROUP BY date\n" +
            "            ORDER BY date", nativeQuery = true)
    List<Map<String, Object>> getWeeklyRevenue();

    // Monthly revenue data for the last 12 months
    @Query(value = "SELECT CONCAT(MONTHNAME(o.order_date), ' ', YEAR(o.order_date)) as date,\n" +
            "            SUM(o.total_amount) as revenue\n" +
            "            FROM ms_order_db.orders o\n" +
            "            WHERE o.order_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH) and o.order_status = 'ORDER_COMPLETED'\n" +
            "            GROUP BY YEAR(o.order_date), MONTH(o.order_date), date\n" +
            "            ORDER BY YEAR(o.order_date), MONTH(o.order_date)", nativeQuery = true)
    List<Map<String, Object>> getMonthlyRevenue();


    // Query to get revenue by category
    @Query(value = "SELECT oi.product_id as productId, SUM(oi.quantity * oi.price) as revenue\n" +
            "               FROM ms_order_db.order_item oi\n" +
            "               JOIN ms_order_db.orders o ON oi.order_id = o.id\n" +
            "               WHERE o.order_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH) and o.order_status = 'ORDER_COMPLETED'\n" +
            "               GROUP BY oi.product_id\n" +
            "               ORDER BY revenue DESC", nativeQuery = true)
    List<Map<String, Object>> getRevenueByProduct();

    // Query to get top selling products
    @Query(value = "SELECT oi.product_id as productId, oi.price as price,\n" +
            "            SUM(oi.quantity) as sold, SUM(oi.quantity * oi.price) as revenue\n" +
            "            FROM ms_order_db.order_item oi\n" +
            "            JOIN ms_order_db.orders o ON oi.order_id = o.id\n" +
            "            WHERE o.order_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH) and o.order_status = 'ORDER_COMPLETED'\n" +
            "            GROUP BY oi.product_id, oi.price\n" +
            "            ORDER BY revenue DESC\n" +
            "            LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> getTopSellingProducts();


    @Query(value = "SELECT YEAR(order_date) as year, MONTH(order_date) as month,\n" +
            "               COUNT(*) as count\n" +
            "               FROM ms_order_db.orders\n" +
            "               WHERE order_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH)\n" +
            "               GROUP BY YEAR(order_date), MONTH(order_date)\n" +
            "               ORDER BY year ASC, month ASC",
            nativeQuery = true)
    List<MonthlyOrderStatsDto> getMonthlyOrderStats();


    @Query(value = "SELECT order_status, COUNT(*) as count\n" +
            "               FROM  ms_order_db.orders\n" +
            "               WHERE order_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 12 MONTH)\n" +
            "               GROUP BY order_status\n" +
            "               ORDER BY count DESC",
            nativeQuery = true)
    List<Map<String,Object>> getOrderStatusStats();

    Page<Order> findAll(Specification<OrderDto> orderDtoSpecification, Pageable pageable);
}

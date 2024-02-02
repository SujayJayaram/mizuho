package com.mizuho;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class OrderBook {
    private static final Logger log = LogManager.getLogger(OrderBook.class);

    // In reality the OrderBook would be per security Id (e.g. ISIN, CUSIP etc) so we may
    // wish to store that in this object.
    public OrderBook() {}

    // Time complexity to insert or remove an element from a Skip List Map is O[log(n)]
    private Map<Double, List<Order> > bidQueue = new ConcurrentSkipListMap<>();
    private Map<Double, List<Order> > offerQueue = new ConcurrentSkipListMap<>(Comparator.reverseOrder());

    // No need to sort keys by order here so we can use a ConcurrentHashMap which offers O[1] performance
    private Map<Long, Order> mapIdToOrder = new ConcurrentHashMap<>();

    public void addOrder(Order order) throws Exception {
        log.debug(() -> "addOrder() called for order id:" + order.getId());
        Map<Double, List<Order> > queue;
        if ( order.getSide() == 'B' ) {
            queue = bidQueue;
        }
        else if ( order.getSide() == 'O' ) {
            queue = offerQueue;
        }
        else {
            throw new Exception("Encountered Order with unknown side: " + order.getSide());
        }

        synchronized (queue) {
            List<Order> orders = queue.computeIfAbsent(order.getPrice(), i -> new LinkedList<>());
            orders.add(order);
        }

        mapIdToOrder.put(order.getId(), order);
        log.debug(() -> "addOrder() exits for order id:" + order.getId());
    }

    public void removeOrder(long id) {
        log.debug(() -> "removeOrder() called for order id:" + id);

        Order order = mapIdToOrder.get(id);
        Map<Double, List<Order> > queue = null;
        if ( order.getSide() == 'B' ) {
            queue = bidQueue;
        }
        else if ( order.getSide() == 'O' ) {
            queue = offerQueue;
        }
        else {
            // Never get here as we check the side when the order was added.
        }

        synchronized (queue) {
            List<Order> orders = queue.get(order.getPrice());
            orders.remove(order);
            mapIdToOrder.remove(id);
        }

        log.debug(() -> "removeOrder() exits for order id:" + id);
    }

    public synchronized void modifyOrderSize(long id, long size) throws Exception {
        log.debug(() -> "modifyOrderSize() called for order id:" + id + " and size: " + size);
        Order order = mapIdToOrder.get(id);
        if ( order == null )
            return; // Might have already been removed by another thread

        Object monitor = null;
        if ( order.getSide() == 'B' ) {
            monitor = bidQueue;
        }
        else if ( order.getSide() == 'O' ) {
            monitor = offerQueue;
        }

        synchronized (monitor) {
            // Need to re-check if this order has not been removed
            // just now by another thread...
            order = mapIdToOrder.get(id);
            if ( order != null ) {
                removeOrder(id);

                if (size > 0)
                    addOrder(new Order(id, order.getPrice(), order.getSide(), size)); // Use new size here
            }
        }

        log.debug(() -> "modifyOrderSize() exits for order id:" + id + " and size: " + size);
    }

    public double getPriceForSideAndLevel(char side, int level) throws Exception {
        return 0;
    }

    public long getSizeForSideAndLevel(char side, int level) throws Exception {
        return 0;
    }

    public List<Order> getOrdersForSide(char side) {
        return null;
    }
}

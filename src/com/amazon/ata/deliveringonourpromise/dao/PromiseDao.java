package com.amazon.ata.deliveringonourpromise.dao;

//import com.amazon.ata.deliveringonourpromise.deliverypromiseservice.DeliveryPromiseServiceClient;
//import com.amazon.ata.deliveringonourpromise.orderfulfillmentservice.OrderFulfillmentServiceClient;
import com.amazon.ata.deliveringonourpromise.ordermanipulationauthority.OrderManipulationAuthorityClient;
import com.amazon.ata.deliveringonourpromise.types.Promise;
import com.amazon.ata.deliveringonourpromise.types.ServiceClient;
import com.amazon.ata.ordermanipulationauthority.OrderResult;
import com.amazon.ata.ordermanipulationauthority.OrderResultItem;
import com.amazon.ata.ordermanipulationauthority.OrderShipment;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO implementation for Promises.
 */
public class PromiseDao implements ReadOnlyDao<String, List<Promise>> {

    // To-Do replace dps client and ofClient with a list of serviceClient
    private OrderManipulationAuthorityClient omaClient;

    private List<ServiceClient> serviceClients;

    /**
     * PromiseDao constructor, accepting service clients for DPS and OMA.
     * @param serviceClients DeliveryPromiseServiceClient for DAO to access DPS
     * @param omaClient OrderManipulationAuthorityClient for DAO to access OMA
     */
    public PromiseDao(List<ServiceClient> serviceClients, OrderManipulationAuthorityClient omaClient) {
        // Single list of Service Clients instead of ofClient and dpsClient
        this.omaClient = omaClient;
        this.serviceClients = serviceClients;

    }

    /**
     * Returns a list of all Promises associated with the given order item ID.
     * @param customerOrderItemId the order item ID to fetch promise for
     * @return a List of promises for the given order item ID
     */
    @Override
    public List<Promise> get(String customerOrderItemId) {
        // Fetch the delivery date, so we can add to any promises that we find
        ZonedDateTime itemDeliveryDate = getDeliveryDateForOrderItem(customerOrderItemId);

        List<Promise> promises = new ArrayList<>();

        // fetch Promise from Delivery Promise Service. If exists, add to list of Promises to return.
        // Set delivery date
        // Create a for loop that will iterate over a list of all service clients

        for (ServiceClient client : serviceClients) {
            Promise promise = client.getDeliveryPromiseByOrderItemId(customerOrderItemId);
            if (promise != null) {
                promise.setDeliveryDate(itemDeliveryDate);
                promises.add(promise);
            }
        }


        return promises;
    }

    /*
     * Fetches the delivery date of the shipment containing the order item specified by the given order item ID,
     * if there is one.
     *
     * If the order item ID doesn't correspond to a valid order item, or if the shipment hasn't been delivered
     * yet, return null.
     */
    private ZonedDateTime getDeliveryDateForOrderItem(String customerOrderItemId) {
        OrderResultItem orderResultItem = omaClient.getCustomerOrderItemByOrderItemId(customerOrderItemId);

        if (null == orderResultItem) {
            return null;
        }

        OrderResult orderResult = omaClient.getCustomerOrderByOrderId(orderResultItem.getOrderId());

        for (OrderShipment shipment : orderResult.getOrderShipmentList()) {
            for (OrderShipment.ShipmentItem shipmentItem : shipment.getCustomerShipmentItems()) {
                if (shipmentItem.getCustomerOrderItemId().equals(customerOrderItemId)) {
                    return shipment.getDeliveryDate();
                }
            }
        }

        // didn't find a delivery date!
        return null;
    }
}

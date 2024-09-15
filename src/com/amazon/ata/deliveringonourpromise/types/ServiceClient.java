package com.amazon.ata.deliveringonourpromise.types;

public interface ServiceClient {

    /**
     * Method Signature for Service Client Interface.
     * @param customerOrderItemId The Id of the customers order item
     * @return String
     */
    Promise getDeliveryPromiseByOrderItemId(String customerOrderItemId);
}

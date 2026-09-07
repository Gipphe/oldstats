package com.oldstats.api;

public final class BankItemPayload {
    public final Integer itemId;
    public final String itemName;
    public final int quantity;
    public final long value;

    public BankItemPayload(Integer itemId, String itemName, int quantity, long value) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.value = value;
    }
}

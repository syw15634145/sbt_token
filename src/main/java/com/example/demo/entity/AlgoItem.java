package com.example.demo.entity;

public class AlgoItem {
    private String address;
    private String assetId;

    public AlgoItem(String address, String assetId) {
        this.address = address;
        this.assetId = assetId;
    }

    public String getAddress() {
        return address;
    }

    public String getAssetId() {
        return assetId;
    }

    @Override
    public String toString() {
        return String.join(address,assetId);
    }
}

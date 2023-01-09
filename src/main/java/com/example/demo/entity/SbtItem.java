package com.example.demo.entity;

import jakarta.persistence.*;
import org.springframework.data.redis.core.RedisHash;

@Entity
public class SbtItem {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;
    @Column(name = "address")
    private String address;
    @Column(name = "assetid")
    private String assetId;
    @Column(name = "note", length = 3000)
    private String note;
    @Column(name = "status")
    private String status;

    public SbtItem() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getNote() {
        return note;
    }

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public void setAssetId(String id) {
        this.assetId = id;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SBT {" +
                "address ='" + address + '\'' +
                ", asset id='" + assetId + '\'' +
                ", note='" + note + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

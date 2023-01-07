package com.example.demo;

import com.example.demo.entity.SbtItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SbtRepository extends JpaRepository<SbtItem, Integer> {
    SbtItem findByAssetId(String assetId);

    SbtItem findByAddressAndAssetId(String address, String assetId);
//     List<SbtItem> FindByAddressAndAssetId(String address, String assetId);
}

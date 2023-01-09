package com.example.demo.service;

import com.example.demo.SBTManager;
import com.example.demo.SbtRepository;
import com.example.demo.entity.AlgoItem;
import com.example.demo.entity.SbtItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class SbtService {
    @Autowired
    private SbtRepository sbtRepository;
    private SBTManager sbtManager;

    public String mintSBT() throws Exception {
        String assetId = sbtManager.mintSBT();
        SbtItem sbtItem = new SbtItem();
        sbtItem.setAddress(sbtManager.getAddress());
        sbtItem.setStatus("not claimed");
        sbtItem.setAssetId(assetId);
        sbtItem.setNote(sbtManager.getNote());
        sbtRepository.save(sbtItem);
        return assetId;
    }
    public SbtItem transferSBT(AlgoItem item) {
        try {
            sbtManager.transferSBT(item.getAddress(),item.getAssetId());
            SbtItem sbtItem = sbtRepository.findByAssetId(item.getAssetId());
            sbtItem.setNote(sbtManager.getNote());
            sbtItem.setAddress(item.getAddress());
            sbtItem.setStatus("active");
            sbtRepository.save(sbtItem);
            return sbtItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public SbtItem revokeSBT(AlgoItem item) {
        try {
            sbtManager.revokeSBT(item.getAddress(),item.getAssetId());
            SbtItem sbtItem = sbtRepository.findByAssetId(item.getAssetId());
            sbtItem.setStatus("revoked");
            sbtRepository.save(sbtItem);
            return sbtItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public SbtItem searchSBT (AlgoItem item) {
        try {
            SbtItem sbtItem = sbtRepository.findByAddressAndAssetId(item.getAddress(),item.getAssetId());
            return sbtItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

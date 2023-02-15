package com.example.demo.service;

import com.example.demo.SBTManager;
import com.example.demo.SbtRepository;
import com.example.demo.entity.AlgoItem;
import com.example.demo.entity.SbtItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class SbtService {

    @Autowired
    private SbtRepository sbtRepository;
    @Autowired
    private RedisTemplate<String,SbtItem> redisTemplate;
    private SBTManager sbtManager;
    private Lock lock = new ReentrantLock();
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
    @Async("asyncTaskExecutor")
    public CompletableFuture<SbtItem> searchSBT (AlgoItem item) {
        try {
            lock.lock();
            CompletableFuture<SbtItem> task = new CompletableFuture<>();
            final ValueOperations<String, SbtItem> operations = redisTemplate.opsForValue();
            boolean hasKey = redisTemplate.hasKey(item.getAssetId());
            if (hasKey) {
                long startTime = System.currentTimeMillis();
                SbtItem sbtItem = operations.get(item.getAssetId());
                long endTime = System.currentTimeMillis();
                System.out.println("cache：" + (endTime - startTime) + "ms");
                task.complete(sbtItem);
                lock.unlock();
                return task;
            } else {
                long startTime = System.currentTimeMillis();
                SbtItem sbtItem = sbtRepository.findByAddressAndAssetId(item.getAddress(),item.getAssetId());
                long endTime = System.currentTimeMillis();
                System.out.println("database：" + (endTime - startTime) + "ms");
                operations.set(sbtItem.getAssetId(),sbtItem);
                task.complete(sbtItem);
                lock.unlock();
                return task;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

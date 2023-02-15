package com.example.demo.controller;

import com.example.demo.SBTManager;
import com.example.demo.SbtRepository;
import com.example.demo.entity.AlgoItem;
import com.example.demo.entity.SbtItem;
import com.example.demo.service.SbtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;
import java.util.concurrent.CompletableFuture;

@RestController
public class FirstController {
    @Autowired
    private SbtService sbtService;
    public FirstController() throws GeneralSecurityException {
        this.sbtService = new SbtService();
    }

    @GetMapping("/mint")
    public String mintSBT() throws Exception {
        return sbtService.mintSBT();
    }

    @PostMapping(path = "/transfer")
    public ResponseEntity transferSBT(@RequestBody AlgoItem item) {
        try {
            sbtService.transferSBT(item);
            return ResponseEntity.ok("successfully transferred SBT"+ item.getAssetId().toString() + " to " + item.getAddress().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(path = "/revoke")
    public ResponseEntity revokeSBT(@RequestBody AlgoItem item) {
        try {
            sbtService.revokeSBT(item);
            return ResponseEntity.ok("successfully revoked SBT" + item.getAssetId().toString() + " from " + item.getAddress().toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    @PostMapping(path = "/search")
//    public ResponseEntity searchSBT (@RequestBody AlgoItem item) {
//        try {
//            List<SBTitem> list = sbtManager.searchSBT(item.getAddress(),item.getAssetid());
//            return ResponseEntity.ok(list.toString());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
    @PostMapping(path = "/search")
    public ResponseEntity searchSBT (@RequestBody AlgoItem item) {
        try {
            CompletableFuture<SbtItem> task = sbtService.searchSBT(item);
            if (task.get() != null) {
                return ResponseEntity.ok(task.get().toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("null");
    }





}

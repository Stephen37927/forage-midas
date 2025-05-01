package com.jpmc.midascore.controller;

import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class BalanceController {

    private BalanceService balanceService;

    @Autowired
    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam("userId") long userId) {

        float balanceAmount = balanceService.getBalance(userId);
        Balance balance = new Balance(balanceAmount);
        return balance;

    }

}

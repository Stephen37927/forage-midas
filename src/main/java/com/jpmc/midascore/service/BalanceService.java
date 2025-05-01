package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {

    private final UserRepository userRepository;

    @Autowired
    public BalanceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public float getBalance(long userId) {
        UserRecord userRecord = userRepository.findById(userId);
        if (userRecord != null) {
            return userRecord.getBalance();
        } else {
            //return 0 if user not found
            return 0;

        }
    }

}

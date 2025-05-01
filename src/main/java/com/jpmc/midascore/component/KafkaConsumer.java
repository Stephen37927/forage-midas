package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.service.IncentiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IncentiveService incentiveService;

    @Value("${general.kafka-topic}")
    private String topic;

    public KafkaConsumer(UserRepository userRepository, TransactionRepository transactionRepository, IncentiveService incentiveService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.incentiveService = incentiveService;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void receive(Transaction transaction) {
        logger.info("Received transaction: {}", transaction);

        // 获取发送方和接收方
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        // 验证交易
        if (isValidTransaction(transaction, sender, recipient)) {
            // 获取激励金额
            float incentiveAmount = incentiveService.getIncentiveAmount(transaction);

            // 更新余额
            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

            // 保存更新后的用户记录
            userRepository.save(sender);
            userRepository.save(recipient);

            // 记录交易
            TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount);
            transactionRepository.save(transactionRecord);

            logger.info("Transaction processed successfully");
        } else {
            logger.warn("Invalid transaction: {}", transaction);
        }
    }

    private boolean isValidTransaction(Transaction transaction, UserRecord sender, UserRecord recipient) {
        return sender != null &&
                recipient != null &&
                sender.getBalance() >= transaction.getAmount();
    }
}
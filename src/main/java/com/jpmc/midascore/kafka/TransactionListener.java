package com.jpmc.midascore.kafka;

import com.jpmc.midascore.dto.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@Transactional
public class TransactionListener {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRepository;
    private final RestTemplate restTemplate;

    @Value("${incentive.url}")
    private String incentiveUrl;

    public TransactionListener(UserRepository userRepository,
                               TransactionRecordRepository transactionRepository,
                               RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void listen(Transaction transaction) {

        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) return;

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        if (sender.getBalance() < transaction.getAmount()) return;

        float incentiveAmount = 0f;
        try {
            Incentive incentive = restTemplate.postForObject(
                    incentiveUrl,
                    transaction,
                    Incentive.class
            );
            incentiveAmount = incentive != null ? incentive.getAmount() : 0f;
        } catch (Exception e) {
            incentiveAmount = 0f;
        }

        sender.setBalance(sender.getBalance() - transaction.getAmount());

        recipient.setBalance(
                recipient.getBalance()
                        + transaction.getAmount()
                        + incentiveAmount
        );

        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(
                sender,
                recipient,
                transaction.getAmount(),
                incentiveAmount
        );

        transactionRepository.save(record);
    }
}
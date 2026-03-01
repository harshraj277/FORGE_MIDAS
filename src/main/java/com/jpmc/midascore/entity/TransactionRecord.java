package com.jpmc.midascore.entity;

import jakarta.persistence.*;

@Entity
public class TransactionRecord
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float amount;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private UserRecord sender;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private UserRecord recipient;

    private float incentive;

    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount ,  float incentive) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.incentive = incentive;
    }

    public TransactionRecord() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public UserRecord getSender() {
        return sender;
    }

    public void setSender(UserRecord sender) {
        this.sender = sender;
    }

    public UserRecord getRecipient() {
        return recipient;
    }

    public void setRecipient(UserRecord recipient) {
        this.recipient = recipient;
    }
}

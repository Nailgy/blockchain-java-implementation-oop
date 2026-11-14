package com.nali.blockchain.model;

import com.nali.blockchain.exception.InsufficientFundsException;
import com.nali.blockchain.exception.InvalidTransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.nali.blockchain.AmountAssertions.assertAmount;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionTest {

    private Ledger ledger;

    @BeforeEach
    void setUp() {
        ledger = new Ledger();
        ledger.credit("ALICE-01", new BigDecimal("100"));
    }

    @Test
    void transferShouldMoveAmountAndChargeFeeToSender() throws InsufficientFundsException {
        Transaction transfer = new TransferTransaction("ALICE-01", "BOB-02", new BigDecimal("30"), new BigDecimal("2"));

        transfer.applyTo(ledger);

        assertAmount("68", ledger.getBalance("ALICE-01"));
        assertAmount("30", ledger.getBalance("BOB-02"));
    }

    @Test
    void rewardShouldCreateNewMoney() {
        Transaction reward = new RewardTransaction("MINER-99", new BigDecimal("50"));

        assertDoesNotThrow(() -> reward.applyTo(ledger));

        assertAmount("50", ledger.getBalance("MINER-99"));
        assertAmount("100", ledger.getBalance("ALICE-01"));
    }

    @Test
    void eachTransactionTypeShouldApplyItsOwnBehaviour() throws InsufficientFundsException {
        // Polymorphism: the same call applyTo() does different things depending on the real type.
        List<Transaction> transactions = List.of(
                new RewardTransaction("BOB-02", new BigDecimal("10")),
                new TransferTransaction("ALICE-01", "BOB-02", new BigDecimal("20"), new BigDecimal("1")));

        for (Transaction transaction : transactions) {
            transaction.applyTo(ledger);
        }

        assertAmount("79", ledger.getBalance("ALICE-01"));
        assertAmount("30", ledger.getBalance("BOB-02"));
    }

    @Test
    void transferShouldFailWhenSenderHasNotEnoughMoney() {
        Transaction transfer = new TransferTransaction("ALICE-01", "BOB-02", new BigDecimal("100"), new BigDecimal("1"));

        assertThrows(InsufficientFundsException.class, () -> transfer.applyTo(ledger));
    }

    @Test
    void validTransferShouldPassValidation() {
        Transaction transfer = new TransferTransaction("ALICE-01", "BOB-02", new BigDecimal("1"), BigDecimal.ZERO);

        assertDoesNotThrow(transfer::validate);
    }

    @Test
    void shouldRejectZeroAmount() {
        Transaction transfer = new TransferTransaction("ALICE-01", "BOB-02", BigDecimal.ZERO, BigDecimal.ONE);

        assertThrows(InvalidTransactionException.class, transfer::validate);
    }

    @Test
    void shouldRejectNegativeFee() {
        Transaction transfer = new TransferTransaction("ALICE-01", "BOB-02", BigDecimal.TEN, new BigDecimal("-1"));

        assertThrows(InvalidTransactionException.class, transfer::validate);
    }

    @Test
    void shouldRejectTransferToYourself() {
        Transaction transfer = new TransferTransaction("ALICE-01", "ALICE-01", BigDecimal.TEN, BigDecimal.ONE);

        assertThrows(InvalidTransactionException.class, transfer::validate);
    }

    @Test
    void shouldRejectRewardWithZeroAmount() {
        Transaction reward = new RewardTransaction("MINER-99", BigDecimal.ZERO);

        assertThrows(InvalidTransactionException.class, reward::validate);
    }

    @Test
    void shouldRejectRewardWithNegativeAmount() {
        Transaction reward = new RewardTransaction("MINER-99", new BigDecimal("-50"));

        assertThrows(InvalidTransactionException.class, reward::validate);
    }
}

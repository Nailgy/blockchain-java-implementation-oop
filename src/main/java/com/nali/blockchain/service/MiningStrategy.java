package com.nali.blockchain.service;

import com.nali.blockchain.model.Block;
import com.nali.blockchain.model.Transaction;

import java.util.List;

/**
 * The rule used to create ("mine") new blocks and to check them.
 * The rest of the application only knows this interface, so the rule can be
 * replaced (for example an easier difficulty in tests) without changing other classes.
 */
public interface MiningStrategy {

    Block mineBlock(int index, String previousHash, List<Transaction> transactions);

    boolean isHashValid(String hash);
}

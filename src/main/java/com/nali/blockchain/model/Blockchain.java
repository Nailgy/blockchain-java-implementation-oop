package com.nali.blockchain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The ordered list of blocks. A new block is only accepted
 * if it points to the hash of the current last block.
 */
public class Blockchain {

    private final List<Block> blocks = new ArrayList<>();

    public void addBlock(Block block) {
        if (block.getIndex() != blocks.size()) {
            throw new IllegalArgumentException("Expected block #" + blocks.size() + " but got block #" + block.getIndex());
        }
        String expectedPreviousHash = isEmpty() ? Block.GENESIS_PREVIOUS_HASH : getLatestBlock().getHash();
        if (!block.getPreviousHash().equals(expectedPreviousHash)) {
            throw new IllegalArgumentException("Block #" + block.getIndex()
                    + " does not point to the last block of the chain");
        }
        blocks.add(block);
    }

    public Block getLatestBlock() {
        if (blocks.isEmpty()) {
            throw new IllegalStateException("The blockchain is empty");
        }
        return blocks.getLast();
    }

    public List<Block> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public int size() {
        return blocks.size();
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }
}

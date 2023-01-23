package com.github.Nail73;

import java.util.concurrent.BlockingDeque;

public interface MediatorsSubscriber {
    void notifyEof(BlockingDeque<String> deque);
}

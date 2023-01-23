package com.github.Nail73;

import java.util.concurrent.BlockingDeque;

public interface Mediator {
    void notifyEof(BlockingDeque<String> deque);
    void subscribe(MediatorsSubscriber subscriber);
}

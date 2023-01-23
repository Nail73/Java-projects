package com.github.Nail73;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class MediatorImpl implements Mediator {

    private List<MediatorsSubscriber> allReceivers = new ArrayList<>();

    @Override
    public synchronized void notifyEof(BlockingDeque<String> deque) {
        for (MediatorsSubscriber subscriber : allReceivers){
            subscriber.notifyEof(deque);
        }
    }

    public void subscribe(MediatorsSubscriber subscriber){
        allReceivers.add(subscriber);
    }
}

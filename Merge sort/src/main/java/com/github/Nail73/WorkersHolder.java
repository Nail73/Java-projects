package com.github.Nail73;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class WorkersHolder implements Closeable {
    private List<BlockingDeque<String>> deques = new CopyOnWriteArrayList<>();
    private List<ReadFileLineByLine> workers = new ArrayList<>();
    private Sorter sorter;
    private Mediator mediator;


    public WorkersHolder(Sorter sorter, Mediator mediator) {
        this.sorter = sorter;
        this.mediator = mediator;
    }

    public void doWork() {
        for (String file : Launcher.inputFileNames) {
            ReadFileLineByLine worker = new ReadFileLineByLine(file, Launcher.encoding, mediator);
            if (!worker.isFailed()) workers.add(worker);
        }
        if (workers.size() == 0) {
            log.error("Нет доступных для обработки входных файлов.");
            System.exit(20);
        } else {
            for (ReadFileLineByLine reader : workers) {
                BlockingDeque<String> deque = reader.beginAsyncReading();
                if (deque != null) deques.add(deque);
            }
        }
        sorter.doSort(deques);
    }

    @Override
    public void close() {
        workers.forEach(ReadFileLineByLine::close);
    }
}

package com.github.Nail73;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;


@Slf4j
public class ReadFileLineByLine implements Runnable, Closeable {

    private String filename;
    private FileInputStream inputStream;
    private Scanner sc;
    private BlockingDeque<String> queue;
    private ExecutorService service;
    private boolean failed;
    private Mediator mediator;

    public ReadFileLineByLine(String filename, String encoding, Mediator mediator) {
        this.mediator = mediator;
        this.filename = filename;
        try {
            inputStream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            log.error("Входной файл \'{}\' не открыт по причине \'{}\'. В сортировке не участвует.", filename, e.getMessage());
            failed = true;
            return;
        }

        sc = new Scanner(inputStream, encoding);
        queue = new LinkedBlockingDeque<>(2);
        service = Executors.newSingleThreadExecutor();
    }

    public boolean isFailed() {
        return failed;
    }

    public BlockingDeque beginAsyncReading() {
        if (!failed) {
            service.submit(this);
            return queue;
        } else {
            return null;
        }
    }

    @Override
    public void run() {
        try {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line == null) continue;
                String value = line.replaceAll("\\s+", "");
                try {
                    queue.putLast(value);
                } catch (InterruptedException e) {
                    log.warn("Прервано чтение файла \'{}\'", filename);
                }
            }
            if (sc.ioException() != null) // note that Scanner suppresses exceptions
                log.warn("Сбой при чтении файла \'{}\' по причине \'{}\'", filename, sc.ioException().getMessage());
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        if (sc != null) sc.close();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // hide on closing
            }
        }
        mediator.notifyEof(queue);
        service.shutdownNow();
    }
}

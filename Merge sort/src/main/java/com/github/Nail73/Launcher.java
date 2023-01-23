package com.github.Nail73;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Launcher {
    static boolean isAscending = true;
    static boolean isStrings = true;
    static String outputFileName = "";
    static String encoding = "utf-8";
    static List<String> inputFileNames = new ArrayList<>();

    public static void main(String[] args) {

        new ParseCommandLine(args).parse();

        Mediator mediator = new MediatorImpl();
        try (Sorter sorter = new SorterImpl(outputFileName, encoding);
             WorkersHolder holder = new WorkersHolder(sorter,mediator)) {
            mediator.subscribe((MediatorsSubscriber) sorter);
            holder.doWork();
        } catch (IOException e) {
            log.error("Проблема при закрытии файлов \'{}\'", e.getMessage());
        }
    }
}

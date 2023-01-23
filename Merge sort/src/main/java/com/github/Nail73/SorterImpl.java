package com.github.Nail73;

import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.BlockingDeque;

import static java.util.stream.Collectors.toList;

@Slf4j
public class SorterImpl implements Sorter, MediatorsSubscriber {

    private PrintWriter out;
    private List<BlockingDeque<String>> eofList;

    public SorterImpl(String outputFileName, String encoding) {
        eofList = new ArrayList<>();
        try {
            this.out = new PrintWriter(outputFileName, encoding);
        } catch (FileNotFoundException e) {
            log.error("Проблема с созданием выходного файла \'{}\' по причине \'{}\'", Launcher.outputFileName, e.getMessage());
            System.exit(10);
        } catch (UnsupportedEncodingException e) {
            log.error("Не поддерживается кодировка выходного файла \'{}\' по причине \'{}\'", Launcher.outputFileName, e.getMessage());
            System.exit(11);
        }
    }

    @Override
    public void doSort(List<BlockingDeque<String>> deques) {

        while (true) {
            cleanFinishedDeques(deques);
            if (deques.size() == 0) break;

            BlockingDeque<String> actualDeque = null;
            try {
                actualDeque = getActualDeque(deques);
                if (failedSortOrder(actualDeque)) {
                    deques.remove(actualDeque);
                    log.error("Нарушение сортировки в одном из входных файлов. Файл исключен из обработки."); // file name will show when shutdown reafer
                    continue;
                }
            } catch (NumberFormatException e) {
                deques.remove(actualDeque);
                log.error(e.getMessage());
                continue;
            } catch (IllegalArgumentException e) {
                continue;
            }

            try {
                if (actualDeque.size() > 0) out.println(actualDeque.takeFirst());
            } catch (InterruptedException e) {
                log.error("Прерывание при получении значения из очереди. \'{}\'", e.getMessage());
            }

        }
    }

    private void cleanFinishedDeques(List<BlockingDeque<String>> deques) {
        for (BlockingDeque<String> deque : deques) {
            if (eofList.contains(deque) && deque.size() == 0) {
                deques.remove(deque);
                eofList.remove(deque);
            }
        }
    }

    private boolean failedSortOrder(BlockingDeque<String> deque) throws NumberFormatException {
        if (deque.peekFirst() == null || deque.peekLast() == null) return false;
        if (Launcher.isStrings) {
            if (Launcher.isAscending) {
                return (deque.peekFirst()).compareTo(deque.peekLast()) > 0;
            } else {
                return (deque.peekFirst()).compareTo(deque.peekLast()) < 0;
            }
        } else {
            if (Launcher.isAscending) {
                return string2integer(deque.peekFirst()) > string2integer(deque.peekLast());
            } else {
                return string2integer(deque.peekFirst()) < string2integer(deque.peekLast());
            }
        }
    }

    private BlockingDeque<String> getActualDeque(List<BlockingDeque<String>> deques)
            throws IllegalArgumentException {
        if (deques.size() == 1) return deques.get(0);
        String vArray[] = new String[deques.size()];
        BlockingDeque<String> dArray[] = new BlockingDeque[deques.size()];
        int i = 0;
        for (BlockingDeque<String> deque : deques) {
            if (deque.peekFirst() == null) continue;
            vArray[i] = deque.peekFirst();
            dArray[i] = deque;
            i++;
        }

        if (Arrays.stream(vArray).anyMatch(Objects::isNull))
            throw new IllegalArgumentException("due over fast grabbing");

        if (Launcher.isStrings) {
            if (Launcher.isAscending) {
                return dArray[findIndexForStringsMinValue(vArray)];
            } else {
                return dArray[findIndexForStringsMaxValue(vArray)];
            }
        } else {
            if (Launcher.isAscending) {
                return dArray[findIndexForIntegersMinValue(vArray)];
            } else {
                return dArray[findIndexForIntegersMaxValue(vArray)];
            }
        }
    }


    private int findIndexForStringsMaxValue(String[] strings) {
        if (strings.length == 1) return 0;
        Optional<String> maximum = Arrays.stream(strings).max(Comparator.comparing(String::toString));
        return Arrays.stream(strings).collect(toList()).indexOf(maximum.get());
    }

    private int findIndexForStringsMinValue(String[] strings) {
        if (strings.length == 1) return 0;
        Optional<String> minimum = Arrays.stream(strings).min(Comparator.comparing(String::toString));
        return Arrays.stream(strings).collect(toList()).indexOf(minimum.get());
    }

    private int findIndexForIntegersMinValue(String[] numbers) throws NumberFormatException {
        if (numbers.length == 1) return 0;
        Optional<Integer> iMinimum = Arrays.stream(numbers).map(this::string2integer).min(Comparator.comparingInt(Integer::intValue));
        Optional<String> minimum = Optional.of(iMinimum.get().toString());
        return Arrays.stream(numbers).collect(toList()).indexOf(minimum.get());
    }

    private int findIndexForIntegersMaxValue(String[] numbers) throws NumberFormatException {
        if (numbers.length == 1) return 0;
        Optional<Integer> iMaximum = Arrays.stream(numbers).map(this::string2integer).max(Comparator.comparingInt(Integer::intValue));
        Optional<String> maximum = Optional.of(iMaximum.get().toString());
        return Arrays.stream(numbers).collect(toList()).indexOf(maximum.get());
    }

    @Override
    public void close() {
        if (out != null) out.close();
    }

    private Integer string2integer(String string) throws NumberFormatException {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Нарушение формата чисел в одном из входных файлов. " +
                    "Файл исключен из обработки. В строке '" + string +
                    "'. Причина '" + e.getMessage() + "'");
        }
    }

    @Override
    public void notifyEof(BlockingDeque<String> deque) {
        eofList.add(deque);
    }

}

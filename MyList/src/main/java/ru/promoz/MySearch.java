package ru.promoz;

import java.util.*;

public class MySearch {

    public static boolean bruteForceSearchInList(MyList<String> list, Object text) {
        for (Object o : list) {
            if (o.toString().contains((CharSequence) text)) {
                return true;
            }
        }
        return false;
    }

    public static boolean binarySearchInList(List<String> sortedList, String text) {
        return Collections.binarySearch(sortedList, text) >= 0;
    }
}
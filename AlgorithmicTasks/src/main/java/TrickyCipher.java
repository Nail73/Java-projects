import java.util.Scanner;

public class TrickyCipher {
    public static void main(String[] args) {
        int i = 0;
        int k = 0;
        int con = 0;
        String alphabet = "0abcdefghijklmnopqrstuvwxyz";

        Scanner scInt = new Scanner(System.in);
        Scanner scLine = new Scanner(System.in);

        int n = scInt.nextInt();

        String[] user = new String[n];
        while (i < n) {
            user[i] = scLine.nextLine().replaceAll(",", " ");
            i++;
        }
        String[] res = String.join(" ", user).split(" ");

        while (k < n) {
            String surname = res[0 + con];
            String name = res[1 + con];
            String patronymic = res[2 + con];
            String date = res[3 + con];
            String month = res[4 + con];
            String year = res[5 + con];

            String s = surname + name + patronymic;

            String temp = "";

            for (i = 0; i < s.length(); i++) {
                char current = s.charAt(i);
                if (temp.indexOf(current) <= 0) {
                    temp = temp + current;
                } else {
                    temp = temp.replace(String.valueOf(current), String.valueOf(current));
                }
            }

            int d = Integer.parseInt(date);
            int sumD = d / 10 + d % 10;
            int m = Integer.parseInt(month);
            int sumM = m / 10 + m % 10;
            int sum = sumD + sumM;

            int index = alphabet.indexOf(surname.toLowerCase().charAt(0));
            System.out.println("index" + index);

            int code = temp.length() + sum * 64 + index * 256;

            String hexString = Integer.toHexString(code);

            if (hexString.length() == 3) {
                System.out.println(hexString);
            } else if (hexString.length() > 3) {
                System.out.println(hexString.substring(hexString.length() - 3));
            } else {
                System.out.println("0" + hexString);
            }
            k++;
            con += 6;
        }
    }
}

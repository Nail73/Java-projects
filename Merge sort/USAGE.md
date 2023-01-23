#### сборка проекта

требуются
```
javac 17
openjdk version "17"
Apache Maven 4.0.0
```

`$ mvn package`

```
... 
[INFO] Building jar: ~/DEV/fs/FS6926/target/mergeSort-jar-with-dependencies.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
...
```

`target/mergeSort-jar-with-dependencies.jar` используется в запускающем скрипте `sort.sh`

#### параметры запуска

```
$ ./sort.sh 

usage: java -jar mergeSort.jar [OPTIONS] output.file input.files...
output.file  Обязательное имя файла с результатом сортировки.
input.files  Один, или больше входных файлов.
 -a          Сортировка по возрастанию. Применяется по умолчанию при
             отсутствии -a или -d.
 -d          Сортировка по убыванию. Опция не обязательна как и -a.
 -h,--help   Отобразить справку.
 -i          Файлы содержат целые числа. Обязательна, взаимоисключительна
             с -s.
 -s          Файлы содержат строки. Обязательна, взаимоисключительна с -i.
 -w          Файлы ожидаются в кодировке CP1251. Опция не обязательна. По
             умолчанию используется UTF8 кодировка файлов.
```
#### примеры запуска

неправильные параметры

```
$ ./sort.sh -a -d -i ; ./sort.sh -s -i ; ./sort.sh -i out.txt ;  ./sort.sh -s out.txt none

00:00:50.287 [main] WARN  com.github.Nail73.ParseCommandLine - Должна быть только одна опция или -a или -d
usage: java -jar mergeSort.jar [OPTIONS] output.file input.files...
output.file  Обязательное имя файла с результатом сортировки.
input.files  Один, или больше входных файлов.
 -a          Сортировка по возрастанию. Применяется по умолчанию при
             отсутствии -a или -d.
 -d          Сортировка по убыванию. Опция не обязательна как и -a.
 -h,--help   Отобразить справку.
 -i          Файлы содержат целые числа. Обязательна, взаимоисключительна
             с -s.
 -s          Файлы содержат строки. Обязательна, взаимоисключительна с -i.
 -w          Файлы ожидаются в кодировке CP1251. Опция не обязательна. По
             умолчанию используется UTF8 кодировка файлов.
             
00:00:50.628 [main] WARN  com.github.Nail73.ParseCommandLine - Должна быть только одна опция или -s или -i
usage: java -jar mergeSort.jar [OPTIONS] output.file input.files...
output.file  Обязательное имя файла с результатом сортировки.
input.files  Один, или больше входных файлов.
 -a          Сортировка по возрастанию. Применяется по умолчанию при
             отсутствии -a или -d.
 -d          Сортировка по убыванию. Опция не обязательна как и -a.
 -h,--help   Отобразить справку.
 -i          Файлы содержат целые числа. Обязательна, взаимоисключительна
             с -s.
 -s          Файлы содержат строки. Обязательна, взаимоисключительна с -i.
 -w          Файлы ожидаются в кодировке CP1251. Опция не обязательна. По
             умолчанию используется UTF8 кодировка файлов.
             
00:00:50.947 [main] WARN  com.github.Nail73.ParseCommandLine - Отсутствует имя файла для результата, или хотя бы одно имя входного файла.
usage: java -jar mergeSort.jar [OPTIONS] output.file input.files...
output.file  Обязательное имя файла с результатом сортировки.
input.files  Один, или больше входных файлов.
 -a          Сортировка по возрастанию. Применяется по умолчанию при
             отсутствии -a или -d.
 -d          Сортировка по убыванию. Опция не обязательна как и -a.
 -h,--help   Отобразить справку.
 -i          Файлы содержат целые числа. Обязательна, взаимоисключительна
             с -s.
 -s          Файлы содержат строки. Обязательна, взаимоисключительна с -i.
 -w          Файлы ожидаются в кодировке CP1251. Опция не обязательна. По
             умолчанию используется UTF8 кодировка файлов.
             
00:00:51.271 [main] ERROR com.github.Nail73.ReadFileLineByLine - Входной файл 'none' не открыт по причине 'none (Нет такого файла или каталога)'. В сортировке не участвует.
00:00:51.275 [main] ERROR com.github.Nail73.WorkersHolder - Нет доступных для обработки входных файлов.


$ touch out ; chmod a-w out ; ./sort.sh -s out in
23:47:57.060 [main] ERROR com.github.Nail73.SorterImpl - Проблема с созданием выходного файла 'out' по причине 'out (Отказано в доступе)'

```

входные файлы integer ascending
``` 
$ ./sort.sh -a -i out.txt ExampleData/*asc*.txt ; cat out.txt
23:50:53.819 [main] ERROR com.github.Nail73.SorterImpl - Нарушение формата чисел в одном из входных файлов. Файл исключен из обработки. В строке 'ASD'. Причина 'For input string: "ASD"'
23:50:53.824 [main] ERROR com.github.Nail73.SorterImpl - Нарушение формата чисел в одном из входных файлов. Файл исключен из обработки. В строке ''. Причина 'For input string: ""'
23:50:53.826 [main] ERROR com.github.Nail73.SorterImpl - Нарушение сортировки в одном из входных файлов. Файл исключен из обработки.
23:50:53.827 [pool-1-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in1ascErrSort.txt'
23:50:53.827 [pool-3-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in2ascErrEmptyLine.txt'
23:50:53.827 [pool-5-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in3ascErrType.txt'
-1
-1
0
1
1
1
1
1
2
3
4
4
5
6
7
8
9
16
27
64

```

входные файлы integer descending
``` 
$ ./sort.sh -d -i out.txt ExampleData/*desc*.txt ; cat out.txt
23:52:00.173 [main] ERROR com.github.Nail73.SorterImpl - Нарушение формата чисел в одном из входных файлов. Файл исключен из обработки. В строке '5.5'. Причина 'For input string: "5.5"'
23:52:00.176 [main] ERROR com.github.Nail73.SorterImpl - Нарушение сортировки в одном из входных файлов. Файл исключен из обработки.
23:52:00.179 [pool-4-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in3descErrTypeFloat.txt'
23:52:00.179 [pool-3-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in3descErrSort.txt'
65
28
16
9
9
7
7
7
6
6
5
5
4
4
3
2
1
1
1
0
-1

```

входные файлы string ascending
```
$ ./sort.sh -a -s out.txt ExampleData/*strAsc*.txt ; cat out.txt
23:53:56.274 [main] ERROR com.github.Nail73.SorterImpl - Нарушение сортировки в одном из входных файлов. Файл исключен из обработки.
23:53:56.280 [pool-2-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in5strAscErrSort.txt'
1
2
2
2
3
3
3
a
a
aa
aa
aaa
aaa
b
b
bb
bb
bbb
bbb
bbbb
bbbb

```


входные файлы string descending
``` 
$ ./sort.sh -d -s out.txt ExampleData/*strDesc*.txt ; cat out.txt
23:54:45.628 [main] ERROR com.github.Nail73.SorterImpl - Нарушение сортировки в одном из входных файлов. Файл исключен из обработки.
23:54:45.632 [pool-2-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in6strDescErrSort.txt'
а
Ц
Х
Ф
ZZZZ
ZZZZ
Z
Z
XXX
XXX
A
444
100A

```

тест на Windows c JRE 1.8.161 и скопированными jar и тестнабором файлов. 
есть артифакты кодировки в консольном выводе 
``` 
C:\sort>sort.cmd -i -a out.txt ExampleData/in1asc.txt ExampleData/in1ascErrSort.txt ExampleData/in2asc.txt ExampleData/in3asc.txt ExampleData/in3ascErrType.txt
Active code page: 65001
00:29:40.375 [main] ERROR com.github.Nail73.SorterImpl - Нарушение формата чисел в одном из входных файлов. Файл исключен из обработки. В строке 'ASD'. Причина 'For input string: "ASD"
'
аботки. В строке 'ASD'. Причина 'For input string: "ASD"'
put string: "ASD"'
00:29:40.406 [main] ERROR com.github.Nail73.SorterImpl - Нарушение сортировки в одном из входных файлов. Файл исключен из обработки.
�йлов. Файл исключен из обработки.
 из обработки.
отки.
�.
00:29:40.437 [pool-5-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in3ascErrType.txt'
n3ascErrType.txt'
00:29:40.437 [pool-2-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData/in1ascErrSort.txt'
n1ascErrSort.txt'

C:\sort>sort.cmd -s -d out.txt ExampleData/in6strDesc*
Active code page: 65001
00:37:50.774 [main] ERROR com.github.Nail73.SorterImpl - Нарушение сортировки в одном из входных файлов. Файл исключен из обработки.
�йлов. Файл исключен из обработки.
 из обработки.
отки.
�.
00:37:50.786 [pool-3-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleData\in6strDescErrSort.txt'
rDescErrSort.txt'

C:\sort>type out.txt
а
Ц
Х
Ф
ZZZZ
ZZZZ
Z
Z
XXX
XXX
A
444
100A

C:\sort>

```

тест сортировки файлов в кодировке cp1251 на Windows, с ключем '-w'.
результирующий out.txt приложен в ExampleDataWin/
``` 
C:\sort>sort.cmd -s -a -w out.txt ExampleDataWin/strAscCP1251.txt ExampleDataWin/strAscCP1251errSort.txt
Active code page: 65001
00:47:59.801 [main] ERROR com.github.Nail73.SorterImpl - Нарушение сортировки в одном из входных файлов. Файл исключен из обработки.
�йлов. Файл исключен из обработки.
 из обработки.
отки.
�.
00:47:59.801 [pool-2-thread-1] WARN  com.github.Nail73.ReadFileLineByLine - Прервано чтение файла 'ExampleDataWin/strAscCP1251errSort.txt'
P1251errSort.txt'

```
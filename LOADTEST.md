# Нагрузочное тестирование

Утилита `wrk`




## Результаты нагрузочного тестирования до оптимизации



### `PUT` без перезаписи.  
```

$ wrk --latency -c4 -d2m -s scripts/highload/put.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.07ms   18.50ms 331.08ms   97.41%
    Req/Sec   339.44     52.24   434.00     88.33%
  Latency Distribution
     50%    5.52ms
     75%    6.88ms
     90%    8.23ms
     99%   93.75ms
  80499 requests in 2.00m, 7.22MB read
Requests/sec:    670.76
Transfer/sec:     61.57KB
```

### `PUT` с перезаписью.
```

$ wrk --latency -c4 -d2m -s scripts/highload/putOver.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     6.74ms   14.76ms 276.97ms   97.73%
    Req/Sec   387.53     63.70   797.00     83.64%
  Latency Distribution
     50%    4.80ms
     75%    6.03ms
     90%    7.28ms
     99%   76.18ms
  92231 requests in 2.00m, 8.27MB read
Requests/sec:    768.00
Transfer/sec:     70.50KB
```

### `GET` с чтением разных записей.
```

$ wrk --latency -c4 -d2m -s scripts/highload/get.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     2.49ms    5.24ms 183.45ms   93.77%
    Req/Sec     1.33k   551.97     3.94k    78.62%
  Latency Distribution
     50%    1.34ms
     75%    1.83ms
     90%    3.23ms
     99%   24.81ms
  318915 requests in 2.00m, 207.07MB read
Requests/sec:   2655.88
Transfer/sec:      1.72MB
```

### `GET` с чтением ограниченного набора записей.
```

$ wrk --latency -c4 -d2m -s scripts/highload/getOver.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   698.88us    2.03ms  70.94ms   98.34%
    Req/Sec     3.89k   777.16     4.75k    90.00%
  Latency Distribution
     50%  442.00us
     75%  589.00us
     90%  792.00us
     99%    8.05ms
  929683 requests in 2.00m, 603.12MB read
Requests/sec:   7746.81
Transfer/sec:      5.03MB
```

### `PUT` + `GET`.
```

$ wrk --latency -c4 -d2m -s scripts/highload/putget.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     6.28ms   10.58ms 219.16ms   97.58%
    Req/Sec   388.76     56.00   500.00     88.83%
  Latency Distribution
     50%    4.94ms
     75%    6.13ms
     90%    7.40ms
     99%   58.11ms
  92707 requests in 2.00m, 32.24MB read
  Non-2xx or 3xx responses: 3584
Requests/sec:    772.36
Transfer/sec:    275.01KB
```


### `STATUS`.
```

$ wrk --latency -c4 -d2m -s scripts/highload/status.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   323.41us   65.52us   4.39ms   80.95%
    Req/Sec     5.85k   196.30     6.31k    83.14%
  Latency Distribution
     50%  313.00us
     75%  351.00us
     90%  394.00us
     99%  512.00us
  1397773 requests in 2.00m, 107.97MB read
Requests/sec:  11638.61
Transfer/sec:      0.90MB
```





## Результаты нагрузочного тестирования после оптимизации

Переделаем обработку get-запросов: теперь, если элемент не найден в базе, метод
`MyStore.get()` возвращает `null`, а не выбрасывает `NoSuchElementException`. 
Будем использовать `try-with-resources` в `MyStore`. Попробуем обрабатывать контекст
/v0/entity в отдельном потоке: put стал быстрее, но get медленнее. Сделаем обработку get 
в главном потоке, а остальные методы - в отдельном.  

### `PUT` без перезаписи.
#### Состояние базы: пустая.
```

$ wrk --latency -c4 -d2m -s scripts/highload/put.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    18.39ms   67.45ms 878.70ms   93.87%
    Req/Sec     1.35k   442.94     1.83k    84.64%
  Latency Distribution
     50%    1.03ms
     75%    1.41ms
     90%   33.48ms
     99%  367.08ms
  303083 requests in 2.00m, 27.17MB read
Requests/sec:   2524.09
Transfer/sec:    231.70KB
```

#### Состояние базы: 400 тыс записей.
```

$ wrk --latency -c4 -d2m -s scripts/highload/put.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    16.86ms   66.95ms 870.77ms   94.36%
    Req/Sec     1.49k   428.61     2.20k    86.50%
  Latency Distribution
     50%    0.95ms
     75%    1.25ms
     90%   21.20ms
     99%  360.57ms
  338104 requests in 2.00m, 30.31MB read
Requests/sec:   2816.19
Transfer/sec:    258.52KB
```


### `PUT` с перезаписью.
#### Состояние базы: пустая.
```

$ wrk --latency -c4 -d2m -s scripts/highload/putOver.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    14.13ms   56.20ms 647.49ms   94.12%
    Req/Sec     1.89k   422.98     2.24k    91.70%
  Latency Distribution
     50%  765.00us
     75%    0.89ms
     90%    1.50ms
     99%  312.88ms
  429887 requests in 2.00m, 38.54MB read
Requests/sec:   3580.86
Transfer/sec:    328.71KB
```

#### Состояние базы: 400 тыс записей.
```

$ wrk --latency -c4 -d2m -s scripts/highload/putOver.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    13.03ms   51.13ms 499.56ms   94.04%
    Req/Sec     1.96k   439.22     2.30k    90.00%
  Latency Distribution
     50%  739.00us
     75%  840.00us
     90%    1.45ms
     99%  290.34ms
  445752 requests in 2.00m, 39.96MB read
Requests/sec:   3713.24
Transfer/sec:    340.86KB
```

### `GET` с чтением разных записей. 
#### Состояние базы: 1 млн записей.
```

$ wrk --latency -c4 -d2m -s scripts/highload/get.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.28ms    1.32ms  47.04ms   97.94%
    Req/Sec     1.68k   403.86     3.48k    84.54%
  Latency Distribution
     50%    1.15ms
     75%    1.45ms
     90%    1.76ms
     99%    5.20ms
  401508 requests in 2.00m, 260.77MB read
Requests/sec:   3344.97
Transfer/sec:      2.17MB
```

### `GET` с чтением ограниченного набора записей.
#### Состояние базы: 1 млн записей.
```

$ wrk --latency -c4 -d2m -s scripts/highload/getOver.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   524.73us  246.38us  10.57ms   83.08%
    Req/Sec     3.71k   241.01     4.17k    81.64%
  Latency Distribution
     50%  503.00us
     75%  630.00us
     90%  751.00us
     99%    1.17ms
  887827 requests in 2.00m, 569.63MB read
Requests/sec:   7392.42
Transfer/sec:      4.74MB
```

### `PUT` + `GET`.
#### Состояние базы: пустая.
```

$ wrk --latency -c4 -d2m -s scripts/highload/putget.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    12.18ms   52.61ms 686.57ms   95.05%
    Req/Sec     1.48k   340.54     1.86k    90.31%
  Latency Distribution
     50%    1.18ms
     75%    1.58ms
     90%    2.34ms
     99%  309.55ms
  342013 requests in 2.00m, 103.86MB read
  Non-2xx or 3xx responses: 32286
Requests/sec:   2848.98
Transfer/sec:      0.87MB
```

#### Состояние базы: 400 тыс записей.
```

$ wrk --latency -c4 -d2m -s scripts/highload/putget.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    12.68ms   56.24ms 644.17ms   95.43%
    Req/Sec     1.50k   325.69     1.87k    90.71%
  Latency Distribution
     50%    1.17ms
     75%    1.57ms
     90%    2.24ms
     99%  349.18ms
  345917 requests in 2.00m, 105.24MB read
  Non-2xx or 3xx responses: 32392
Requests/sec:   2881.61
Transfer/sec:      0.88MB
```

### `STATUS`.
#### Состояние базы: 100 тыс записей.
```

$ wrk --latency -c4 -d2m -s scripts/highload/status.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   315.24us   63.07us   3.14ms   83.18%
    Req/Sec     6.01k   163.64     6.58k    83.31%
  Latency Distribution
     50%  305.00us
     75%  338.00us
     90%  379.00us
     99%  514.00us
  1435444 requests in 2.00m, 110.88MB read
Requests/sec:  11952.11
Transfer/sec:      0.92MB
```

## Итоги оптимизации
- `PUT` без перезаписи - среднее число запросов в секунду увеличилось примерно в 4 раза
- `PUT` с перезаписью - среднее число запросов в секунду увеличилось примерно в 5 раз
- `GET` с чтением разных записей - среднее число запросов в секунду увеличилось примерно на 25%
- `GET` с чтением ограниченного набора записей - среднее число запросов в секунду осталось почти таким же, но уменьшилась задержка примерно на 25%
- `PUT` + `GET` - среднее число запросов в секунду увеличилось примерно в 4 раза
- `STATUS` - результаты примерно те же.

Имеем хорошее увеличение быстродействия `PUT`, для GET быстродействие увеличилось менее значительно.
Для раз состояний базы, когда она пустая или наполнена (примерно 400 тыс записей), быстродействие одинаковое.
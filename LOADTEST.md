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
```

$ wrk --latency -c4 -d2m -s scripts/highload/put.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     9.12ms   32.81ms 523.40ms   96.87%
    Req/Sec   448.72     79.56     1.09k    93.30%
  Latency Distribution
     50%    4.09ms
     75%    4.54ms
     90%    5.19ms
     99%  178.03ms
  105874 requests in 2.00m, 9.49MB read
Requests/sec:    881.69
Transfer/sec:     80.94KB
```

### `PUT` с перезаписью.
```

$ wrk --latency -c4 -d2m -s scripts/highload/putOver.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     8.15ms   32.64ms 511.36ms   96.91%
    Req/Sec   570.96     91.13   747.00     93.25%
  Latency Distribution
     50%    3.14ms
     75%    3.46ms
     90%    3.91ms
     99%  185.26ms
  134422 requests in 2.00m, 12.05MB read
Requests/sec:   1119.81
Transfer/sec:    102.80KB
```

### `GET` с чтением разных записей.
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
```

$ wrk --latency -c4 -d2m -s scripts/highload/putget.lua http://localhost:8088

Running 2m test @ http://localhost:8088
  2 threads and 4 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.77ms   23.57ms 426.85ms   97.11%
    Req/Sec   802.02    124.11     1.00k    90.95%
  Latency Distribution
     50%    2.42ms
     75%    3.16ms
     90%    3.83ms
     99%  131.01ms
  189500 requests in 2.00m, 62.05MB read
  Non-2xx or 3xx responses: 1157
Requests/sec:   1579.02
Transfer/sec:    529.43KB
```

### `STATUS`.
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
`PUT` без перезаписи - среднее число запросов в секунду увеличилось примерно на 25%
`PUT` с перезаписью - среднее число запросов в секунду увеличилось примерно на 30%
`GET` с чтением разных записей - среднее число запросов в секунду увеличилось примерно на 25%
`GET` с чтением ограниченного набора записей - среднее число запросов в секунду осталось почти таким же, но уменьшилась задержка примерно на 25%
`PUT` + `GET` - среднее число запросов в секунду увеличилось примерно в 2 раза
`STATUS` - результаты примерно те же.

Имеем хорошее увеличение быстродействия `PUT`, для GET быстродействие увеличилось менее значительно.
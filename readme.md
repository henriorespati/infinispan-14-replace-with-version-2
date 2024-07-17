# Replace With Version on Infinispan 14

## How to Run
```
$ java -Xmx4G -Xms4G -XX:+UseShenandoahGC -jar target/infinispan-14-replace-with-version-2-1.0-SNAPSHOT.jar
```

## Logs
```
10-07-2024 16:31:36 [pool-1-thread-1] INFO  com.edw.helper.GenerateCacheHelper.lambda$generate$0 - success processing 391dbcf1-18d7-4037-9ce4-21a435bd3cb3 for 0
10-07-2024 16:31:36 [pool-1-thread-1] INFO  com.edw.helper.GenerateCacheHelper.lambda$generate$0 - success processing 71356421-3a13-4173-af63-136ec9a5e0c8 for 1
10-07-2024 16:31:36 [pool-1-thread-1] INFO  com.edw.helper.GenerateCacheHelper.lambda$generate$0 - success processing ab8f443b-d499-435f-a37e-210b31052a2c for 0
10-07-2024 16:31:36 [pool-1-thread-1] INFO  com.edw.helper.GenerateCacheHelper.lambda$generate$0 - success processing aac05fea-b222-46ad-a3a3-2283d0a2ef33 for 0
10-07-2024 16:31:36 [pool-1-thread-1] INFO  com.edw.helper.GenerateCacheHelper.lambda$generate$0 - success processing 63d64c2e-c822-4aaa-ac46-2c4c1ac520af for 1
10-07-2024 16:31:36 [pool-1-thread-1] INFO  com.edw.helper.GenerateCacheHelper.lambda$generate$0 - success processing 0271a6b0-4f0f-4d20-9c89-7fbc28244a88 for 0
10-07-2024 16:31:36 [pool-1-thread-1] INFO  com.edw.helper.GenerateCacheHelper.lambda$generate$0 - success processing 4babb57c-6b4b-4b22-9804-cc693ff73f21 for 0
10-07-2024 16:31:36 [pool-1-thread-1] INFO  com.edw.helper.GenerateCacheHelper.lambda$generate$0 - success processing c1e4263e-7e83-40cb-b030-0924e3ed3df2 for 0
10-07-2024 16:31:36 [http-nio-8080-exec-1] INFO  com.edw.helper.GenerateCacheHelper.generate - done ==================== for 35766
```

## Custom Controller

Testing with 1000 numUpdateRequest and 1 maxProcess

```
$ curl -kv http://127.0.0.1:8080/replace-with-version/v.1
*   Trying 127.0.0.1:8080...
* Connected to 127.0.0.1 (127.0.0.1) port 8080
> GET /replace-with-version/v.1 HTTP/1.1
> Host: 127.0.0.1:8080
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 200
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 90
< Date: Wed, 17 Jul 2024 04:22:58 GMT
<
* Connection #0 to host 127.0.0.1 left intact
Method:replaceWithVersion, Thread: 1, Total Row: 1000, elapsed time: 1437ms, TPS: 695.8942
```

Testing with 1000 numUpdateRequest and 2 maxProcess
```
$ curl -kv http://127.0.0.1:8080/replace-with-version/v.1
*   Trying 127.0.0.1:8080...
* Connected to 127.0.0.1 (127.0.0.1) port 8080
> GET /replace-with-version/v.1 HTTP/1.1
> Host: 127.0.0.1:8080
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 200
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 90
< Date: Wed, 17 Jul 2024 05:59:08 GMT
<
* Connection #0 to host 127.0.0.1 left intact
Method:replaceWithVersion, Thread: 2, Total Row: 1000, elapsed time: 2017ms, TPS: 495.7858
```


Testing with 1000 numUpdateRequest and 5 maxProcess
```
$ curl -kv http://127.0.0.1:8080/replace-with-version/v.1
*   Trying 127.0.0.1:8080...
* Connected to 127.0.0.1 (127.0.0.1) port 8080
> GET /replace-with-version/v.1 HTTP/1.1
> Host: 127.0.0.1:8080
> User-Agent: curl/8.6.0
> Accept: */*
>
< HTTP/1.1 200
< Content-Type: text/plain;charset=UTF-8
< Content-Length: 90
< Date: Wed, 17 Jul 2024 06:02:30 GMT
<
* Connection #0 to host 127.0.0.1 left intact
Method:replaceWithVersion, Thread: 5, Total Row: 1000, elapsed time: 3635ms, TPS: 275.1032
```

## Conclusion
Increasing number of threads, gives a negative impact to `replace-with-version` TPS. 
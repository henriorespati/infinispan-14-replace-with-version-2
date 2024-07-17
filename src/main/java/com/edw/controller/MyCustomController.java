package com.edw.controller;

import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <pre>
 *  com.edw.controller.MyCustomController
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 17 Jul 2024 11:16
 */
@RestController
public class MyCustomController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RemoteCacheManager cacheManager;

    @GetMapping("/replace-with-version/v.1")
    public String replaceWithVersion2(@RequestParam(defaultValue = "1", required = false) Integer maxProcess,
                                      @RequestParam(defaultValue = "1000", required = false) Integer numUpdateRequest) {

        final RemoteCache cache = cacheManager.getCache("balance");

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxProcess);
        try {
            Random random = new Random();
            String account = UUID.randomUUID().toString(); // this is your account number
            long startTime = System.currentTimeMillis();
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < numUpdateRequest; i++) {
                futures.add(executor.submit(() -> {
                    long start = System.currentTimeMillis();
                    boolean success = false;
                    double val1 = random.nextDouble(10000000D, 1000000000D);
                    int count1 = 0;
                    while (!success) {
                        MetadataValue metadataValue = cache.getWithMetadata(account);
                        if (metadataValue != null) {
                            BigDecimal newValue = new BigDecimal((String) metadataValue.getValue()).add(BigDecimal.valueOf(val1));
                            success = cache.replaceWithVersion(account, newValue, metadataValue.getVersion());
                        } else {
                            cache.put(account, new BigDecimal(0));
                        }
                        count1++;
                    }
                    logger.info("Count request: {}, elapsed: {}ms", count1, (System.currentTimeMillis() - start));
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            double tps = numUpdateRequest / ((elapsedTime == 0 ? 1D : elapsedTime) / 1000D);
            logger.info("Total Request: {}, Elapsed time: {}ms, TPS: {}", numUpdateRequest, elapsedTime, tps);

            return "Method:replaceWithVersion, Thread: %d, Total Row: %d, elapsed time: %dms, TPS: %.4f".formatted(maxProcess,
                    numUpdateRequest, elapsedTime, tps);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}

package com.edw.controller;

import jakarta.annotation.PostConstruct;
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
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <pre>
 *  com.edw.controller.LatestController
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 19 Jul 2024 11:07
 */
@RestController
public class LatestController {

    @Autowired
    private RemoteCacheManager balanceCache;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String account;

    @GetMapping("/replace-with-version/v.3")
    public String updateData(@RequestParam(defaultValue = "1", required = false) Integer maxProcess,
                             @RequestParam(defaultValue = "1000", required = false) Integer numUpdateRequest) throws InterruptedException {

        long oldTime = System.currentTimeMillis();

        // generate account number
        account = UUID.randomUUID().toString();

        for (int i = 0; i < numUpdateRequest; i++) {
            updateData(maxProcess);
        }

        log.info("-========- done in {} ms", System.currentTimeMillis() - oldTime);

        return String.format("done in %s ms \n", System.currentTimeMillis() - oldTime);
    }

    private List<LinkedHashMap<String, Object>> updateData(Integer maxProcess) throws InterruptedException {
        long oldTime = System.currentTimeMillis();

        String instructionType = UUID.randomUUID().toString();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(maxProcess);

        List<LinkedHashMap<String, Object>> listResultData = new ArrayList<>();

        List<Future<LinkedHashMap<String, Object>>> futures = new ArrayList<>();
        DebitBalance debitBalance = new DebitBalance(account, balanceCache.getCache("balance"));
        CreditBalance creditBalance = new CreditBalance(account, balanceCache.getCache("balance"));

        futures.add(executor.submit(debitBalance));
        futures.add(executor.submit(creditBalance));

        for (Future<LinkedHashMap<String, Object>> future : futures) {
            try {
                listResultData.add(future.get());
            } catch (Exception e) {
                throw new InterruptedException(e.getMessage());
            }
        }

        // print only if more than 10ms
        if (System.currentTimeMillis() - oldTime > 10) {
            log.info("[{}.{}.{}]: JDG, idPos : {}[{},{}] balance update time is {} ms = {} + {} , debet loop count {}, credit loop count {}",
                    debitBalance.threadName, instructionType, Math.max(executor.getCorePoolSize(), executor.getCorePoolSize()),
                    "0", "0", "0",
                    System.currentTimeMillis() - oldTime, debitBalance.time, creditBalance.time,
                    debitBalance.count, creditBalance.count);
        }

        return listResultData;
    }

    private class DebitBalance implements Callable<LinkedHashMap<String, Object>> {

        private int count;
        private long time;
        private String movement;
        private RemoteCache<String, Object> balanceCache;
        private long threadId;
        private String threadName;
        private final Random randomDelay = new Random();

        public DebitBalance(String movement, RemoteCache<String, Object> balanceCache) {
            this.movement = movement;
            this.balanceCache = balanceCache;
        }

        @Override
        public LinkedHashMap<String, Object> call() throws Exception {
            count = 0;
            time = System.currentTimeMillis();
            boolean result = Boolean.FALSE;
            LinkedHashMap<String, Object> data = null;
            while (!result) {
                MetadataValue<Object> versionData = balanceCache.getWithMetadata(movement);
                if (versionData != null) {
                    BigDecimal resultBalance = new BigDecimal(versionData.getValue().toString()).subtract(new BigDecimal(1000l));
                    result = balanceCache.replaceWithVersion(movement,
                            resultBalance.toString(),
                            versionData.getVersion());
                    if (result) {
                        log.debug(" + updateData debetAcc {} replaceWithVersion Balance {} result {}", movement,
                                resultBalance, result);
                    } else {
                        log.debug(" ++++++++ retry debetAcc {} replaceWithVersion Balance {} result {}", movement,
                                resultBalance, result);
                    }
                } else {
                    versionData = balanceCache.getWithMetadata(movement);
                    if (versionData == null) {
                        balanceCache.put(movement, new BigDecimal(0l));
                    }
                }
                count++;
            }
            time = System.currentTimeMillis() - time;
            threadId = Thread.currentThread().getId();
            threadName = Thread.currentThread().getName();
            return data;
        }
    }

    private class CreditBalance implements Callable<LinkedHashMap<String, Object>> {

        private int count;
        private long time;
        private String movement;
        private RemoteCache<String, Object> balanceCache;
        private long threadId;
        private String threadName;
        private final Random randomDelay = new Random();

        public CreditBalance(String movement, RemoteCache<String, Object> balanceCache) {
            this.movement = movement;
            this.balanceCache = balanceCache;
        }

        @Override
        public LinkedHashMap<String, Object> call() throws Exception {
            count = 0;
            time = System.currentTimeMillis();
            boolean result = Boolean.FALSE;
            ;
            LinkedHashMap<String, Object> data = null;
            while (!result) {
                MetadataValue<Object> versionData = balanceCache.getWithMetadata(movement);
                if (versionData != null) {
                    BigDecimal resultBalance = new BigDecimal(versionData.getValue().toString()).add(new BigDecimal(1000l));
                    result = balanceCache.replaceWithVersion(movement,
                            resultBalance.toString(),
                            versionData.getVersion());
                    if (result) {
                        log.debug(" - updateData creditAcc {} replaceWithVersion Balance {} result {}", movement,
                                resultBalance, result);
                    } else {
                        log.debug(" -------- retry creditAcc {} replaceWithVersion Balance {} result {}", movement,
                                resultBalance, result);
                    }
                } else {
                    versionData = balanceCache.getWithMetadata(movement);
                    if (versionData == null) {
                        balanceCache.put(movement, new BigDecimal(0l));
                    }
                }
                count++;
            }
            time = System.currentTimeMillis() - time;
            threadId = Thread.currentThread().getId();
            threadName = Thread.currentThread().getName();
            return data;
        }
    }
}

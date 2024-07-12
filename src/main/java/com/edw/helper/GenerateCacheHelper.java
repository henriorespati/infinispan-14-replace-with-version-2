package com.edw.helper;

import jakarta.annotation.PostConstruct;
import org.infinispan.client.hotrod.MetadataValue;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *  com.edw.helper.GenerateCacheHelper
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 10 Jul 2024 15:49
 */
@Service
public class GenerateCacheHelper {

    @Autowired
    private RemoteCacheManager cacheManager;

    private List<String> listOfUuid = new ArrayList<>();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private int  NUM_ENTRIES = 1000;
    private int  NUM_EXECUTORS = 100;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private CountDownLatch latch;

    @PostConstruct
    public void prepareData () {
        for (int i = 0; i < NUM_ENTRIES; i ++) {
            listOfUuid.add(UUID.randomUUID().toString());
        }
    }

    public void generate() {

        Long startingTime = System.currentTimeMillis();
        logger.info("starting ====================");
        latch = new CountDownLatch(NUM_EXECUTORS);

        final RemoteCache cache = cacheManager.getCache("balance");
        for(int j = 0 ; j < NUM_EXECUTORS; j ++) {
            executor.execute(() -> {
                for (int i = 0; i < NUM_ENTRIES; i++) {
                    while (true) {
                        Long timestamp = System.currentTimeMillis();
                        MetadataValue metadataValue = cache.getWithMetadata(listOfUuid.get(i));

                        if(metadataValue == null) {
                            cache.put(listOfUuid.get(i), new BigDecimal(1000));
                            break;
                        }

                        BigDecimal newValue =
                                ((BigDecimal) metadataValue.getValue()).add(new BigDecimal(1000));
                        Boolean success = cache.replaceWithVersion(listOfUuid.get(i), newValue, metadataValue.getVersion());

                        if (success) {
                            logger.info("success processing {} for {} ms",
                                    listOfUuid.get(i),
                                    System.currentTimeMillis() - timestamp);
                            break;
                        } else {
                            logger.info("+++ retrying {} for {} ms",
                                    listOfUuid.get(i),
                                    System.currentTimeMillis() - timestamp);
                        }
                    }
                }

                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        logger.info("done ==================== for {} ms",
                System.currentTimeMillis() - startingTime);
    }
}

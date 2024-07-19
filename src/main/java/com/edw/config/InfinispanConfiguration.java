package com.edw.config;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.commons.marshall.ProtoStreamMarshaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Value;

/**
 * <pre>
 *  com.edw.config.InfinispanConfiguration
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 10 Jul 2024 15:50
 */
@Configuration
public class InfinispanConfiguration {

    @Value( "${host}" )
    private String host;
    
    @Value( "${port}" )
    private String port;
    
    @Value( "${username}" )
    private String username;
    
    @Value( "${password}" )
    private String password;
    
    @Bean
    public RemoteCacheManager remoteCacheManager() {
        return new RemoteCacheManager(
                new org.infinispan.client.hotrod.configuration.ConfigurationBuilder()
                        .addServer().host(host).port(port)
                        .security().authentication().username(username).password(password)
                        .clientIntelligence(ClientIntelligence.HASH_DISTRIBUTION_AWARE)
                        .marshaller(ProtoStreamMarshaller.class)
                        .build());
    }
}

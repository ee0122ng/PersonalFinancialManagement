package iss.ibf.pfm_expenses_server.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // bring in value from properties file
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Optional<Integer> redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Value("${spring.data.redis.database}")
    private Optional<Integer> redisDatabase;

    @Bean
    @Scope("singleton")
    public RedisTemplate<String, Object> redisTemplate() {

        // configuration class used for setting up RedisConnection via RedisConnectionFactory by connecting to a single node
        final RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();

        // setup parameters for RedisConnection
        config.setHostName(redisHost);
        config.setPort(redisPort.get());
        if ((!redisUsername.isEmpty() && !redisPassword.isEmpty())) {
            config.setUsername(redisUsername);
            config.setPassword(redisPassword);
            
        }
        config.setDatabase(redisDatabase.get());

        // provide optional configuration elements
        // builder to create a new JedisClientConfiguration
        final JedisClientConfiguration jedisClient = JedisClientConfiguration
                                                        .builder()
                                                        .build();

        // create Jedis based connection
        // support RedisStandaloneConfiguration, RedisSentinelConfiguration, RedisClusterConfiguration
        final JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
        // initialize bean
        jedisFac.afterPropertiesSet();

        // instantiate a RedisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(jedisFac);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        RedisSerializer<Object> objSerializer = new JdkSerializationRedisSerializer(getClass().getClassLoader());
        redisTemplate.setValueSerializer(objSerializer);
        redisTemplate.setHashValueSerializer(objSerializer);


        return redisTemplate;
    }   
    
}

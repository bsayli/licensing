package io.github.bsayli.licensing.config;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Value("${async.pool.core:4}")
  private int core;

  @Value("${async.pool.max:8}")
  private int max;

  @Value("${async.pool.queue:100}")
  private int queue;

  @Value("${async.pool.threadNamePrefix:LicenseService-}")
  private String threadNamePrefix;

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setCorePoolSize(core);
    ex.setMaxPoolSize(max);
    ex.setQueueCapacity(queue);
    ex.setThreadNamePrefix(threadNamePrefix);
    ex.setAllowCoreThreadTimeOut(true);
    ex.initialize();
    return ex;
  }
}

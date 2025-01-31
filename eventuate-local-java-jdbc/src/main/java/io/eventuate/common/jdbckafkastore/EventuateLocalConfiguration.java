package io.eventuate.common.jdbckafkastore;

import io.eventuate.common.jdbc.EventuateCommonJdbcOperations;
import io.eventuate.common.jdbc.EventuateSchemaConfiguration;
import io.eventuate.javaclient.commonimpl.AggregateCrud;
import io.eventuate.javaclient.commonimpl.AggregateEvents;
import io.eventuate.javaclient.commonimpl.SerializedEventDeserializer;
import io.eventuate.javaclient.commonimpl.adapters.AsyncToSyncAggregateEventsAdapter;
import io.eventuate.javaclient.commonimpl.adapters.AsyncToSyncTimeoutOptions;
import io.eventuate.javaclient.commonimpl.adapters.SyncToAsyncAggregateCrudAdapter;
import io.eventuate.javaclient.spring.common.EventuateCommonConfiguration;
import io.eventuate.javaclient.spring.jdbc.EventuateJdbcAccess;
import io.eventuate.common.jdbc.EventuateSchema;
import io.eventuate.messaging.kafka.basic.consumer.EventuateKafkaConsumerConfigurationProperties;
import io.eventuate.messaging.kafka.common.EventuateKafkaConfigurationProperties;
import io.eventuate.messaging.kafka.common.EventuateKafkaPropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Defines the Spring beans for the JDBC-based aggregate store
 */
@Configuration
@EnableTransactionManagement
@Import({EventuateCommonConfiguration.class, EventuateKafkaPropertiesConfiguration.class, EventuateSchemaConfiguration.class})
@EnableConfigurationProperties(EventuateKafkaConsumerConfigurationProperties.class)
public class EventuateLocalConfiguration {

  @Autowired(required=false)
  private SerializedEventDeserializer serializedEventDeserializer;

  @Autowired(required=false)
  private AsyncToSyncTimeoutOptions timeoutOptions;

  @Bean
  public JdbcTemplate jdbcTemplate(DataSource db) {
    return new JdbcTemplate(db);
  }

  @Bean
  public EventuateCommonJdbcOperations eventuateCommonJdbcOperations(JdbcTemplate jdbcTemplate) {
    return new EventuateCommonJdbcOperations(jdbcTemplate);
  }

  @Bean
  public EventuateJdbcAccess eventuateJdbcAccess(EventuateSchema eventuateSchema,
                                                 EventuateCommonJdbcOperations eventuateCommonJdbcOperations,
                                                 JdbcTemplate jdbcTemplate) {
    return new EventuateLocalJdbcAccess(jdbcTemplate, eventuateCommonJdbcOperations, eventuateSchema);
  }

  @Bean
  public EventuateLocalAggregateCrud eventuateLocalAggregateCrud(EventuateJdbcAccess eventuateJdbcAccess) {
    return new EventuateLocalAggregateCrud(eventuateJdbcAccess);
  }

  @Bean
  public AggregateCrud asyncAggregateCrud(io.eventuate.javaclient.commonimpl.sync.AggregateCrud aggregateCrud) {
    return new SyncToAsyncAggregateCrudAdapter(aggregateCrud);
  }


  // Events

  @Bean
  public EventuateKafkaAggregateSubscriptions aggregateEvents(EventuateKafkaConfigurationProperties eventuateLocalAggregateStoreConfiguration,
                                                              EventuateKafkaConsumerConfigurationProperties eventuateKafkaConsumerConfigurationProperties) {
    return new EventuateKafkaAggregateSubscriptions(eventuateLocalAggregateStoreConfiguration, eventuateKafkaConsumerConfigurationProperties);
  }


  @Bean
  public io.eventuate.javaclient.commonimpl.sync.AggregateEvents syncAggregateEvents(AggregateEvents aggregateEvents) {
    AsyncToSyncAggregateEventsAdapter adapter = new AsyncToSyncAggregateEventsAdapter(aggregateEvents);
    if (timeoutOptions != null)
      adapter.setTimeoutOptions(timeoutOptions);
    return adapter;
  }

  // Aggregate Store
  // Why @ConditionalOnMissingBean(EventuateAggregateStore.class)??
}

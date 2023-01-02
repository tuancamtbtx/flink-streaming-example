package vn.example.streaming.flink.connector;

import com.google.common.base.Strings;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Preconditions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import vn.example.streaming.flink.entities.KafkaSourceConfig;

import java.time.Duration;
import java.util.Properties;

public class KafkaSourceConnector {
    private static final Duration DEFAULT_MAX_OUT_ORDER_NESS_INTERVAL = Duration.ofMinutes(15);
    private static final Duration NEGATIVE_IDLENESS_INTERVAL = Duration.ofSeconds(-1L);

    /**
     * Building kafka source builder  with kafka config
     *
     * @return KafkaSourceBuilder
     */
    public static <T> KafkaSourceBuilder getKafkaSourceBuilder(KafkaSourceConfig sourceConfig) {
        Preconditions.checkNotNull(sourceConfig.getTopicName(), "topic name is must not empty");
        String[] topics = sourceConfig.getTopicName().split(",");
        Properties properties = new Properties();
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return KafkaSource.<T>builder()
                .setClientIdPrefix(sourceConfig.getClientIdPrefix())
                .setBootstrapServers(sourceConfig.getBrokers())
                .setGroupId(sourceConfig.getGroupId())
                .setStartingOffsets(sourceConfig.getOffsetsInitializer())
                .setTopics(topics)
                .setProperties(properties)
                .setDeserializer(sourceConfig.getRecordDeserializationSchema());
    }

    /**
     * Get datasource stream with flink factory env and kafka source config
     * param StreamExecutionEnvironment returning streaming flink execution
     *
     * @return DataStreamSource kafka
     */
    public static <T> DataStreamSource<T> getSource(StreamExecutionEnvironment env, KafkaSourceConfig sourceConfig) {
        OffsetsInitializer offsetsInitializer = OffsetsInitializer.latest();
        if (sourceConfig.getInitOffsetAtTimeInMills() > 0) {
            offsetsInitializer = OffsetsInitializer.timestamp(sourceConfig.getInitOffsetAtTimeInMills());
        } else if (sourceConfig.isUseEarliestOffset()) {
            offsetsInitializer = OffsetsInitializer.earliest();
        }
        sourceConfig.setOffsetsInitializer(offsetsInitializer);
        KafkaSourceBuilder kafkaSourceBuilder = getKafkaSourceBuilder(sourceConfig);
        KafkaSource kafkaSource = kafkaSourceBuilder.build();
        return (DataStreamSource<T>) env
                .fromSource(kafkaSource,
                        forBoundedOutOfOrderness(
                                Strings.isNullOrEmpty(sourceConfig.getMaxOutOrdernessInterval())
                                        ? DEFAULT_MAX_OUT_ORDER_NESS_INTERVAL
                                        : Duration.parse(sourceConfig.getMaxOutOrdernessInterval()),
                                Strings.isNullOrEmpty(sourceConfig.getIdleInterval())
                                        ? NEGATIVE_IDLENESS_INTERVAL
                                        : Duration.parse(sourceConfig.getIdleInterval()),
                                sourceConfig.getTimestampAssigner()
                        ),
                        sourceConfig.getSourceName()
                );

    }

    public static <T> WatermarkStrategy<T> forBoundedOutOfOrderness(
            Duration maxOutOfOrderness,
            Duration idlenessDuration,
            SerializableTimestampAssigner<T> timestampAssigner) {
        WatermarkStrategy<T> watermarkStrategy = WatermarkStrategy.<T>forBoundedOutOfOrderness(maxOutOfOrderness)
                .withTimestampAssigner(timestampAssigner);
        if (idlenessDuration != null && !idlenessDuration.isNegative()) {
            watermarkStrategy = watermarkStrategy.withIdleness(idlenessDuration);
        }

        return watermarkStrategy;
    }
}

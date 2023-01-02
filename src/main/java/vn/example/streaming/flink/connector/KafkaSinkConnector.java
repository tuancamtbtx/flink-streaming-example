package vn.example.streaming.flink.connector;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import vn.example.streaming.flink.entities.KafkaSinkConfig;

import java.io.Serializable;

public class KafkaSinkConnector<T> implements Serializable {
    public static KafkaSink<String> getSink(KafkaSinkConfig sinkConfig) {
        return KafkaSink.<String>builder()
                .setBootstrapServers(sinkConfig.getBrokers())
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic(sinkConfig.getTopicName())
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .build();
    }
}

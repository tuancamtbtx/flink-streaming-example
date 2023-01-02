package vn.example.streaming.flink.entities.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;

import java.io.Serializable;

@Data
@Builder
@Setter
@Getter
@JsonAutoDetect
public class KafkaSourceConfig<T> implements Serializable {
    @JsonProperty("brokers")
    @SerializedName("brokers")
    String brokers;

    @JsonProperty("topic_name")
    @SerializedName("topic_name")
    String topicName;

    @JsonProperty("client_id_prefix")
    @SerializedName("client_id_prefix")
    String clientIdPrefix;

    @JsonProperty("group_id")
    @SerializedName("group_id")
    String groupId;

    @JsonProperty("init_offset_at_timestamp")
    @SerializedName("init_offset_at_timestamp")
    long initOffsetAtTimeInMills;

    @JsonProperty("use_earliest_offset")
    @SerializedName("use_earliest_offset")
    boolean useEarliestOffset;

    @JsonProperty("offsets_initializer")
    @SerializedName("offsets_initializer")
    OffsetsInitializer offsetsInitializer;

    @JsonProperty("timestamp_assigner")
    @SerializedName("timestamp_assigner")
    SerializableTimestampAssigner<T> timestampAssigner;

    @JsonProperty("record_deserialization_schema")
    @SerializedName("record_deserialization_schema")
    KafkaRecordDeserializationSchema<T> recordDeserializationSchema;

    @JsonProperty("source_name")
    @SerializedName("source_name")
    String sourceName;

    @JsonProperty("idle_interval")
    @SerializedName("idle_interval")
    private String idleInterval;

    @JsonProperty("max_OutOrderness_Interval")
    @SerializedName("max_OutOrderness_Interval")
    private String maxOutOrdernessInterval;
}

package vn.example.streaming.flink.entities.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.io.Serializable;

@Data
@Builder
@Setter
@Getter
@JsonAutoDetect
public class KafkaSinkConfig<T> implements Serializable {
    @JsonProperty("sink_name")
    @SerializedName("sink_name")
    String sinkName;

    @JsonProperty("brokers")
    @SerializedName("brokers")
    String brokers;
    @JsonProperty("topic_name")
    @SerializedName("topic_name")
    String topicName;

    @JsonProperty("group_id")
    @SerializedName("group_id")
    String groupId;

    @JsonProperty("key_serialization_schema")
    @SerializedName("key_serialization_schema")
    SerializationSchema<? super T> keySerializationSchema;

    @JsonProperty("value_serialization_schema")
    @SerializedName("value_serialization_schema")
    SerializationSchema<T> valueSerializationSchema;
}

package vn.example.streaming.flink.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Builder
@Setter
@Getter
@JsonAutoDetect
public class ScyllaSinkConfig implements Serializable {

    @JsonProperty("keyspace")
    @SerializedName("keyspace")
    String keyspace;

    @JsonProperty("host")
    @SerializedName("hosts")
    String hosts;

    @JsonProperty("username")
    @SerializedName("username")
    String username;

    @JsonProperty("password")
    @SerializedName("password")
    String password;

    @JsonProperty("port")
    @SerializedName("port")
    int port;
}
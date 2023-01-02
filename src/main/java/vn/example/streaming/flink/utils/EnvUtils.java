package vn.example.streaming.flink.utils;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EnvUtils {
    public static Dotenv dotenv = Dotenv.configure().load();

    public static String getEnvironmentEntry(String key) {
        return dotenv.get(key);
    }
}

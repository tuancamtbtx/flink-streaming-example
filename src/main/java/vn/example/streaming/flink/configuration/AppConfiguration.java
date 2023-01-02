package vn.example.streaming.flink.configuration;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.java.utils.ParameterTool;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AppConfiguration implements Serializable {
    final Config config;
    final String hiddenSecret = "***";

    public AppConfiguration(Config config) {
        this.config = config;
    }

    public static AppConfiguration fromRuntimeContext(RuntimeContext ctx) {
        return fromExecutionConfig(ctx.getExecutionConfig());
    }

    public static AppConfiguration fromExecutionConfig(ExecutionConfig config) {
        ParameterTool tool = (ParameterTool) config.getGlobalJobParameters();
        return AppConfiguration.fromProperties(tool.getProperties());
    }

    /**
     * WARN: This is not thread safe
     * but this is still true in multi-threaded env, cause it's read-only config
     * <p>
     * If you get config from ProcessFunction better use {@link #fromExecutionConfig(ExecutionConfig)}
     * </p>
     */
    public static AppConfiguration getDefaultConfig() {
        return AppConfigHolder.INSTANCE;
    }

    public static AppConfiguration fromProperties(Properties properties) {
        try {
            return new AppConfiguration(ConfigFactory.parseProperties(properties)
                    .withOnlyPath(ConfigConstants.CONFIG_PREFIX));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public int getInt(String key, int defaultValue) {
        try {
            return config.getInt(key);
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    public String getString(String key) {
        return config.getString(key);
    }

    public Long getLong(String key, Long defaultValue) {
        try {
            return config.getLong(key);
        } catch (Exception ex) {
        }
        return defaultValue;
    }

    public String getString(String key, String defaultValue) {
        try {
            return config.getString(key);
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return config.getBoolean(key);
        } catch (Exception ex) {
        }
        return defaultValue;
    }

    public List<String> getList(String key) {
        return config.getStringList(key);
    }

    private void flattenToMap(String key, Map<String, String> map) {
        ConfigValue value = this.config.getValue(key);
        if (ConfigValueType.OBJECT.equals(value.valueType())) {
            Config nested = this.config.getConfig(key);
            for (String nestedKey : nested.root().keySet()) {
                flattenToMap(key + "." + nestedKey, map);
            }
        } else if (value.valueType().equals(ConfigValueType.LIST)) {
            List<String> strings = this.config.getStringList(key);
            for (int i = 0; i < strings.size(); ++i) {
                map.put(key + "." + i, strings.get(i));
            }
        } else {
            map.put(key, value.unwrapped().toString());
        }
    }

    private boolean isHiddenConfig(String key) {
        return !Strings.isNullOrEmpty(key) && (key.contains("password")
                || key.contains("host"));
    }

    public Map<String, String> toMap() {
        Map<String, String> mmap = new HashMap<>();
        this.config.root().keySet().forEach(key -> flattenToMap(key, mmap));
        return mmap;
    }

    public String pretty() {
        StringBuilder sb = new StringBuilder();
        toMap().forEach((key, value) -> {
            sb.append("\t\t").append(key).append(": ");
            if (isHiddenConfig(key)) {
                sb.append(hiddenSecret);
            } else {
                sb.append(value);
            }
            sb.append("\n");
        });

        return sb.toString();
    }

    @Override
    public String toString() {
        return this.config.toString();
    }

    private static class AppConfigHolder {

        private static final AppConfiguration INSTANCE = AppConfiguration.fromProperties(System.getProperties());
    }
}

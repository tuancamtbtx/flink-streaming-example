package vn.example.streaming.flink.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class TGlobalConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(TGlobalConfiguration.class);

    private static String getConfigDir() {
        String configDir = System.getenv(ConfigConstants.ENV_APPLICATION_CONF_DIR);
        if (configDir == null) {
            configDir = System.getProperty("user.dir") + "/" + "config/local.conf";
        }
        return configDir;
    }

    public static AppConfiguration loadConfiguration(Map<String, String> dynamicProperties) {
        return loadConfiguration(getConfigDir(), dynamicProperties);
    }

    public static AppConfiguration loadConfiguration(final String configDir, Map<String, String> dynamicProperties) {
        LOGGER.info("[loadConfiguration]: load config from file: " + configDir);
        Config reference = ConfigFactory.load("trackity-reference.conf");
        Config config = ConfigFactory.parseMap(dynamicProperties)
                .withFallback(ConfigFactory.parseFile(new File(configDir)))
                .withFallback(reference)
                .resolve()
                .withOnlyPath(ConfigConstants.CONFIG_PREFIX);

        AppConfiguration appConfig = new AppConfiguration(config);
        appConfig.toMap().forEach(System::setProperty);
        return appConfig;
    }
}

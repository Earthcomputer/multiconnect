package net.earthcomputer.multiconnect.provider;

import com.viaversion.viaversion.configuration.AbstractViaConfig;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MulticonnectViaConfig extends AbstractViaConfig {
    public MulticonnectViaConfig(File configFile) {
        super(configFile);
        reloadConfig();
    }

    @Override
    public URL getDefaultConfigURL() {
        return getClass().getClassLoader().getResource("assets/multiconnect/via_config.yml");
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return Collections.emptyList();
    }
}

package net.earthcomputer.multiconnect.protocols.v1_8;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class SoundData_1_8 {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static SoundData_1_8 INSTANCE;
    public static SoundData_1_8 getInstance() {
        // TODO: rewrite for via?
        if (INSTANCE == null) {
            try {
                INSTANCE = new Gson().fromJson(
                        new InputStreamReader(SoundData_1_8.class.getResourceAsStream("/protocol_data/sounds-1.8.json"), StandardCharsets.UTF_8),
                        SoundData_1_8.class);
            } catch (JsonParseException e) {
                LOGGER.error("Unable to load 1.8 sound data", e);
                INSTANCE = new SoundData_1_8();
            }
        }
        return INSTANCE;
    }

    private SoundData_1_8() {}

    private Map<String, SoundValue> sounds = new HashMap<>();

    public Collection<String> getAllSounds() {
        return Collections.unmodifiableSet(sounds.keySet());
    }

    public SoundSource getCategory(String sound) {
        SoundValue value = sounds.get(sound);
        if (value == null) return null;
        return value.getCategoryEnum();
    }

    public SoundEvent getSoundEvent(String sound) {
        SoundValue value = sounds.get(sound);
        if (value == null) return null;
        return value.getSoundEvent();
    }

    private static class SoundValue {
        private String category = SoundSource.MASTER.getName();
        private String map;

        private transient SoundSource categoryEnum;
        public SoundSource getCategoryEnum() {
            if (categoryEnum == null) {
                for (SoundSource ctgy : SoundSource.values()) {
                    if (ctgy.getName().equals(category)) {
                        categoryEnum = ctgy;
                        break;
                    }
                }
            }
            return categoryEnum;
        }

        private transient SoundEvent soundEvent;
        public SoundEvent getSoundEvent() {
            if (soundEvent == null) {
                ResourceLocation id = ResourceLocation.tryParse(map);
                if (id != null) {
                    soundEvent = Registry.SOUND_EVENT.getOptional(id).orElse(null);
                }
            }
            return soundEvent;
        }
    }
}

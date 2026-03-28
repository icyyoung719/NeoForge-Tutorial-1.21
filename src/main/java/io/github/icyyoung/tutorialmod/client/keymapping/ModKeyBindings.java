package io.github.icyyoung.tutorialmod.client.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.icyyoung.tutorialmod.TutorialMod;
import net.minecraft.client.KeyMapping;

public class ModKeyBindings {
    public static final String KEY_CATEGORY_TUTORIALMOD = "key.category." + TutorialMod.MOD_ID;
    
    public static final KeyMapping MINIMAP_TOGGLE_KEY = new KeyMapping(
            "key." + TutorialMod.MOD_ID + ".minimap_toggle",
            InputConstants.KEY_N,
            KEY_CATEGORY_TUTORIALMOD
    );

    public static final KeyMapping FULL_MAP_KEY = new KeyMapping(
            "key." + TutorialMod.MOD_ID + ".full_map",
            InputConstants.KEY_M,
            KEY_CATEGORY_TUTORIALMOD
    );
}
package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IMixinScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Screen.class)
public abstract class MixinScreen implements IMixinScreen {

    @Shadow
    protected abstract <T extends AbstractButtonWidget> T addButton(T p_addButton_1_);

    @Override
    public void addGuiButtonToList(AbstractButtonWidget button) {
        addButton(button);
    }

}


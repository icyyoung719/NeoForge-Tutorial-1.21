package io.github.icyyoung.tutorialmod.client.screen;

import io.github.icyyoung.tutorialmod.client.music.MusicManager;
import io.github.icyyoung.tutorialmod.client.music.MusicTrack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MusicPlayerScreen extends Screen {

    public MusicPlayerScreen() {
        super(Component.literal("Background Music Player"));
    }

    @Override
    protected void init() {
        super.init();
        MusicManager.getInstance().discoverTracks();
        List<MusicTrack> tracks = MusicManager.getInstance().getTracks();

        int yOffset = 40;
        for (MusicTrack track : tracks) {
            this.addRenderableWidget(Button.builder(Component.literal("Play: " + track.getTitle()), b -> {
                MusicManager.getInstance().playTrack(track);
            }).bounds(this.width / 2 - 100, yOffset, 200, 20).build());
            yOffset += 25;
        }

        int controlsY = this.height - 40;
        this.addRenderableWidget(Button.builder(Component.literal("Stop Music"), b -> {
            MusicManager.getInstance().stop();
        }).bounds(this.width / 2 - 105, controlsY, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Volume --"), b -> {
            MusicManager.getInstance().adjustVolume(-0.1f);
        }).bounds(this.width / 2 + 5, controlsY, 50, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Volume ++"), b -> {
            MusicManager.getInstance().adjustVolume(0.1f);
        }).bounds(this.width / 2 + 60, controlsY, 50, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        
        MusicTrack current = MusicManager.getInstance().getCurrentTrack();
        if (current != null) {
            guiGraphics.drawCenteredString(this.font, Component.literal("Now Playing: " + current.getTitle() + " by " + current.getArtist()), this.width / 2, 25, 0x00FF00);
        }
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

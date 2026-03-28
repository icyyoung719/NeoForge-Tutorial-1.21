package io.github.icyyoung.tutorialmod.client.music;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import javazoom.jl.player.Player;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class MusicManager {
    private static final MusicManager INSTANCE = new MusicManager();
    public static MusicManager getInstance() { return INSTANCE; }

    private List<MusicTrack> tracks = new ArrayList<>();
    private Player player;
    private Thread playerThread;
    private MusicTrack currentTrack;
    private float volume = 1f;

    private MusicManager() {}

    public void adjustVolume(float delta) {
        this.volume += delta;
        if (this.volume < 0f) this.volume = 0f;
        if (this.volume > 1f) this.volume = 1f;
        // In a full implementation, you would apply this to the underlying audio device or Clip's MASTER_GAIN.
        // JLayer does not provide direct volume adjustment natively without custom AudioDevice.
    }

    public void discoverTracks() {
        tracks.clear();
        File musicDir = new File(Minecraft.getInstance().gameDirectory, "tutorialmod/music");
        if (!musicDir.exists()) {
            musicDir.mkdirs();
        }

        File[] files = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (files != null) {
            for (File file : files) {
                try {
                    Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                    String title = file.getName();
                    String artist = "Unknown Artist";

                    if (mp3file.hasId3v2Tag()) {
                        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                        title = id3v2Tag.getTitle() != null ? id3v2Tag.getTitle() : title;
                        artist = id3v2Tag.getArtist() != null ? id3v2Tag.getArtist() : artist;
                    } else if (mp3file.hasId3v1Tag()) {
                        ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                        title = id3v1Tag.getTitle() != null ? id3v1Tag.getTitle() : title;
                        artist = id3v1Tag.getArtist() != null ? id3v1Tag.getArtist() : artist;
                    }
                    
                    tracks.add(new MusicTrack(file.getAbsolutePath(), title, artist));
                } catch (Exception e) {
                    System.err.println("Could not parse MP3 tag for " + file.getName());
                    tracks.add(new MusicTrack(file.getAbsolutePath(), file.getName(), "Unknown Artist"));
                }
            }
        }
    }

    public List<MusicTrack> getTracks() {
        return tracks;
    }

    public void playTrack(MusicTrack track) {
        stop();
        currentTrack = track;
        playerThread = new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(track.getFilePath());
                player = new Player(fis);
                player.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        playerThread.setDaemon(true);
        playerThread.start();
    }

    public void stop() {
        if (player != null) {
            player.close();
            player = null;
        }
        if (playerThread != null) {
            playerThread.interrupt();
            playerThread = null;
        }
        currentTrack = null;
    }

    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }
}

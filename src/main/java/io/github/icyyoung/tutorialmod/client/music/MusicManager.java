package io.github.icyyoung.tutorialmod.client.music;

import javazoom.jl.player.JavaSoundAudioDevice;
import javazoom.jl.player.Player;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

public class MusicManager {
    private static final Field SOURCE_LINE_FIELD = resolveSourceLineField();

    private static MusicManager INSTANCE;
    private Player player;
    private Thread playerThread;
    private FloatControl volumeControl;
    private float currentVolume = 0.5f;

    // Single source of truth for visible/default order.
    private final List<MusicTrack> playlist = new ArrayList<>();
    // Order reference snapshot from last load; used to restore ordering when shuffle turns off.
    private final List<MusicTrack> orderedReference = new ArrayList<>();
    // Shuffle state is modeled as a queue instead of replacing playlist.
    private final Deque<MusicTrack> shuffleQueue = new ArrayDeque<>();
    private final Deque<MusicTrack> shuffleHistory = new ArrayDeque<>();
    private MusicTrack currentTrack;
    private long playbackStartMs;
    private long pausedElapsedMs;
    
    private boolean isLooping = false;
    private boolean isShuffle = false;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private boolean showNowPlayingOverlay = true;
    
    private MusicManager() {
        loadTracks();
    }
    
    public static MusicManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MusicManager();
        }
        return INSTANCE;
    }
    
    public synchronized void loadTracks() {
        String currentFileName = currentTrack != null ? currentTrack.getFileName() : null;
        boolean wasPlaying = isPlaying;
        boolean wasPaused = isPaused;
        long resumeElapsedMs = getCurrentElapsedMillis();

        stopPlaybackOnly();
        playlist.clear();
        orderedReference.clear();
        shuffleQueue.clear();
        shuffleHistory.clear();

        File musicDir = new File(Minecraft.getInstance().gameDirectory, "tutorialmod/music");
        if (!musicDir.exists()) {
            musicDir.mkdirs();
        }
        File[] files = musicDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
        if (files != null) {
            for (File file : files) {
                MusicTrack track = new MusicTrack(file);
                playlist.add(track);
                orderedReference.add(track);
            }
        }

        MusicTrack matchedTrack = null;
        if (currentFileName != null) {
            for (MusicTrack track : playlist) {
                if (currentFileName.equals(track.getFileName())) {
                    matchedTrack = track;
                    break;
                }
            }
        }

        if (matchedTrack != null) {
            currentTrack = matchedTrack;
            long safeResumeMs = clampResumeMs(matchedTrack, resumeElapsedMs);
            if (wasPlaying) {
                playTrackInternal(matchedTrack, safeResumeMs);
            } else {
                isPaused = wasPaused;
                pausedElapsedMs = wasPaused ? safeResumeMs : 0L;
                isPlaying = false;
                playbackStartMs = 0L;
            }
        } else {
            currentTrack = null;
            isPlaying = false;
            isPaused = false;
            pausedElapsedMs = 0L;
            playbackStartMs = 0L;
        }

        if (isShuffle) {
            rebuildShuffleQueue(currentTrack);
        }
    }
    
    public synchronized List<MusicTrack> getPlaylist() {
        return playlist;
    }
    
    public synchronized void playTrack(int index) {
        if (index < 0 || index >= playlist.size()) return;
        MusicTrack selected = playlist.get(index);
        if (isShuffle) {
            shuffleHistory.clear();
            rebuildShuffleQueue(selected);
        }
        pausedElapsedMs = 0L;
        isPaused = false;
        playTrackInternal(selected, 0L);
    }

    private synchronized void playTrackInternal(MusicTrack track, long startMs) {
        stopPlaybackOnly();
        Minecraft.getInstance().getMusicManager().stopPlaying();
        this.currentTrack = track;
        this.isPlaying = true;
        this.isPaused = false;
        long safeStartMs = clampResumeMs(track, Math.max(0L, startMs));
        this.pausedElapsedMs = safeStartMs;
        this.playbackStartMs = System.currentTimeMillis() - safeStartMs;
        
        playerThread = new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(track.getFile())) {
                if (safeStartMs > 0L) {
                    long skipBytes = estimateSkipBytes(track, safeStartMs);
                    skipFully(fis, skipBytes);
                }

                JavaSoundAudioDevice device = new JavaSoundAudioDevice();
                Player localPlayer = new Player(fis, device);
                
                if (!isPlaying || Thread.currentThread() != playerThread) {
                    localPlayer.close();
                    return;
                }
                
                player = localPlayer;
                
                captureVolumeControl(device);
                
                player.play();
                
                if (shouldHandleTrackEndForCurrentThread()) {
                    onTrackEnd();
                }
            } catch (Exception ignored) {
                if (shouldHandleTrackEndForCurrentThread()) {
                    onTrackEnd();
                }
            }
        }, "tutorialmod-music-player");
        playerThread.setDaemon(true);
        playerThread.setPriority(Thread.MIN_PRIORITY);
        playerThread.start();
    }
    
    private synchronized void onTrackEnd() {
        if (!isPlaying) return;

        if (playlist.isEmpty()) {
            isPlaying = false;
            player = null;
            currentTrack = null;
            return;
        }

        if (isLooping) {
            if (currentTrack != null) {
                pausedElapsedMs = 0L;
                playTrackInternal(currentTrack, 0L);
            }
        } else {
            playNext();
        }
    }
    
    public synchronized void stop() {
        stopPlaybackOnly();
        isPaused = false;
        pausedElapsedMs = 0L;
        currentTrack = null;
    }

    private synchronized void stopPlaybackOnly() {
        isPlaying = false;
        if (player != null) {
            player.close();
            player = null;
        }
        if (playerThread != null) {
            playerThread.interrupt();
            playerThread = null;
        }
        volumeControl = null;
        playbackStartMs = 0L;
    }

    public synchronized void pause() {
        if (!isPlaying || currentTrack == null) {
            return;
        }

        pausedElapsedMs = getCurrentElapsedMillis();
        isPaused = true;
        stopPlaybackOnly();
    }

    public synchronized void resume() {
        if (!isPaused || currentTrack == null) {
            return;
        }

        playTrackInternal(currentTrack, pausedElapsedMs);
    }

    public synchronized void togglePlayPause() {
        if (isPlaying) {
            pause();
        } else if (isPaused) {
            resume();
        } else if (currentTrack != null) {
            playTrackInternal(currentTrack, 0L);
        } else if (!playlist.isEmpty()) {
            playTrack(0);
        }
    }
    
    public synchronized void playNext() {
        if (playlist.isEmpty()) return;

        if (isShuffle) {
            if (currentTrack != null) {
                shuffleHistory.push(currentTrack);
            }

            MusicTrack nextTrack = shuffleQueue.pollFirst();
            if (nextTrack == null) {
                rebuildShuffleQueue(currentTrack);
                nextTrack = shuffleQueue.pollFirst();
            }
            if (nextTrack == null && !playlist.isEmpty()) {
                nextTrack = playlist.get(0);
            }
            if (nextTrack != null) {
                pausedElapsedMs = 0L;
                isPaused = false;
                playTrackInternal(nextTrack, 0L);
            }
        } else {
            int nextIndex;
            int currentIndex = playlist.indexOf(currentTrack);
            if (currentIndex < 0) {
                nextIndex = 0;
            } else {
                nextIndex = (currentIndex + 1) % playlist.size();
            }
            pausedElapsedMs = 0L;
            isPaused = false;
            playTrackInternal(playlist.get(nextIndex), 0L);
        }
    }
    
    public synchronized void playPrev() {
        if (playlist.isEmpty()) return;

        if (isShuffle) {
            MusicTrack prevTrack = shuffleHistory.pollFirst();
            if (prevTrack != null) {
                if (currentTrack != null) {
                    shuffleQueue.addFirst(currentTrack);
                }
                pausedElapsedMs = 0L;
                isPaused = false;
                playTrackInternal(prevTrack, 0L);
            } else if (currentTrack == null) {
                pausedElapsedMs = 0L;
                isPaused = false;
                playTrackInternal(playlist.get(playlist.size() - 1), 0L);
            }
        } else {
            int currentIndex = playlist.indexOf(currentTrack);
            int prevIndex;
            if (currentIndex < 0) {
                prevIndex = playlist.size() - 1;
            } else {
                prevIndex = currentIndex - 1;
                if (prevIndex < 0) {
                    prevIndex = playlist.size() - 1;
                }
            }
            pausedElapsedMs = 0L;
            isPaused = false;
            playTrackInternal(playlist.get(prevIndex), 0L);
        }
    }
    
    public void setVolume(float volume) {
        this.currentVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateVolumeInternal();
    }
    
    private void updateVolumeInternal() {
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float range = max - min;
            // logarithmic scale approximation could be better, but linear is ok
            float gain = (range * currentVolume) + min;
            if (currentVolume == 0.0f) {
                gain = min;
            }
            volumeControl.setValue(gain);
        }
    }
    
    public float getVolume() {
        return currentVolume;
    }
    
    public boolean isLooping() { return isLooping; }
    public void setLooping(boolean looping) { this.isLooping = looping; }
    
    public boolean isShuffle() { return isShuffle; }
    public synchronized void toggleShuffle() {
        this.isShuffle = !this.isShuffle;

        if (this.isShuffle) {
            shuffleHistory.clear();
            rebuildShuffleQueue(currentTrack);
        } else {
            // Restore deterministic order from snapshot without replacing list object.
            playlist.clear();
            playlist.addAll(orderedReference);
            shuffleQueue.clear();
            shuffleHistory.clear();
        }
    }
    
    public MusicTrack getCurrentTrack() {
        return currentTrack;
    }

    public int getCurrentTrackIndex() {
        return playlist.indexOf(currentTrack);
    }
    
    public boolean isPlaying() { return isPlaying; }
    public boolean isPaused() { return isPaused; }
    public boolean isNowPlayingOverlayEnabled() { return showNowPlayingOverlay; }
    public void setNowPlayingOverlayEnabled(boolean enabled) { this.showNowPlayingOverlay = enabled; }
    public void toggleNowPlayingOverlay() { this.showNowPlayingOverlay = !this.showNowPlayingOverlay; }

    public synchronized int getCurrentElapsedSeconds() {
        int elapsed = (int) (getCurrentElapsedMillis() / 1000L);
        if (currentTrack == null) {
            return elapsed;
        }
        int duration = currentTrack.getDurationSeconds();
        if (duration > 0) {
            return Math.min(elapsed, duration);
        }
        return elapsed;
    }

    private synchronized long getCurrentElapsedMillis() {
        if (isPaused && currentTrack != null) {
            return pausedElapsedMs;
        }

        if (currentTrack == null || playbackStartMs <= 0L) {
            return 0L;
        }

        long elapsed = Math.max(0L, System.currentTimeMillis() - playbackStartMs);
        long maxDurationMs = Math.max(0L, currentTrack.getDurationSeconds() * 1000L);
        if (maxDurationMs > 0L) {
            return Math.min(elapsed, maxDurationMs);
        }
        return elapsed;
    }

    private static Field resolveSourceLineField() {
        try {
            Field field = JavaSoundAudioDevice.class.getDeclaredField("source");
            field.setAccessible(true);
            return field;
        } catch (Exception ignored) {
            return null;
        }
    }

    private void captureVolumeControl(JavaSoundAudioDevice device) {
        if (SOURCE_LINE_FIELD == null) {
            return;
        }

        Thread inspector = new Thread(() -> {
            for (int i = 0; i < 20 && isPlaying; i++) {
                try {
                    Thread.sleep(25);
                    SourceDataLine sourceDataLine = (SourceDataLine) SOURCE_LINE_FIELD.get(device);
                    if (sourceDataLine != null && sourceDataLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
                        updateVolumeInternal();
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (IllegalAccessException ignored) {
                    return;
                }
            }
        }, "tutorialmod-music-volume");

        inspector.setDaemon(true);
        inspector.setPriority(Thread.MIN_PRIORITY);
        inspector.start();
    }

    private void rebuildShuffleQueue(MusicTrack exclude) {
        shuffleQueue.clear();

        List<MusicTrack> candidates = new ArrayList<>(playlist);
        if (exclude != null) {
            candidates.remove(exclude);
        }

        Collections.shuffle(candidates, new Random());
        for (MusicTrack track : candidates) {
            shuffleQueue.addLast(track);
        }
    }

    private synchronized boolean shouldHandleTrackEndForCurrentThread() {
        return isPlaying && Thread.currentThread() == playerThread;
    }

    private long estimateSkipBytes(MusicTrack track, long startMs) {
        long durationMs = Math.max(0L, track.getDurationSeconds() * 1000L);
        if (durationMs <= 0L) {
            return 0L;
        }

        long fileLength = track.getFile().length();
        if (fileLength <= 0L) {
            return 0L;
        }

        long cappedStartMs = Math.min(startMs, durationMs - 1L);
        return (fileLength * cappedStartMs) / durationMs;
    }

    private long clampResumeMs(MusicTrack track, long requestedMs) {
        long durationMs = Math.max(0L, track.getDurationSeconds() * 1000L);
        if (durationMs <= 1L) {
            return Math.max(0L, requestedMs);
        }
        return Math.max(0L, Math.min(requestedMs, durationMs - 1L));
    }

    private void skipFully(FileInputStream stream, long bytesToSkip) throws IOException {
        long remaining = bytesToSkip;
        while (remaining > 0) {
            long skipped = stream.skip(remaining);
            if (skipped <= 0) {
                break;
            }
            remaining -= skipped;
        }
    }
}

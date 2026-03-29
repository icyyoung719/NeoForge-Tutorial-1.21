package io.github.icyyoung.tutorialmod.client.music;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import java.io.File;

public class MusicTrack {
    private final File file;
    private final String fileName;
    private final String title;
    private final String artist;
    private final String album;
    private final int durationSeconds;
    private final byte[] coverArt;

    public MusicTrack(File file) {
        this.file = file;
        this.fileName = file.getName();
        String resolvedTitle = stripMp3Extension(fileName);
        String resolvedArtist = "Unknown Artist";
        String resolvedAlbum = "Unknown Album";
        int resolvedDurationSeconds = 0;
        byte[] resolvedCoverArt = null;
        
        try {
            Mp3File mp3file = new Mp3File(file.getAbsolutePath());
            resolvedDurationSeconds = (int) Math.max(0, mp3file.getLengthInSeconds());
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                resolvedTitle = pickFirstNonBlank(
                    id3v2Tag.getTitle(),
                    resolvedTitle
                );
                resolvedArtist = pickFirstNonBlank(
                    id3v2Tag.getArtist(),
                    id3v2Tag.getComposer(),
                    resolvedArtist
                );
                resolvedAlbum = pickFirstNonBlank(id3v2Tag.getAlbum(), resolvedAlbum);
                resolvedCoverArt = id3v2Tag.getAlbumImage();
            } else if (mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                resolvedTitle = pickFirstNonBlank(id3v1Tag.getTitle(), resolvedTitle);
                resolvedArtist = pickFirstNonBlank(id3v1Tag.getArtist(), resolvedArtist);
                resolvedAlbum = pickFirstNonBlank(id3v1Tag.getAlbum(), resolvedAlbum);
            }
        } catch (Exception ignored) {
        }

        this.title = sanitizeTagValue(resolvedTitle, stripMp3Extension(fileName));
        this.artist = sanitizeTagValue(resolvedArtist, "Unknown Artist");
        this.album = sanitizeTagValue(resolvedAlbum, "Unknown Album");
        this.durationSeconds = resolvedDurationSeconds;
        this.coverArt = resolvedCoverArt;
    }

    private String stripMp3Extension(String input) {
        if (input == null) {
            return "Unknown";
        }
        String lower = input.toLowerCase();
        if (lower.endsWith(".mp3")) {
            return input.substring(0, input.length() - 4);
        }
        return input;
    }

    private String sanitizeTagValue(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String sanitized = value.replace("\u0000", "").trim();
        return sanitized.isEmpty() ? fallback : sanitized;
    }

    private String pickFirstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    public File getFile() { return file; }
    public String getFileName() { return fileName; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public int getDurationSeconds() { return durationSeconds; }
    public byte[] getCoverArt() { return coverArt; }
}

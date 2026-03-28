package io.github.icyyoung.tutorialmod.client.music;

public class MusicTrack {
    private final String filePath;
    private final String title;
    private final String artist;

    public MusicTrack(String filePath, String title, String artist) {
        this.filePath = filePath;
        this.title = title;
        this.artist = artist;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}

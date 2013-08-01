import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class VideoTest {
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectInvalidBaseUrl() {
        new Video(new File(""), "hi!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectMissingTrailingSlash() {
        new Video(new File(""), "http://missing-trailing.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNonMov() {
        new Video(new File("something.jpg"), "http://domain.com/");
    }

    @Test
    public void shouldKnowItsPaths() {
        Video video = new Video(new File("/some/where/video.mov"), "https://mon.bucket.com/");

        assertEquals(video.getInputKey(), "videos/video.mov");
        assertEquals(video.getInputUrl(), "https://mon.bucket.com/videos/video.mov");
        assertEquals(video.getOutputKeyH264(), "videos/video.h264");
        assertEquals(video.getOutputUrlH264(), "https://mon.bucket.com/videos/video.h264");
        assertEquals(video.getOutputKeyWebM(), "videos/video.webm");
        assertEquals(video.getOutputUrlWebM(), "https://mon.bucket.com/videos/video.webm");
    }
}

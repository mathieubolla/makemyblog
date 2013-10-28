import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput;
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest;
import com.amazonaws.services.elastictranscoder.model.JobInput;

import java.io.File;

public class VideoService {
    private final AmazonElasticTranscoder transcoder;
    private final StorageService storage;
    private final String presetIdH264;
    private final String presetIdWebM;
    private final String presetIdSDH264;
    private final String presetIdSDWebM;
    private final String pipelineId;

    public VideoService(AmazonElasticTranscoder transcoder, StorageService storage, String presetIdH264, String presetIdWebM, String presetIdSDH264, String presetIdSDWebM, String pipelineId) {
        this.transcoder = transcoder;
        this.storage = storage;
        this.presetIdH264 = presetIdH264;
        this.presetIdWebM = presetIdWebM;
        this.presetIdSDH264 = presetIdSDH264;
        this.presetIdSDWebM = presetIdSDWebM;
        this.pipelineId = pipelineId;
    }

    public Video transcode(File input, String baseUrl) {
        Video video = new Video(input, baseUrl);
        if (!storage.exists(video.getInputKey(), input)) {
            storage.sendPrivate(input, video.getInputKey(), null);
        }

        if (!storage.exists(video.getOutputKeyH264())) {
            sendJob(video.getInputKey(), video.getOutputKeyH264(), presetIdH264);
        }

        if (!storage.exists(video.getOutputKeyWebM())) {
            sendJob(video.getInputKey(), video.getOutputKeyWebM(), presetIdWebM);
        }

        if (!storage.exists(video.getOutputKeySDH264())) {
            sendJob(video.getInputKey(), video.getOutputKeySDH264(), presetIdSDH264);
        }

        if (!storage.exists(video.getOutputKeySDWebM())) {
            sendJob(video.getInputKey(), video.getOutputKeySDWebM(), presetIdSDWebM);
        }

        return video;
    }

    private void sendJob(String inputKey, String outputKey, String presetId) {
        CreateJobRequest createJobRequest = new CreateJobRequest().withInput(
                new JobInput()
                        .withKey(inputKey)
                        .withAspectRatio("auto")
                        .withContainer("auto")
                        .withResolution("auto")
                        .withFrameRate("auto")
                        .withInterlaced("auto")
        ).withOutput(
                new CreateJobOutput()
                        .withKey(outputKey)
                        .withPresetId(presetId)
                        .withRotate("0")
                        .withThumbnailPattern(outputKey+"{count}")
        ).withPipelineId(pipelineId);

        transcoder.createJob(createJobRequest);
    }
}

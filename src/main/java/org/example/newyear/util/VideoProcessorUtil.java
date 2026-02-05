package org.example.newyear.util;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 视频处理工具类（基于JavaCV）
 * 支持视频拼接、音频拼接、音视频合成
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Component
public class VideoProcessorUtil {

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 异步拼接多个视频
     *
     * @param videoUrls 视频URL列表
     * @return 拼接后的视频文件路径
     */
    public CompletableFuture<String> concatVideosAsync(List<String> videoUrls) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return concatVideos(videoUrls);
            } catch (Exception e) {
                log.error("视频拼接失败", e);
                throw new RuntimeException("视频拼接失败", e);
            }
        }, executorService);
    }

    /**
     * 拼接多个视频
     *
     * @param videoUrls 视频URL列表
     * @return 拼接后的视频文件路径
     */
    public String concatVideos(List<String> videoUrls) throws Exception {
        if (videoUrls == null || videoUrls.isEmpty()) {
            throw new IllegalArgumentException("视频URL列表不能为空");
        }

        log.info("开始拼接视频: {} 个视频", videoUrls.size());

        String outputPath = "output/videos/concat_" + UUID.randomUUID() + ".mp4";
        Files.createDirectories(Paths.get(outputPath).getParent());

        try {
            // 方案：使用FFmpeg的concat demuxer
            List<String> concatList = new ArrayList<>();
            for (String url : videoUrls) {
                concatList.add("file '" + url + "'");
            }

            // 创建临时concat列表文件
            Path listFile = Files.createTempFile("ffmpeg_list_", ".txt");
            Files.write(listFile, concatList);

            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(listFile.toString());
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath,
                    grabber.getImageWidth(),
                    grabber.getImageHeight(),
                    grabber.getAudioChannels());

            recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setSampleFormat(grabber.getSampleFormat());
            recorder.setSampleRate(grabber.getSampleRate());

            grabber.start();
            recorder.start();

            Frame frame;
            while ((frame = grabber.grab()) != null) {
                recorder.record(frame);
            }

            recorder.stop();
            grabber.stop();

            // 删除临时文件
            Files.deleteIfExists(listFile);

            log.info("视频拼接完成: {}", outputPath);
            return outputPath;

        } catch (Exception e) {
            log.error("视频拼接异常", e);
            throw e;
        }
    }

    /**
     * 异步拼接多个音频
     *
     * @param audioUrls 音频URL列表
     * @return 拼接后的音频文件路径
     */
    public CompletableFuture<String> concatAudiosAsync(List<String> audioUrls) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return concatAudios(audioUrls);
            } catch (Exception e) {
                log.error("音频拼接失败", e);
                throw new RuntimeException("音频拼接失败", e);
            }
        }, executorService);
    }

    /**
     * 拼接多个音频
     *
     * @param audioUrls 音频URL列表
     * @return 拼接后的音频文件路径
     */
    public String concatAudios(List<String> audioUrls) throws Exception {
        if (audioUrls == null || audioUrls.isEmpty()) {
            throw new IllegalArgumentException("音频URL列表不能为空");
        }

        log.info("开始拼接音频: {} 个音频", audioUrls.size());

        String outputPath = "output/audios/concat_" + UUID.randomUUID() + ".mp3";
        Files.createDirectories(Paths.get(outputPath).getParent());

        try {
            // 使用FFmpeg的concat filter
            StringBuilder filterComplex = new StringBuilder();
            for (int i = 0; i < audioUrls.size(); i++) {
                if (i > 0) {
                    filterComplex.append(";");
                }
                filterComplex.append("[").append(i).append(":a]");
            }
            filterComplex.append("concat=n=").append(audioUrls.size()).append(":v=0:a=1[outa]");

            FFmpegFrameGrabber[] grabbers = new FFmpegFrameGrabber[audioUrls.size()];
            for (int i = 0; i < audioUrls.size(); i++) {
                grabbers[i] = new FFmpegFrameGrabber(audioUrls.get(i));
                grabbers[i].start();
            }

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, grabbers[0].getAudioChannels());
            recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_MP3);
            recorder.setSampleRate(grabbers[0].getSampleRate());
            recorder.setSampleFormat(grabbers[0].getSampleFormat());
            recorder.start();

            // 简单实现：依次录制音频帧
            for (FFmpegFrameGrabber grabber : grabbers) {
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    if (frame.samples != null) {
                        recorder.record(frame);
                    }
                }
                grabber.stop();
            }

            recorder.stop();

            log.info("音频拼接完成: {}", outputPath);
            return outputPath;

        } catch (Exception e) {
            log.error("音频拼接异常", e);
            throw e;
        }
    }

    /**
     * 合成视频和音频
     *
     * @param videoUrl 视频URL
     * @param audioUrl 音频URL
     * @return 合成后的视频文件路径
     */
    public String mergeVideoAndAudio(String videoUrl, String audioUrl) throws Exception {
        log.info("开始合成视频和音频: video={}, audio={}", videoUrl, audioUrl);

        String outputPath = "output/videos/merge_" + UUID.randomUUID() + ".mp4";
        Files.createDirectories(Paths.get(outputPath).getParent());

        FFmpegFrameGrabber videoGrabber = new FFmpegFrameGrabber(videoUrl);
        FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(audioUrl);

        videoGrabber.start();
        audioGrabber.start();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath,
                videoGrabber.getImageWidth(),
                videoGrabber.getImageHeight(),
                audioGrabber.getAudioChannels());

        recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
        recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC);
        recorder.setFormat("mp4");
        recorder.setFrameRate(videoGrabber.getFrameRate());
        recorder.setSampleRate(audioGrabber.getSampleRate());

        recorder.start();

        // 同时读取视频和音频帧并录制
        Frame videoFrame;
        Frame audioFrame;

        while (true) {
            videoFrame = videoGrabber.grabImage();
            audioFrame = audioGrabber.grabSamples();

            if (videoFrame == null && audioFrame == null) {
                break;
            }

            if (videoFrame != null) {
                recorder.record(videoFrame);
            }

            if (audioFrame != null) {
                recorder.record(audioFrame);
            }
        }

        recorder.stop();
        videoGrabber.stop();
        audioGrabber.stop();

        log.info("音视频合成完成: {}", outputPath);
        return outputPath;
    }

    /**
     * 异步合成视频和音频
     *
     * @param videoUrl 视频URL
     * @param audioUrl 音频URL
     * @return 合成后的视频文件路径
     */
    public CompletableFuture<String> mergeVideoAndAudioAsync(String videoUrl, String audioUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mergeVideoAndAudio(videoUrl, audioUrl);
            } catch (Exception e) {
                log.error("音视频合成失败", e);
                throw new RuntimeException("音视频合成失败", e);
            }
        }, executorService);
    }

    /**
     * 替换视频中的音频
     *
     * @param videoUrl    视频URL
     * @param newAudioUrl 新音频URL
     * @return 替换后的视频文件路径
     */
    public String replaceAudio(String videoUrl, String newAudioUrl) throws Exception {
        log.info("开始替换音频: video={}, newAudio={}", videoUrl, newAudioUrl);

        String outputPath = "output/videos/replace_audio_" + UUID.randomUUID() + ".mp4";
        Files.createDirectories(Paths.get(outputPath).getParent());

        // 实际上和mergeVideoAndAudio类似，但只使用新音频
        return mergeVideoAndAudio(videoUrl, newAudioUrl);
    }

    /**
     * 获取视频时长（秒）
     *
     * @param videoUrl 视频URL
     * @return 时长（秒）
     */
    public double getVideoDuration(String videoUrl) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoUrl)) {
            grabber.start();
            double duration = grabber.getLengthInTime() / 1000000.0;
            grabber.stop();
            return duration;
        }
    }

    /**
     * 获取音频时长（秒）
     *
     * @param audioUrl 音频URL
     * @return 时长（秒）
     */
    public double getAudioDuration(String audioUrl) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(audioUrl)) {
            grabber.start();
            double duration = grabber.getLengthInTime() / 1000000.0;
            grabber.stop();
            return duration;
        }
    }
}

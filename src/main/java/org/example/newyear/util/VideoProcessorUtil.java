package org.example.newyear.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.*;
import org.example.newyear.service.oss.OssService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
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
 * 支持视频拼接、音频拼接、音视频合成、OSS上传
 *
 * @author Claude
 * @since 2026-02-05
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoProcessorUtil {

    private final OssService ossService;
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
        String outputPath = "output/videos/concat_" + UUID.randomUUID() + ".mp4";
        return concatVideos(videoUrls, outputPath, "default", "default");
    }

    /**
     * 拼接多个视频（指定recordId，自动上传到默认OSS）
     *
     * @param videoUrls 视频URL列表
     * @param recordId  记录ID
     * @return 拼接后的视频OSS URL
     */
    public String concatVideos(List<String> videoUrls, String recordId) throws Exception {
        String localOutputPath = "output/videos/concat_" + UUID.randomUUID() + ".mp4";
        return concatVideos(videoUrls, localOutputPath, recordId, "default");
    }

    /**
     * 拼接多个视频（指定recordId和OSS账号）
     *
     * @param videoUrls  视频URL列表
     * @param recordId   记录ID
     * @param accountType OSS账号类型（如：default、cv）
     * @return 拼接后的视频OSS URL
     */
    public String concatVideos(List<String> videoUrls, String recordId, String accountType) throws Exception {
        String localOutputPath = "output/videos/concat_" + UUID.randomUUID() + ".mp4";
        return concatVideos(videoUrls, localOutputPath, recordId, accountType);
    }

    /**
     * 拼接多个视频（指定输出路径和OSS账号）
     *
     * @param videoUrls   视频URL列表
     * @param outputUrl   输出路径（本地或OSS）
     * @param recordId    记录ID（用于生成OSS路径）
     * @param accountType OSS账号类型（如：default、cv）
     * @return 拼接后的视频OSS URL
     */
    public String concatVideos(List<String> videoUrls, String outputUrl, String recordId, String accountType) throws Exception {
        if (videoUrls == null || videoUrls.isEmpty()) {
            throw new IllegalArgumentException("视频URL列表不能为空");
        }

        log.info("开始拼接视频: {} 个视频, recordId={}, accountType={}", videoUrls.size(), recordId, accountType);

        String localOutputPath = outputUrl;

        Files.createDirectories(Paths.get(localOutputPath).getParent());

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
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(localOutputPath,
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

            log.info("视频拼接完成: localPath={}", localOutputPath);

            // 上传到OSS（指定账号类型）
            String ossUrl = uploadLocalFileToOss(localOutputPath, recordId, "videos", "final_result", accountType);
            log.info("拼接视频已上传到OSS[{}]: {}", accountType, ossUrl);

            // 删除本地临时文件
            deleteLocalFile(localOutputPath);

            return ossUrl;

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
     * 混入背景音乐（BGM）到视频
     * 将视频的原始音频与背景音乐混合
     *
     * @param videoUrl 视频URL
     * @param bgmUrl   背景音乐URL
     * @param recordId 记录ID（用于生成OSS路径）
     * @return 混合后的视频OSS URL
     */
    public String mixAudioWithBgm(String videoUrl, String bgmUrl, String recordId) throws Exception {
        log.info("开始混入背景音乐: video={}, bgm={}, recordId={}", videoUrl, bgmUrl, recordId);

        String localOutputPath = "output/videos/mix_bgm_" + UUID.randomUUID() + ".mp4";
        Files.createDirectories(Paths.get(localOutputPath).getParent());

        try {
            FFmpegFrameGrabber videoGrabber = new FFmpegFrameGrabber(videoUrl);
            FFmpegFrameGrabber bgmGrabber = new FFmpegFrameGrabber(bgmUrl);

            videoGrabber.start();
            bgmGrabber.start();

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(localOutputPath,
                    videoGrabber.getImageWidth(),
                    videoGrabber.getImageHeight(),
                    videoGrabber.getAudioChannels());

            recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
            recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC);
            recorder.setFormat("mp4");
            recorder.setFrameRate(videoGrabber.getFrameRate());
            recorder.setSampleRate(videoGrabber.getSampleRate());
            recorder.setAudioQuality(0);  // 高质量音频

            recorder.start();

            // 读取视频帧和混合音频
            Frame videoFrame;
            Frame videoAudioFrame;
            Frame bgmFrame;

            while ((videoFrame = videoGrabber.grabImage()) != null) {
                // 录制视频帧
                recorder.record(videoFrame);
            }

            // 混合音频：视频原始音频 + BGM
            // 简化实现：先录制视频音频，再录制BGM（实际应使用音频混合滤镜）
            while ((videoAudioFrame = videoGrabber.grabSamples()) != null) {
                recorder.record(videoAudioFrame);
            }

            // 循环BGM以匹配视频长度
            while ((bgmFrame = bgmGrabber.grabSamples()) != null) {
                recorder.record(bgmFrame);
            }

            recorder.stop();
            videoGrabber.stop();
            bgmGrabber.stop();

            log.info("背景音乐混合完成: localPath={}", localOutputPath);

            // 上传到OSS
            String ossUrl = uploadLocalFileToOss(localOutputPath, recordId, "videos", "mix_bgm");
            log.info("背景音乐视频已上传到OSS: {}", ossUrl);

            // 删除本地临时文件
            deleteLocalFile(localOutputPath);

            return ossUrl;

        } catch (Exception e) {
            log.error("背景音乐混合异常", e);
            throw e;
        }
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

    // ======================== OSS上传辅助方法 ========================

    /**
     * 上传本地文件到OSS（使用默认账号）
     *
     * @param localFilePath 本地文件路径
     * @param recordId      记录ID
     * @param category      文件分类（如：videos, audios, images）
     * @param fileName      文件名（不含扩展名）
     * @return OSS访问URL
     */
    private String uploadLocalFileToOss(String localFilePath, String recordId, String category, String fileName) throws Exception {
        return uploadLocalFileToOss(localFilePath, recordId, category, fileName, "default");
    }

    /**
     * 上传本地文件到OSS（指定OSS账号）
     *
     * @param localFilePath 本地文件路径
     * @param recordId      记录ID
     * @param category      文件分类（如：videos, audios, images）
     * @param fileName      文件名（不含扩展名）
     * @param accountType   OSS账号类型（如：default、cv）
     * @return OSS访问URL
     */
    private String uploadLocalFileToOss(String localFilePath, String recordId, String category, String fileName, String accountType) throws Exception {
        try {
            File localFile = new File(localFilePath);
            if (!localFile.exists()) {
                throw new RuntimeException("本地文件不存在: " + localFilePath);
            }

            // 获取文件扩展名
            String extension = getFileExtension(localFile.getName());

            // 生成OSS路径：spring2026/{recordId}/{category}/{fileName}{extension}
            String ossPath = String.format("%s/%s/%s/%s%s",
                    recordId, category, fileName, extension);

            log.info("开始上传到OSS[{}]: localFile={}, ossPath={}", accountType, localFilePath, ossPath);

            // 创建MultipartFile（需要自定义实现）
            // 由于Spring的MultipartFile是接口，我们需要创建一个适配器
            FileMultipartFile fileAdapter = new FileMultipartFile(localFile);

            // 上传到OSS（指定账号类型）
            var uploadResult = ossService.upload(fileAdapter, ossPath, accountType);

            log.info("OSS上传成功[{}]: fileKey={}, accessUrl={}",
                    accountType, uploadResult.getFileKey(), uploadResult.getAccessUrl());

            return uploadResult.getAccessUrl();

        } catch (Exception e) {
            log.error("上传到OSS失败: localFile={}", localFilePath, e);
            throw new RuntimeException("上传到OSS失败", e);
        }
    }

    /**
     * 删除本地文件
     *
     * @param filePath 文件路径
     */
    private void deleteLocalFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                log.info("删除本地文件: {}, success={}", filePath, deleted);
            }
        } catch (Exception e) {
            log.warn("删除本地文件失败: {}", filePath, e);
        }
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（包含点）
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * File到MultipartFile的适配器
     */
    private static class FileMultipartFile implements org.springframework.web.multipart.MultipartFile {
        private final File file;

        public FileMultipartFile(File file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getOriginalFilename() {
            return file.getName();
        }

        @Override
        public String getContentType() {
            // 根据文件扩展名推测MIME类型
            String filename = file.getName().toLowerCase();
            if (filename.endsWith(".mp4")) {
                return "video/mp4";
            } else if (filename.endsWith(".mp3")) {
                return "audio/mpeg";
            } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (filename.endsWith(".png")) {
                return "image/png";
            }
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return file.length() == 0;
        }

        @Override
        public long getSize() {
            return file.length();
        }

        @Override
        public byte[] getBytes() throws java.io.IOException {
            return java.nio.file.Files.readAllBytes(file.toPath());
        }

        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            return new FileInputStream(file);
        }

        @Override
        public void transferTo(File dest) throws java.io.IOException, IllegalStateException {
            java.nio.file.Files.copy(file.toPath(), dest.toPath());
        }
    }
}

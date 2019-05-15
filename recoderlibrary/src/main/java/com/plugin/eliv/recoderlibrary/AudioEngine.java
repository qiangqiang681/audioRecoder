package com.plugin.eliv.recoderlibrary;

import android.content.Context;

public class AudioEngine {

    private boolean enableSystemAEC = true;
    private boolean hasInit;
    private AudioCore audioCore;

    public static AudioEngine getInstance() {
        return AudioEngineHandle.INSTANCE;
    }

    private AudioEngine() {
    }

    private static class AudioEngineHandle {
        private static AudioEngine INSTANCE = new AudioEngine();
    }

    public synchronized AudioEngine init(Context context, RecodeAudioDataListener recodeListener, Frequency hz, int frameSize) {
        if (hasInit) {
            return this;
        }
        if (context == null) {
            throw new RuntimeException("context can not be null");
        }
        hasInit = true;
        audioCore = new AudioCore(context.getApplicationContext(), recodeListener, hz, frameSize, enableSystemAEC);
        return this;
    }

    public AudioEngine init(Context context, RecodeAudioDataListener recodeListener, Frequency hz) {
        return init(context, recodeListener, hz, hz.value() / 10);
    }

    public AudioEngine init(Context context, RecodeAudioDataListener recodeListener) {
        return init(context, recodeListener, Frequency.PCM_8K);
    }

    public AudioEngine init(Context context) {
        return init(context, null, Frequency.PCM_8K);
    }

    /**
     * 开始录音
     * @param path
     */
    public void startRecord(String path) {
        if (!hasInit) {
            throw new RuntimeException("must init before start");
        }
        audioCore.startRecord(path);
    }

    /**
     * 开始播放
     * @param path
     */
    public void startPlay(String path) {
        if (!hasInit) {
            throw new RuntimeException("must init before start");
        }
        audioCore.startPlay(path);
    }

    public void stopRecord() {
        if (audioCore != null)
            audioCore.stopRecord();
    }

    public void stopPlay() {
        if (audioCore != null)
            audioCore.stopPlay();
    }

    public void pauseRecord() {
        if (audioCore != null)
            audioCore.pauseRecord();
    }

    public void pausePlay() {
        if (audioCore != null)
            audioCore.pausePlay();
    }

    public void resumeRecord() {
        if (audioCore != null)
            audioCore.resumeRecord();
    }

    public void resumePlay() {
        if (audioCore != null)
            audioCore.resumePlay();
    }

//    public void release() {
//        hasInit = false;
//        if (audioCore != null) {
//            audioCore.release();
//        }
//    }


    public AudioEngine disableSystemAEC() {
        enableSystemAEC = false;
        return this;
    }

    public AudioEngine enableSystemAEC() {
        enableSystemAEC = true;
        return this;
    }

    public interface RecodeAudioDataListener {
        int onRecodeAudioData(byte[] data, int begin, int end);
    }

}

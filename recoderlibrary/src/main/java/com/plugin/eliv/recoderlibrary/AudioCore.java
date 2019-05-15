package com.plugin.eliv.recoderlibrary;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

import java.io.*;

/**
 * Created by eliv on 19-5-14.
 */
class AudioCore {

    private final int AUDIO_RECORD_SAMPLE_RATE;
    private final Context context;
    private final AudioEngine.RecodeAudioDataListener listener;
    private final int frameSize;
    private boolean enableSystemAEC;
    private volatile AudioRecord audioRecode = null;
    private volatile AudioTrack audioPlay = null;
    private volatile Status status = Status.STATUS_NO_READY;

    AudioCore(Context context, AudioEngine.RecodeAudioDataListener recodeListener, Frequency hz, int frameSize, boolean enableSystemAEC) {
        AUDIO_RECORD_SAMPLE_RATE = hz.value();
        this.enableSystemAEC = enableSystemAEC;
        AudioUtils.chooseAudioMode(context, AudioManager.MODE_IN_COMMUNICATION, enableSystemAEC);
        this.context = context;
        this.listener = recodeListener;
        this.frameSize = frameSize;
    }

    void startPlay(String path) {
        int[] minBuffSize = new int[1];
        audioPlay = AudioUtils.createTracker(minBuffSize, AUDIO_RECORD_SAMPLE_RATE, frameSize, enableSystemAEC);
        audioPlay.play();
        status = Status.STATUS_READY;

        new Thread(() -> readDataFromFile(path, minBuffSize[0])).start();
    }

    private void readDataFromFile(String path, int bufferSizeInBytes) {
        byte[] data = new byte[bufferSizeInBytes];
        FileInputStream fileInputStream = null;
        try {
            if (audioPlay.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                audioPlay.play();
            }
            status = Status.STATUS_START;
            fileInputStream = new FileInputStream(path);
            while (fileInputStream.read(data) > 0) {
                awaitLock();
                if (audioPlay == null) return;
                Log.e("AudioTrack", audioPlay + "" + status);
                if (audioPlay.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
                    audioPlay.write(data, 0, data.length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e) {
            Log.e("AudioTrack", e.getMessage());
        }
    }

    public void startRecord(String path) {
        if (status == Status.STATUS_START) {
            Log.e("AudioRecorder", "正在录音");
            return;
        }
        int[] minBuffSize = new int[1];
        audioRecode = AudioUtils.creatAudioRecord(minBuffSize, AUDIO_RECORD_SAMPLE_RATE, frameSize, enableSystemAEC);
        Log.d("AudioRecorder", "===startRecord===" + audioRecode.getState());
        audioRecode.startRecording();
        status = Status.STATUS_READY;

        new Thread(() -> writeDataT0File(path, minBuffSize[0])).start();
    }

    private void writeDataT0File(String path, int bufferSizeInBytes) {
        byte[] data = new byte[bufferSizeInBytes];

        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);

            status = Status.STATUS_START;
            while (audioRecode.read(data, 0, bufferSizeInBytes) != AudioRecord.ERROR_INVALID_OPERATION) {
                awaitLock();
                if (audioRecode == null) return;
                if (audioRecode.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    try {
                        fos.write(data);
                        if (listener != null) {
                            listener.onRecodeAudioData(data, 0, data.length);
                        }
                    } catch (IOException e) {
                        Log.e("AudioRecorder", e.getMessage());
                    }
                }
            }
        } catch (IllegalStateException e) {
            Log.e("AudioRecorder", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        } catch (FileNotFoundException e) {
            Log.e("AudioRecorder", e.getMessage());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            Log.e("AudioRecorder", e.getMessage());
        }
    }

    private void awaitLock() throws InterruptedException {
        if (status == Status.STATUS_PAUSE) {
            synchronized (AudioCore.class) {
                AudioCore.class.wait();
            }
        }
    }

    private void notifyLock() {
        synchronized (AudioCore.class) {
            AudioCore.class.notify();
        }
    }

    void pausePlay() {
        Log.d("AudioTrack", "===pausePlay===");
        if (status != Status.STATUS_START) {
            Log.e("AudioTrack", "没有在播放");
        } else {
//            audioPlay.pause();
            status = Status.STATUS_PAUSE;
        }
    }

    void resumePlay() {
        Log.d("AudioTrack", "===resumePlay===");
        if (status != Status.STATUS_PAUSE) {
            Log.d("AudioTrack", "没有在播放");
        } else {
//            audioPlay.play();
            status = Status.STATUS_START;
            notifyLock();
        }
    }

    void pauseRecord() {
        Log.d("AudioRecorder", "===pauseRecord===");
        if (status != Status.STATUS_START) {
            Log.d("AudioRecorder", "没有在录音");
        } else {
//            audioRecode.stop();
            status = Status.STATUS_PAUSE;
        }
    }

    void resumeRecord() {
        Log.d("AudioRecorder", "===resumeRecord===");
        if (status != Status.STATUS_PAUSE) {
            Log.d("AudioRecorder", "没有在录音");
        } else {
//            audioRecode.stop();
            status = Status.STATUS_START;
        }
    }

    void stopPlay() {
        Log.d("AudioTrack", "===stopPlay===");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            Log.d("AudioTrack", "播放尚未开始");
        } else {
            status = Status.STATUS_STOP;
            if (audioPlay != null) {
                audioPlay.stop();
                audioPlay.release();
                audioPlay = null;
                status = Status.STATUS_NO_READY;
                notifyLock();
            }
        }
    }

    void stopRecord() {
        Log.d("AudioRecorder", "===stopRecord===");
        if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY) {
            Log.d("AudioRecorder", "录音尚未开始");
        } else {
            status = Status.STATUS_STOP;
            if (audioRecode != null) {
                audioRecode.stop();
                audioRecode.release();
                audioRecode = null;
            }
            status = Status.STATUS_NO_READY;
            notifyLock();
            AudioUtils.chooseAudioMode(context, AudioManager.MODE_NORMAL,enableSystemAEC);
        }
    }

    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }
}

package com.plugin.eliv.recoderlibrary;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;

class AudioUtils {
    static AudioRecord creatAudioRecord(int[] minBuffS, int sampRate, int frameSize, boolean enableaec) {
        AudioRecord audioRecord = null;
        int minBufSize = AudioRecord.getMinBufferSize(sampRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if ((minBufSize != -2) && (minBufSize % frameSize != 0)) {
            minBufSize = (minBufSize / frameSize + 1) * frameSize;
        }
        minBuffS[0] = minBufSize * 3;
        audioRecord = new AudioRecord(enableaec ? AudioSource.VOICE_COMMUNICATION : AudioSource.DEFAULT, sampRate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufSize * 3);
        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            return audioRecord;
        }
        return null;
    }

    static AudioTrack createTracker(int[] minBuffs, int sampRate, int frameSize, boolean enableaec) {
        try {
            AudioTrack tracker = null;

            int minBufSize = AudioTrack.getMinBufferSize(sampRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if ((minBufSize != -2) && (minBufSize % frameSize != 0)) {
                minBufSize = (minBufSize / frameSize + 1) * frameSize;
            }
            minBuffs[0] = minBufSize * 3;
            tracker = new AudioTrack(enableaec ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC, sampRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufSize * 3, AudioTrack.MODE_STREAM);
            if (tracker.getState() == AudioTrack.STATE_INITIALIZED) {
                return tracker;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static void chooseAudioMode(Context context, int mode, boolean enableaec) {
        if (context == null) return;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(enableaec ? mode : AudioManager.MODE_CURRENT);
        if (enableaec) {
            audioManager.setSpeakerphoneOn(true);
        }
    }
}

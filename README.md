# audioRecoder回声降噪

### 想要实现aec功能主要有三个切入点：
AcousticEchoCanceler（API4.1提供）

AudioSource.VOICE_COMMUNICATION（使用voip通道录音）

webrtc（业界有名的回声消除库，支持跨平台，难点是计算delay时间）
### AcousticEchoCanceler
回音消除需要和采集声音配合在一起，使用AudioRecord:
```
recorder = AudioRecord(
   MediaRecorder.AudioSource.DEFAULT, 16000 
, AudioFormat.CHANNEL_IN_MONO,
       AudioFormat.ENCODING_PCM_16BIT, bufferSize
)
fun enableAcousticEchoCanceler() {
   if (AcousticEchoCanceler.isAvailable()) {
       aec = AcousticEchoCanceler.create(recorder.getAudioSessionId())
       if (aec != null) {
           aec.setEnabled(true)
       } else {
           Log.e(TAG, "AudioInput: AcousticEchoCanceler is null and not enabled")
       }
   }
}
```
在开始录制之前调用enableAcousticEchoCanceler方法开启AEC：
enableAcousticEchoCanceler()
recorder.startRecording()

#### 结论
按照官方文档描述在API4.1开始支持，然而在各类国产机型中测试并不起作用（文末列有测试机型）。
### AudioSource.VOICE_COMMUNICATION
使用 VOICE_COMMUNICATION 作为 AudioSource，调谐语音通信( 如 VoIP )的麦克风音频源
```
recorder = AudioRecord(
   MediaRecorder.AudioSource.VOICE_COMMUNICATION
, 16000 
, AudioFormat.CHANNEL_IN_MONO,
       AudioFormat.ENCODING_PCM_16BIT, bufferSize
)
```
调用start方法开始录制。
#### 结论
再部分手机，如 华为、vivo等机型可以实现扬声器消除，小米和OPPO不支持，同样在此基础上添加AcousticEchoCanceler，测试结果一致。
### 与AudioManager.MODE_IN_COMMUNICATION配合
Android系统中是用AudioManager来管理播放模式的，通过AudioManager.setMode()方法来实现。其中MODE_IN_COMMUNICATION为通信模式，包括音/视频,VoIP通话。 
```
private fun chooseAudioMode(enableAec: Boolean) {
   val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
   audioManager.mode = if (enableAec) AudioManager.MODE_IN_COMMUNICATION else AudioManager.MODE_NORMAL
   audioManager.isSpeakerphoneOn = enableAec
}
```
在Recorder初始化之前开启 MODE_IN_COMMUNICATION 模式，配合前面两种测试方法：
chooseAudioMode(AudioManager.MODE_IN_COMMUNICATION,true)
#### 结论
在使用了 VOICE_COMMUNICATION 作为 AudioSource的测试案例中，发现添加上AudioManager来管理播放模式，其结果满足几乎各种品牌（三星效果不太好），录制效果也是最为理想。
### webrtc方案
基于webrtc-aec模块的回声消除。
https://github.com/HelloSoul/AndroidWebrtcAec
#### 结论
单纯使用webrtc-aec 进行降噪处理，其效果并不太理想，大概能去除80%左右的扬声器回声，不过仍能听到20%左右的背景声音。此方法最大好处是不依赖ROM，适配性较好。

## 综上：
AcousticEchoCanceler没什么作用
AudioSource.VOICE_COMMUNICATION + AudioManager.MODE_IN_COMMUNICATION
方式除了三星效果不太好，其他品牌没测出问题。
webrtc 适配性较好，降噪效果不太理想

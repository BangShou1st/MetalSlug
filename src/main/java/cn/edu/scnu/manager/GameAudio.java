package cn.edu.scnu.manager;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 通过 classpath 懒加载并复用课程演示所需的短音频。
 */
public final class GameAudio {
    private static final String BACKGROUND_MUSIC_KEY="music.background";
    private static final float BACKGROUND_MUSIC_GAIN_DB=-8.0F;
    private static final String CONFIG_PATH="text/AudioData.properties"; //音频配置路径
    private static final Map<String,Clip> CLIPS=new HashMap<>(); //每个键唯一的音频缓存
    private static final Set<String> FAILED_KEYS=new HashSet<>(); //已经报告过错误的音频键
    private static final Set<String> LOOPING_KEYS=new HashSet<>(); //当前持续循环播放的音频键
    private static final Properties AUDIO_PATHS=loadAudioPaths(); //音频键和资源路径

    private GameAudio() {
    }

    //重新从头播放指定音效
    public static synchronized void play(String key) {
        Clip clip=getClip(key);
        //音频不可用时忽略播放
        if(clip==null) {
            return;
        }
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    //音效仍在播放时不重复叠加
    public static synchronized void playIfIdle(String key) {
        Clip clip=getClip(key);
        //正在播放时不重复叠加
        if(clip==null || clip.isRunning()) {
            return;
        }
        clip.setFramePosition(0);
        clip.start();
    }

    //从头开始并持续循环播放指定音频
    public static synchronized void loop(String key) {
        Clip clip=getClip(key);
        //音频不可用时忽略循环请求
        if(clip==null) {
            return;
        }
        applyBackgroundGain(key,clip);
        //已经循环播放时不重新开始
        if(clip.isRunning() && LOOPING_KEYS.contains(key)) {
            return;
        }
        clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        LOOPING_KEYS.add(key);
    }

    private static void applyBackgroundGain(String key,Clip clip) {
        //只调整支持音量控制的背景音乐
        if(!BACKGROUND_MUSIC_KEY.equals(key)
                || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        try {
            FloatControl gain=(FloatControl)clip.getControl(
                    FloatControl.Type.MASTER_GAIN);
            float value=Math.max(gain.getMinimum(),
                    Math.min(gain.getMaximum(),BACKGROUND_MUSIC_GAIN_DB));
            gain.setValue(value);
        }catch (IllegalArgumentException ignored) {
            //设备不支持音量控制时保持原音量
        }
    }

    //只停止一次性音效，保留持续循环的背景音乐
    public static synchronized void stopEffects() {
        for(Map.Entry<String,Clip> entry:CLIPS.entrySet()) {
            if(!LOOPING_KEYS.contains(entry.getKey())) {
                entry.getValue().stop();
            }
        }
    }

    //停止所有当前音效但保留缓存
    public static synchronized void stopAll() {
        for(Clip clip:CLIPS.values()) {
            clip.stop();
        }
        LOOPING_KEYS.clear();
    }

    //关闭全部音频资源并清空缓存
    public static synchronized void closeAll() {
        for(Clip clip:CLIPS.values()) {
            clip.stop();
            clip.close();
        }
        CLIPS.clear();
        LOOPING_KEYS.clear();
        FAILED_KEYS.clear();
    }

    //获取或首次加载指定键对应的唯一 Clip
    private static Clip getClip(String key) {
        Clip cached=CLIPS.get(key);
        //优先复用已加载音频
        if(cached!=null) {
            return cached;
        }
        //已失败的音频不重复加载
        if(FAILED_KEYS.contains(key)) {
            return null;
        }

        String path=AUDIO_PATHS.getProperty(key);
        //检查音频配置和资源路径
        if(path==null || path.trim().isEmpty()) {
            reportAudioFailure(key);
            return null;
        }
        URL resource=GameAudio.class.getClassLoader().getResource(path.trim());
        if(resource==null) {
            reportAudioFailure(key);
            return null;
        }

        //首次读取音频并加入缓存
        try(AudioInputStream stream=AudioSystem.getAudioInputStream(resource)) {
            Clip clip=AudioSystem.getClip();
            clip.open(stream);
            CLIPS.put(key,clip);
            return clip;
        }catch (Exception e) {
            reportAudioFailure(key);
            return null;
        }
    }

    //读取音频键配置，配置不可用时降级为空配置
    private static Properties loadAudioPaths() {
        Properties properties=new Properties();
        try(InputStream stream=GameAudio.class.getClassLoader()
                .getResourceAsStream(CONFIG_PATH)) {
            if(stream==null) {
                reportAudioFailure(CONFIG_PATH);
                return properties;
            }
            properties.load(new InputStreamReader(stream,StandardCharsets.UTF_8));
        }catch (Exception e) {
            reportAudioFailure(CONFIG_PATH);
        }
        return properties;
    }

    //同一个错误键只输出一次简短说明
    private static void reportAudioFailure(String key) {
        if(FAILED_KEYS.add(key)) {
            System.err.println("音频不可用："+key);
        }
    }
}

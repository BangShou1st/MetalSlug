package cn.edu.scnu.manager;

import cn.edu.scnu.element.ElementObj;

import javax.swing.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** 加载配置、图片和游戏对象。 */

public class GameLoad {
    //资源管理器
    private static final ElementManager EM=ElementManager.getManager();

    private static final String TEXT_PATH="text/";
    //单张图片
    private static final Map<String,ImageIcon> imgMap=new HashMap<>();
    //序列图片
    private static final Map<String,List<ImageIcon>> imgMaps=new HashMap<>();
    //全局配置
    private static Properties gamePro=new Properties();
    //图片是否已经加载
    private static boolean imgLoaded=false;
    //对象名称和类的对应关系
    private static Map<String,Class<?>> objMap=new HashMap<>();


    //读取基础配置
    private static Properties loadPro(String fileName) {
        Properties properties=new Properties();
        try(InputStream in=GameLoad.class.getClassLoader()
                .getResourceAsStream(TEXT_PATH+fileName)) {
            properties.load(new InputStreamReader(in,StandardCharsets.UTF_8));
        }catch (Exception e) {
            throw new RuntimeException(fileName+"读取失败");
        }
        return properties;
    }

    //加载游戏全局配置
    public static void loadGameData() {
        gamePro=loadPro("GameData.properties");
    }

    //读取必需的字符串配置
    public static String getString(String key) {
        //首次读取时加载游戏配置
        if(gamePro.isEmpty()) {
            loadGameData();
        }
        String value=gamePro.getProperty(key);
        //缺少必需配置时直接报错
        if(value==null) {
            throw new IllegalArgumentException("缺少游戏配置："+key);
        }
        return value.trim();
    }

    //读取整数配置
    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    //读取布尔配置
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    //加载全部图片
    public static void loadImg() {
        //图片已经加载时直接复用缓存
        if(imgLoaded) {
            return;
        }
        Properties imgPro=loadPro("ImageData.properties");

        for(String key:imgPro.stringPropertyNames()) {
            String[] data=imgPro.getProperty(key).split(",");
            String path=data[0].trim();
            //按配置加载单图或动画序列
            if(data.length==1) {
                imgMap.put(key,loadIcon(path));
            }else {
                int count=Integer.parseInt(data[1].trim());
                imgMaps.put(key,loadImages(path,count));
            }
        }
        imgLoaded=true;
    }

    //加载单张图片
    private static ImageIcon loadIcon(String path) {
        path=path.trim();

        java.net.URL url=GameLoad.class.getClassLoader().getResource(path);

        //资源路径不存在时停止加载
        if(url==null) {
            throw new RuntimeException("图片不存在："+path);
        }

        return new ImageIcon(url);
    }

    //加载图片序列
    private static List<ImageIcon> loadImages(String path,int count) {
        List<ImageIcon> images=new ArrayList<>();
        for(int i=0;i<count;i++) {
            String file=String.format("%s/frame_%02d.png", path, i);
            images.add(loadIcon(file));
        }
        return images;
    }

    //获取单张图片
    public static ImageIcon getImage(String key) {
        loadImg();
        ImageIcon image=imgMap.get(key);
        if(image==null) {
            throw new IllegalArgumentException("缺少单张图片配置："+key);
        }
        return image;
    }

    //获取图片序列
    public static List<ImageIcon> getImages(String key) {
        loadImg();
        List<ImageIcon> images=imgMaps.get(key);
        if(images==null) {
            throw new IllegalArgumentException("缺少图片序列配置："+key);
        }
        return images;
    }

    //读取对象配置
    public static void loadObj() {
        //对象配置已经加载时直接复用
        if(!objMap.isEmpty()) {
            return;
        }
        Properties objPro=loadPro("obj.properties");
        //把对象键映射到对应类型
        for(String key:objPro.stringPropertyNames()) {
            try {
                String className=objPro.getProperty(key);
                objMap.put(key,Class.forName(className));
            }catch (ClassNotFoundException e) {
                throw new RuntimeException(key+"对应的类不存在");
            }
        }
    }

    //根据配置创建对象
    public static ElementObj getObj(String key) {
        loadObj();
        Class<?> clazz=objMap.get(key);
        //缺少对象映射时直接报错
        if(clazz==null) {
            throw new IllegalArgumentException("缺少对象配置："+key);
        }
        //通过无参构造创建对象模板
        try {
            return (ElementObj)clazz.getDeclaredConstructor().newInstance();
        }catch (Exception e) {
            throw new RuntimeException(key+"对象创建失败",e);
        }
    }

    //加载玩家
    public static void loadPlay(String playStr) {
        ElementObj obj=getObj("play");
        ElementObj play=obj.createElement(playStr);
        EM.addElement(play,GameElement.PLAY);
    }
}

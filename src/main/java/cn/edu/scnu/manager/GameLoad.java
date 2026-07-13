package cn.edu.scnu.manager;

import cn.edu.scnu.element.ElementObj;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 加载器（工具：用于读取配置文件的工具）工具类，大多提供的是static方法
 */

public class GameLoad {
    //得到资源管理器
    private static ElementManager em=ElementManager.getManager();

    private static final String TEXT_PATH="text/";
    //单张图片
    public static Map<String,ImageIcon> imgMap=new HashMap<>();
    //序列图片
    public static Map<String,List<ImageIcon>> imgMaps=new HashMap<>();
    //全局配置
    private static Properties gamePro=new Properties();


    /**
     * 读取配置文件
     */
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

    //读取字符串配置
    public static String getString(String key) {
        if(gamePro.isEmpty()) {
            loadGameData();
        }
        return gamePro.getProperty(key);
    }

    //读取整数配置
    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    //读取布尔配置
    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    /**
     * 加载全部图片
     */
    public static void loadImg() {
        Properties imgPro=loadPro("ImageData.properties");

        for(String key:imgPro.stringPropertyNames()) {
            String[] data=imgPro.getProperty(key).split(",");

            //单张图片
            if(data.length==1) {
                imgMap.put(key,loadIcon(data[0]));
            }else { //图片序列
                imgMaps.put(key,loadImages(data[0], Integer.parseInt(data[1])));
            }
        }
    }

    //加载单张图片
    private static ImageIcon loadIcon(String path) {
        path=path.trim();

        java.net.URL url=GameLoad.class.getClassLoader().getResource(path);

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
        return imgMap.get(key);
    }

    //获取图片序列
    public static List<ImageIcon> getImages(String key) {
        return imgMaps.get(key);
    }


//    //用户读取文件的类
//    private static Properties pro=new Properties();
//    /**
//     * 说明 传入地图id有加载方法依据文件规则自动产生地图文件名称，加载文件
//     * @param mapId  文件编号，文件id
//     */
//    public static void MapLoad(int mapId) {
//        String mapName="cn/edu/scnu/text/"+mapId+".map"; //文件路径
//        //使用io流来获取文件对象 得到类加载器
//        ClassLoader classLoader =  GameLoad.class.getClassLoader();
//        InputStream maps = classLoader.getResourceAsStream(mapName);
//        System.out.println(maps);
//        if(null==maps){
//            System.out.println("配置文件处理异常，请重新安装");
//            return;
//        }
//
//        try{
//            pro.clear();
//            pro.load(maps);
//            //可以直接动态的获取所有的key，有key就可以获取value
//            Enumeration<?> names = pro.propertyNames();
//            while (names.hasMoreElements()) { //获取是无序的
//                String key = names.nextElement().toString();
//                //自动的创建和加载地图
//                String[] arrs=pro.getProperty(key).split(";");
//                for(int i=0;i<arrs.length;i++){
//                    ElementObj element = new MapObj().createElement(key+","+arrs[i]);
//                    em.addElement(element,GameElement.MAPS);
//                }
//            }
//
//        }catch (Exception e){}
//    }
//
//    /**
//     * 加载玩家
//     */
//    public static void loadPlay() {
//        loadObj();
//        String playStr="500,500,player1_up"; //不规范的
//        ElementObj obj=getObj("play");
//        ElementObj play = obj.createElement(playStr);
//        //ElementObj play = new Play().createElement(playStr);
//        //解耦，降低代码和代码之间的耦合度  可以直接通过接口或者是抽象父类就可以获取到实体对象
//
//        em.addElement(play,GameElement.PLAY);
//    }
//
//    public static ElementObj getObj(String str){
//        try {
//            Class<?> class1 = objMap.get(str);
//            Object newInstance = class1.newInstance();
//            if(newInstance instanceof ElementObj){
//                return (ElementObj)newInstance; //这个对象就和new Play()等价
//                //新建立了一个叫GamePlay的类
//            }
//        } catch (InstantiationException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//        return null;
//    }
//
//    /**
//     * 扩展：使用配置文件，来实例化对象  通过固定的key(字符串来实例化)
//     */
//    private  static Map<String, Class<?>> objMap=new HashMap<>();
//    public static void loadObj() {
//        String texturl="cn/edu/scnu/text/obj.pro";
//        ClassLoader classLoader =  GameLoad.class.getClassLoader();
//        InputStream texts = classLoader.getResourceAsStream(texturl);
//        pro.clear();
//        try{
//            pro.load(texts);
//            Set<Object> set = pro.keySet();
//            for (Object o : set) {
//                String classUrl=pro.getProperty(o.toString());
//                //使用反射的方式直接将类进行获取
//                Class<?> forName = Class.forName(classUrl);
//                objMap.put(o.toString(),forName);
//            }
//        }catch (IOException | ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    /*
//    public static void main(String[] args) throws IOException {
//        MapLoad(10);
//    }*/
}

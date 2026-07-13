package cn.edu.scnu.manager;

/**
 * 摄像机，记录当前窗口在世界地图中的位置
 */
public class Camera {

    private int x; //摄像机在世界地图中的横坐标
    private int y; //摄像机在世界地图中的纵坐标
    private int worldWidth; //地图总宽度
    private int worldHeight; //地图总高度

    //让玩家中心尽量位于窗口左侧四分之一并限制摄像机范围
    public void follow(int playerX,int playerW) {
        int width=GameLoad.getInt("window.width");
        //玩家中心点对应窗口左侧四分之一的位置
        x=playerX+playerW/2-width/4;
        int maxX=Math.max(0,worldWidth-width);
        if(x<0) {
            x=0;
        }
        if(x>maxX) {
            x=maxX;
        }
    }

    //获取摄像机在世界地图中的横坐标
    public int getX() {
        return x;
    }

    //获取摄像机在世界地图中的纵坐标
    public int getY() {
        return y;
    }

    //设置摄像机可查看的地图世界尺寸
    public void setWorldSize(int width,int height) {
        worldWidth=width;
        worldHeight=height;
    }

    //将摄像机视口恢复到当前关卡起点
    public void reset() {
        x=0;
        y=0;
    }
}

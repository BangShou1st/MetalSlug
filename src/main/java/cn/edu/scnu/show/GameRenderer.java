package cn.edu.scnu.show;

import cn.edu.scnu.controller.GameThread;
import cn.edu.scnu.element.ElementObj;
import cn.edu.scnu.element.Play;
import cn.edu.scnu.element.RoleObj;
import cn.edu.scnu.element.boss.AbstractBoss;
import cn.edu.scnu.manager.Camera;
import cn.edu.scnu.manager.ElementManager;
import cn.edu.scnu.manager.GameElement;
import cn.edu.scnu.manager.GameLoad;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 集中负责游戏世界、HUD、菜单和流程界面的绘制。
 * 本类只计算绘制与按钮区域，不修改游戏流程状态。
 */
public class GameRenderer {
    //当前鼠标命中的界面按钮
    public enum MenuButton {
        NONE, //没有命中按钮
        START_GAME, //进入关卡选择
        CONTROLS, //进入操作说明
        EXIT_GAME, //退出整个游戏
        LEVEL_ONE, //选择第一关
        LEVEL_TWO, //选择第二关
        BACK, //返回上一级菜单
        SETTINGS, //打开游戏设置
        RETURN_GAME, //返回当前游戏
        HOME //返回主菜单
    }

    private final ElementManager em; //读取需要绘制的游戏元素
    private Camera camera; //绘制世界层时使用的摄像机
    private GameThread gameThread; //读取当前游戏流程状态
    private RoleObj trackedBoss; //当前 Boss HP 条跟踪的 Boss
    private int bossMaxHp; //当前 Boss 的最大生命值

    //取得唯一元素管理器供视图读取
    public GameRenderer() {
        em=ElementManager.getManager();
    }

    //注入与游戏线程共用的摄像机
    public void setCamera(Camera camera) {
        this.camera=camera;
    }

    //注入绘制界面时读取的游戏线程
    public void setGameThread(GameThread gameThread) {
        this.gameThread=gameThread;
    }

    //根据当前游戏状态绘制对应画面
    public void draw(Graphics2D g,int panelWidth,int panelHeight,
                     MenuButton hoveredButton) {
        switch(gameThread.getGameState()) {
            case MAIN_MENU:
                drawMainMenu(g,panelWidth,panelHeight,hoveredButton);
                break;
            case LEVEL_SELECT:
                drawLevelSelect(g,panelWidth,panelHeight,hoveredButton);
                break;
            case CONTROLS:
                drawControls(g,panelWidth,panelHeight,hoveredButton);
                break;
            default:
                drawGame(g,panelWidth,panelHeight,hoveredButton);
                break;
        }
    }

    //根据当前状态和鼠标位置返回命中的按钮
    public MenuButton findButton(int x,int y,int panelWidth,int panelHeight) {
        switch(gameThread.getGameState()) {
            case MAIN_MENU:
                return findMainMenuButton(x,y,panelWidth);
            case LEVEL_SELECT:
                if(getBackButtonBounds().contains(x,y)) {
                    return MenuButton.BACK;
                }
                if(getLevelOneBounds(panelWidth).contains(x,y)) {
                    return MenuButton.LEVEL_ONE;
                }
                if(getLevelTwoBounds(panelWidth).contains(x,y)) {
                    return MenuButton.LEVEL_TWO;
                }
                break;
            case CONTROLS:
                if(getBackButtonBounds().contains(x,y)) {
                    return MenuButton.BACK;
                }
                break;
            case RUNNING:
                if(getSettingsButtonBounds(panelWidth).contains(x,y)) {
                    return MenuButton.SETTINGS;
                }
                break;
            case PAUSED:
                if(getBackButtonBounds().contains(x,y)) {
                    return MenuButton.RETURN_GAME;
                }
                if(getHomeButtonBounds(panelWidth).contains(x,y)) {
                    return MenuButton.HOME;
                }
                if(getSettingsExitButtonBounds(panelWidth).contains(x,y)) {
                    return MenuButton.EXIT_GAME;
                }
                break;
            default:
                break;
        }
        return MenuButton.NONE;
    }

    //查找主菜单中的三个操作按钮
    private MenuButton findMainMenuButton(int x,int y,int panelWidth) {
        if(getMainMenuButtonBounds(0,panelWidth).contains(x,y)) {
            return MenuButton.START_GAME;
        }
        if(getMainMenuButtonBounds(1,panelWidth).contains(x,y)) {
            return MenuButton.CONTROLS;
        }
        if(getMainMenuButtonBounds(2,panelWidth).contains(x,y)) {
            return MenuButton.EXIT_GAME;
        }
        return MenuButton.NONE;
    }

    //绘制关卡世界、固定 UI、HUD 和当前流程覆盖层
    private void drawGame(Graphics2D g,int panelWidth,int panelHeight,
                          MenuButton hoveredButton) {
        drawWorld(g);
        drawUiElements(g);
        drawHud(g,panelWidth);

        GameThread.GameState state=gameThread.getGameState();
        if(state==GameThread.GameState.RUNNING) {
            drawMenuButton(g,getSettingsButtonBounds(panelWidth),"设置",
                    hoveredButton==MenuButton.SETTINGS);
        }else if(state==GameThread.GameState.PAUSED) {
            drawSettings(g,panelWidth,panelHeight,hoveredButton);
        }else if(state==GameThread.GameState.START
                || state==GameThread.GameState.FAILED
                || state==GameThread.GameState.LEVEL_CLEAR
                || state==GameThread.GameState.VICTORY) {
            drawStateMessage(g,panelWidth,panelHeight,state);
        }
    }

    //按原枚举顺序绘制经过摄像机平移的世界元素
    private void drawWorld(Graphics2D g) {
        Graphics2D worldGraphics=(Graphics2D)g.create();
        try {
            worldGraphics.translate(-camera.getX(),-camera.getY());
            for(GameElement ge:GameElement.values()) {
                if(ge==GameElement.UI) {
                    continue;
                }
                for(ElementObj obj:em.getElementSnapshot(ge)) {
                    obj.showElement(worldGraphics);
                }
            }
        }finally {
            worldGraphics.dispose();
        }
    }

    //使用未平移的画笔绘制固定 UI 元素
    private void drawUiElements(Graphics2D g) {
        for(ElementObj obj:em.getElementSnapshot(GameElement.UI)) {
            obj.showElement(g);
        }
    }

    //绘制主菜单背景和三个操作按钮
    private void drawMainMenu(Graphics2D g,int panelWidth,int panelHeight,
                              MenuButton hoveredButton) {
        ImageIcon background=GameLoad.getImage("ui.menu.start");
        drawCoverImage(g,background.getImage(),panelWidth,panelHeight);
        drawMenuButton(g,getMainMenuButtonBounds(0,panelWidth),"开始游戏",
                hoveredButton==MenuButton.START_GAME);
        drawMenuButton(g,getMainMenuButtonBounds(1,panelWidth),"操作说明",
                hoveredButton==MenuButton.CONTROLS);
        drawMenuButton(g,getMainMenuButtonBounds(2,panelWidth),"退出游戏",
                hoveredButton==MenuButton.EXIT_GAME);
    }

    //绘制操作说明和左上角返回按钮
    private void drawControls(Graphics2D g,int panelWidth,int panelHeight,
                              MenuButton hoveredButton) {
        ImageIcon background=GameLoad.getImage("ui.menu.start");
        drawCoverImage(g,background.getImage(),panelWidth,panelHeight);

        Graphics2D content=(Graphics2D)g.create();
        try {
            content.setColor(new Color(0,0,0,180));
            content.fillRoundRect(170,120,560,350,20,20);
            content.setColor(Color.WHITE);
            content.setFont(new Font("Microsoft YaHei",Font.BOLD,30));
            drawCenteredString(content,"操作说明",170,panelWidth);
            content.setFont(new Font("Microsoft YaHei",Font.PLAIN,21));
            String[] lines={
                    "A / D 或方向键：左右移动",
                    "W 或上方向键：跳跃",
                    "S 或下方向键：下蹲",
                    "J：射击",
                    "K：投掷手雷",
                    "P：打开 / 关闭设置",
                    "R：失败后重新开始",
                    "Esc：返回上一界面 / 打开设置"
            };
            int textY=210;
            for(String line:lines) {
                content.drawString(line,225,textY);
                textY+=30;
            }
        }finally {
            content.dispose();
        }

        drawMenuButton(g,getBackButtonBounds(),"返回",
                hoveredButton==MenuButton.BACK);
    }

    //绘制关卡选择背景、两个关卡区域和左上角返回按钮
    private void drawLevelSelect(Graphics2D g,int panelWidth,int panelHeight,
                                 MenuButton hoveredButton) {
        ImageIcon background=GameLoad.getImage("ui.menu.levelSelect");
        drawCoverImage(g,background.getImage(),panelWidth,panelHeight);

        Graphics2D levelGraphics=(Graphics2D)g.create();
        try {
            drawLevelOption(levelGraphics,getLevelOneBounds(panelWidth),
                    MenuButton.LEVEL_ONE,"点击进入",hoveredButton);
            drawLevelOption(levelGraphics,getLevelTwoBounds(panelWidth),
                    MenuButton.LEVEL_TWO,"点击进入",hoveredButton);
        }finally {
            levelGraphics.dispose();
        }

        drawMenuButton(g,getBackButtonBounds(),"返回",
                hoveredButton==MenuButton.BACK);
    }

    //绘制可点击的关卡区域
    private void drawLevelOption(Graphics2D g,Rectangle bounds,
                                 MenuButton button,String text,
                                 MenuButton hoveredButton) {
        if(hoveredButton==button) {
            g.setStroke(new BasicStroke(4));
            g.setColor(new Color(255,150,30));
            g.drawRoundRect(bounds.x,bounds.y,bounds.width,bounds.height,18,18);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Microsoft YaHei",Font.BOLD,20));
        FontMetrics metrics=g.getFontMetrics();
        g.drawString(text,bounds.x+bounds.width-metrics.stringWidth(text)-20,
                bounds.y+bounds.height-18);
    }

    //在当前世界上方绘制暂停设置界面
    private void drawSettings(Graphics2D g,int panelWidth,int panelHeight,
                              MenuButton hoveredButton) {
        Graphics2D overlay=(Graphics2D)g.create();
        try {
            overlay.setColor(new Color(0,0,0,175));
            overlay.fillRect(0,0,panelWidth,panelHeight);
            overlay.setColor(Color.WHITE);
            overlay.setFont(new Font("Microsoft YaHei",Font.BOLD,42));
            drawCenteredString(overlay,"游戏设置",190,panelWidth);
        }finally {
            overlay.dispose();
        }

        drawMenuButton(g,getBackButtonBounds(),"返回游戏",
                hoveredButton==MenuButton.RETURN_GAME);
        drawMenuButton(g,getHomeButtonBounds(panelWidth),"回到主页",
                hoveredButton==MenuButton.HOME);
        drawMenuButton(g,getSettingsExitButtonBounds(panelWidth),"退出游戏",
                hoveredButton==MenuButton.EXIT_GAME);
    }

    //绘制带悬停效果的金属风格菜单按钮
    private void drawMenuButton(Graphics2D g,Rectangle bounds,String text,
                                boolean hovered) {
        Graphics2D buttonGraphics=(Graphics2D)g.create();
        try {
            if(hovered) {
                buttonGraphics.setColor(new Color(100,55,10,230));
            }else {
                buttonGraphics.setColor(new Color(15,20,16,210));
            }
            buttonGraphics.fillRoundRect(bounds.x,bounds.y,bounds.width,bounds.height,16,16);
            if(hovered) {
                buttonGraphics.setColor(new Color(255,175,45));
            }else {
                buttonGraphics.setColor(new Color(210,120,25));
            }
            buttonGraphics.setStroke(new BasicStroke(2));
            buttonGraphics.drawRoundRect(bounds.x,bounds.y,bounds.width,bounds.height,16,16);
            buttonGraphics.setFont(new Font("Microsoft YaHei",Font.BOLD,24));
            buttonGraphics.setColor(Color.WHITE);
            FontMetrics metrics=buttonGraphics.getFontMetrics();
            int textX=bounds.x+(bounds.width-metrics.stringWidth(text))/2;
            int textY=bounds.y+(bounds.height-metrics.getHeight())/2+metrics.getAscent();
            buttonGraphics.drawString(text,textX,textY);
        }finally {
            buttonGraphics.dispose();
        }
    }

    //保持图片宽高比并裁掉超出面板的部分
    private void drawCoverImage(Graphics2D g,Image image,
                                int panelWidth,int panelHeight) {
        int imageWidth=image.getWidth(null);
        int imageHeight=image.getHeight(null);
        double scaleX=(double)panelWidth/imageWidth;
        double scaleY=(double)panelHeight/imageHeight;
        double scale=Math.max(scaleX,scaleY);
        int drawWidth=(int)Math.ceil(imageWidth*scale);
        int drawHeight=(int)Math.ceil(imageHeight*scale);
        int drawX=(panelWidth-drawWidth)/2;
        int drawY=(panelHeight-drawHeight)/2;
        g.drawImage(image,drawX,drawY,drawWidth,drawHeight,null);
    }

    //在固定窗口坐标绘制玩家生命值和关卡信息
    private void drawHud(Graphics2D g,int panelWidth) {
        int hp=0;
        List<ElementObj> plays=em.getElementSnapshot(GameElement.PLAY);
        if(!plays.isEmpty()) {
            hp=((Play)plays.get(0)).getHp();
        }

        Graphics2D hud=(Graphics2D)g.create();
        try {
            hud.setFont(new Font("SansSerif",Font.BOLD,20));
            drawHudText(hud,"HP: "+hp,20,30);
            drawHudText(hud,"LEVEL: "+gameThread.getCurrentLevel(),20,55);
            drawBossHud(hud,panelWidth);
        }finally {
            hud.dispose();
        }
    }

    //读取当前 Boss 并绘制固定在窗口顶部的生命条
    private void drawBossHud(Graphics2D g,int panelWidth) {
        List<ElementObj> bosses=em.getElementSnapshot(GameElement.BOSS);
        if(bosses.isEmpty()) {
            trackedBoss=null;
            bossMaxHp=0;
            return;
        }

        RoleObj boss=(RoleObj)bosses.get(0);
        if(trackedBoss!=boss) {
            trackedBoss=boss;
            bossMaxHp=boss.getHp();
        }
        if(boss.getHp()>bossMaxHp) {
            bossMaxHp=boss.getHp();
        }
        if(boss instanceof AbstractBoss && !((AbstractBoss)boss).isActive()) {
            return;
        }
        if(bossMaxHp<=0) {
            return;
        }

        int currentHp=Math.max(0,Math.min(boss.getHp(),bossMaxHp));
        int barWidth=320;
        int barHeight=20;
        int barX=(panelWidth-barWidth)/2;
        int barY=22;
        int currentWidth=(int)((long)barWidth*currentHp/bossMaxHp);

        g.setFont(new Font("SansSerif",Font.BOLD,14));
        g.setColor(Color.WHITE);
        g.drawString("BOSS",barX,barY-5);
        g.setColor(Color.BLACK);
        g.fillRect(barX-2,barY-2,barWidth+4,barHeight+4);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX,barY,barWidth,barHeight);
        g.setColor(Color.RED);
        g.fillRect(barX,barY,currentWidth,barHeight);

        String hpText=currentHp+" / "+bossMaxHp;
        FontMetrics metrics=g.getFontMetrics();
        int textX=barX+(barWidth-metrics.stringWidth(hpText))/2;
        int textY=barY+(barHeight-metrics.getHeight())/2+metrics.getAscent();
        g.setColor(Color.WHITE);
        g.drawString(hpText,textX,textY);
    }

    //使用黑色阴影绘制清晰的 HUD 文字
    private void drawHudText(Graphics2D g,String text,int x,int y) {
        g.setColor(Color.BLACK);
        g.drawString(text,x+2,y+2);
        g.setColor(Color.WHITE);
        g.drawString(text,x,y);
    }

    //绘制关卡开始或失败状态提示
    private void drawStateMessage(Graphics2D g,int panelWidth,int panelHeight,
                                  GameThread.GameState state) {
        String title;
        String message;
        if(state==GameThread.GameState.START) {
            title="METAL SLUG";
            message="Press ENTER to Start";
        }else if(state==GameThread.GameState.LEVEL_CLEAR) {
            title="MISSION COMPLETE";
            message="Press ENTER for Next Mission";
        }else if(state==GameThread.GameState.VICTORY) {
            title="ALL MISSIONS COMPLETE";
            message="Press ENTER to Main Menu";
        }else {
            title="MISSION FAILED";
            message="Press R to Restart";
        }

        Graphics2D overlay=(Graphics2D)g.create();
        try {
            overlay.setColor(new Color(0,0,0,150));
            overlay.fillRect(0,0,panelWidth,panelHeight);
            overlay.setColor(Color.WHITE);
            overlay.setFont(new Font("SansSerif",Font.BOLD,42));
            drawCenteredString(overlay,title,260,panelWidth);
            overlay.setFont(new Font("SansSerif",Font.PLAIN,22));
            drawCenteredString(overlay,message,310,panelWidth);
        }finally {
            overlay.dispose();
        }
    }

    //按照当前字体将文字绘制在窗口水平方向中央
    private void drawCenteredString(Graphics2D g,String text,int y,int panelWidth) {
        FontMetrics metrics=g.getFontMetrics();
        int x=(panelWidth-metrics.stringWidth(text))/2;
        g.drawString(text,x,y);
    }

    //获取主菜单指定按钮的绘制和命中区域
    private Rectangle getMainMenuButtonBounds(int index,int panelWidth) {
        int buttonWidth=250;
        int buttonHeight=54;
        int x=panelWidth-310;
        int y=300+index*70;
        return new Rectangle(x,y,buttonWidth,buttonHeight);
    }

    //获取关卡选择中的第一关区域
    private Rectangle getLevelOneBounds(int panelWidth) {
        return new Rectangle(35,105,panelWidth-70,195);
    }

    //获取关卡选择中的第二关区域
    private Rectangle getLevelTwoBounds(int panelWidth) {
        return new Rectangle(35,325,panelWidth-70,190);
    }

    //获取左上角返回按钮区域
    private Rectangle getBackButtonBounds() {
        return new Rectangle(20,20,120,42);
    }

    //获取游戏内设置按钮区域
    private Rectangle getSettingsButtonBounds(int panelWidth) {
        return new Rectangle(panelWidth-120,20,100,42);
    }

    //获取设置界面的回到主页按钮区域
    private Rectangle getHomeButtonBounds(int panelWidth) {
        return new Rectangle((panelWidth-250)/2,285,250,54);
    }

    //获取设置界面的退出游戏按钮区域
    private Rectangle getSettingsExitButtonBounds(int panelWidth) {
        return new Rectangle((panelWidth-250)/2,355,250,54);
    }
}

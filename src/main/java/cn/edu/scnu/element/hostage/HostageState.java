package cn.edu.scnu.element.hostage;

//人质当前行为状态
public enum HostageState {
    IDLE, //等待玩家救援
    RESCUING, //获救后的起身动画
    RUNNING, //起身后向左侧逃离
    SAVED //已经离开当前视口
}

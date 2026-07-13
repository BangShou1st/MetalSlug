女主角素材目录（单角色轻量结构）

本项目只保留一名女主角，不再保存 male/female 两套重复目录。

目录说明：
- stand/：默认站立静帧。
- idle/：延迟触发一次的特殊待机动画。
- run/：跑动动画。
- jump/：跳跃动画。
- crouch/：蹲姿静帧。
- shoot/stand/：站姿水平射击，枪口抬起后回落。
- shoot/crouch/：蹲姿水平射击。
- throw/：投掷手榴弹动画。

所有动作只保存朝右版本；面朝左时由 Java Graphics2D 水平翻转。
所有 frame_XX.png 使用统一脚底锚点，按编号顺序播放。

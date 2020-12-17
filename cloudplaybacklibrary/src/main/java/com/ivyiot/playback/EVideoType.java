package com.ivyiot.playback;

/**
 * 录像类型
 */
public enum EVideoType {
    LIVE(0),
    TIMER(1),
    MOTION(2),
    SOUND(3),
    TEMPERATURE(4),
    HUMIDITY(5),
    IO(6),
    HUMAN(7),

    CrossLine(9),//越线
    DoorBellLeave(10),//留言
    FACE(11),//人脸
    DoorBellAlarm(12);//门铃

    private int _type;

    EVideoType(int type) {
        _type = type;
    }

    /**
     * 获取对应的类型
     */
    public int value() {
        return this._type;
    }

    /**
     * 获取录像类型
     *
     * @return
     */
    public static EVideoType getCloudVideoType(int type) {
        switch (type) {
            //正常情况——淡蓝色
            case 0:
                return EVideoType.LIVE;
            case 1:
                return EVideoType.TIMER;
            //移动侦测——橘黄色 #ac6a00
            case 2:
                return EVideoType.MOTION;
            // 声音侦测——深蓝色#06316b
            case 3:
                return EVideoType.SOUND;
            // 温度侦测——紫粉色#a1628f
            case 4:
                return EVideoType.TEMPERATURE;
            //湿度侦测——红色 #008f9b
            case 5:
                return EVideoType.HUMIDITY;
            // IO侦测——棕色#522e08
            case 6:
                return EVideoType.IO;
            //人形侦测  #b62b00
            case 7:
                return EVideoType.HUMAN;
            //其他     #c6b62d

            case 9:
                return EVideoType.CrossLine;
            case 10:
                return EVideoType.DoorBellLeave;
            case 11:
                return EVideoType.FACE;
            case 12:
                return EVideoType.DoorBellAlarm;
        }
        return EVideoType.MOTION;
    }
}

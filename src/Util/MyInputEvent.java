package Util;

/**
 * Directory: WarmVector_Client_Singleplayer/Main/
 * Created by Wyatt on 9/2/2016.
 */
public class MyInputEvent {

    public static final int MOUSE_UP = 0,
            MOUSE_DOWN = 1,
            KEY_UP = 2,
            KEY_DOWN = 3,
            MOUSE_MOVE = 4;

    public int type, code, x, y;

    public MyInputEvent(int _type, int _code) {
        type = _type;
        code = _code;
    }

    public MyInputEvent(int _type, int _x, int _y) {
        type = _type;
        x = _x;
        y = _y;
    }

    public MyInputEvent(int _type, int _x, int _y, int _code) {
        type = _type;
        x = _x;
        y = _y;
        code = _code;
    }

}

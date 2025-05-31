package GameState;

import Main.OutputManager;
import Main.Window;
import UI.ButtonUI;
import UI.MouseCursor;
import UI.SliderUI;
import UI.TextUI;
import Util.MyInputEvent;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by wyatt on 8/17/2015.
 */
public abstract class MenuState extends GameState {

    protected static final int MENU_WIDTH = (int) (480.0 * Window.SCALE),
            BORDER_DIST = (int) (50.0 * Window.SCALE),
            RIGHT_ALIGN_X = Main.Window.WIDTH - BORDER_DIST,
            TEXT_HEIGHT = (int) (30.0 * Window.SCALE),
            DISPLAY_DIST = (int) (ButtonUI.BUTTON_HEIGHT * 1.4);
    protected static final Font TEXT_FONT = new Font("Dotum Bold", Font.BOLD, TEXT_HEIGHT);

    protected ArrayList<ButtonUI> buttons;
    protected ArrayList<SliderUI> sliders;
    protected ArrayList<TextUI> strings;

    protected Color buttonOver;

    protected enum CurrentPage {
        MAIN,
        HELP,
        CREDITS,
        OPTIONS,
        QUIT
    }

    protected CurrentPage currentPage;

    private int startY;
    protected int drawY;

    protected Window window;

    public MenuState(GameStateManager _gsm, int _startY) {
        super(_gsm);
        window = _gsm.window;
        startY = _startY;

        buttons = new ArrayList<>();
        sliders = new ArrayList<>();
        strings = new ArrayList<>();

        buttonOver = ButtonUI.COLOR_OVER;
    }

    public void init() {
        setPage(CurrentPage.MAIN);
        gsm.cursor.setSprite(MouseCursor.CURSOR);
    }

    protected abstract void customMainInit();

    protected void setPage(CurrentPage page) {

        currentPage = page;

        drawY = startY;

        buttons.clear();
        sliders.clear();
        strings.clear();

        switch (page) {
            case MAIN:
                addButton("QUIT", ButtonUI.ButtonType.QUIT_CONFIRM);
                addButton("HELP", ButtonUI.ButtonType.HELP);
                addButton("CREDITS", ButtonUI.ButtonType.CREDITS);
                addButton("OPTIONS", ButtonUI.ButtonType.OPTIONS);
                customMainInit();
                break;

            case HELP:
                addButton("BACK", ButtonUI.ButtonType.BACK);
                addLineBreak();
                addText("enemies to progress", TEXT_FONT);
                addText("Clear the map of", TEXT_FONT);
                addText("OBJECTIVE:", ButtonUI.BUTTON_FONT);
                addLineBreak();
                addText("ESC: pause", TEXT_FONT);
                addText("R: reload", TEXT_FONT);
                addText("RIGHT CLICK: pickup/drop", TEXT_FONT);
                addText("LEFT CLICK: shoot", TEXT_FONT);
                addText("W-A-S-D: move", TEXT_FONT);
                addText("CONTROLS:", ButtonUI.BUTTON_FONT);
                break;

            case QUIT:
                addButton("NO", ButtonUI.ButtonType.BACK);
                addButton("YES", ButtonUI.ButtonType.QUIT);
                addText("Really quit?", ButtonUI.BUTTON_FONT);
                break;

            case CREDITS:
                addButton("BACK", ButtonUI.ButtonType.BACK);
                addLineBreak();
                addText("Illegal Sources", TEXT_FONT);
                addText("MUSIC BY", ButtonUI.BUTTON_FONT);
                addLineBreak();
                addText("Wyatt Ades", TEXT_FONT);
                addText("ART BY", ButtonUI.BUTTON_FONT);
                addLineBreak();
                addText("Wyatt Ades", TEXT_FONT);
                addText("CODING BY", ButtonUI.BUTTON_FONT);
                break;

            case OPTIONS:
                addButton("BACK", ButtonUI.ButtonType.BACK);

                addSlider("Fix Bugs", "fix_bugs", new String[] { "Off", "On" });
                addSlider("Cave Mode", "cave_mode", new String[] { "Off", "On" });
                addSlider("Quality", "quality", new String[] { "Good", "Great" });
                addSlider("Music Level", "music_volume", new String[] { "0", "25", "50", "75", "100" });
                addSlider("SFX Level", "sfx_volume", new String[] { "0", "25", "50", "75", "100" });
                break;
        }
    }

    private void addLineBreak() {
        drawY -= DISPLAY_DIST;
    }

    protected void addText(String line, Font font) {
        strings.add(new TextUI(line, RIGHT_ALIGN_X, drawY, font));
        addLineBreak();
    }

    protected void addButton(String name, ButtonUI.ButtonType value) {
        buttons.add(new ButtonUI(name, RIGHT_ALIGN_X, drawY, ButtonUI.BUTTON_FONT, value));
        addLineBreak();
    }

    protected void addSlider(String text, String name, String[] options) {
        sliders.add(
                new SliderUI(RIGHT_ALIGN_X, drawY - DISPLAY_DIST, text, name, options, OutputManager.getSetting(name)));
        addLineBreak();
    }

    protected void buttonOutcome(ButtonUI.ButtonType value) {
        switch (value) {
            case OPTIONS:
                setPage(CurrentPage.OPTIONS);
                break;
            case HELP:
                setPage(CurrentPage.HELP);
                break;
            case NEXTLEVEL:
                gsm.setState(GameStateManager.FADEOUT, GameStateManager.TOP);
                break;
            case CONTINUE:
                gsm.setState(GameStateManager.FADEOUT, GameStateManager.TOP);
                break;
            case QUIT_CONFIRM:
                setPage(CurrentPage.QUIT);
                break;
            case QUIT:
                gsm.quit();
                break;
            case RESUME:
                gsm.unloadState(GameStateManager.TOP);
                break;
            case NEWGAME:
                OutputManager.setSetting("level", 1);
                gsm.setState(GameStateManager.FADEOUT, GameStateManager.TOP);
                break;
            case RESTART:
                gsm.setState(GameStateManager.PLAY, GameStateManager.MAIN);
                break;
            case BACK:
                setPage(CurrentPage.MAIN);
                break;
            case CREDITS:
                setPage(CurrentPage.CREDITS);
                break;
            case MAINMENU:
                gsm.setState(GameStateManager.MAINMENU, GameStateManager.MAIN);
                gsm.unloadState(GameStateManager.TOP);
                gsm.cursor.setSprite(MouseCursor.CURSOR);
                break;
        }
    }

    public void draw(Graphics2D g) {
        for (SliderUI s : sliders) {
            s.draw(g);
        }
        for (TextUI t : strings) {
            t.draw(g);
        }
        for (ButtonUI b : buttons) {
            if (b.mouseOver(gsm.cursor.x, gsm.cursor.y)) {
                b.setColor(buttonOver);
            } else {
                b.setColor(ButtonUI.COLOR_DEFAULT);
            }
            b.draw(g);
        }
    }

    private SliderUI currentSlider = null;

    public void inputHandle(MyInputEvent event) {

        switch (event.type) {
            case MyInputEvent.MOUSE_MOVE:

                gsm.cursor.setPosition(event.x, event.y);

                if (currentSlider != null) {
                    currentSlider.slide(gsm.cursor.x - currentSlider.dragPos);
                }

                break;
            case MyInputEvent.MOUSE_DOWN:

                gsm.cursor.setPosition(event.x, event.y);

                if (event.code == MouseEvent.BUTTON1) {
                    for (ButtonUI b : buttons) {
                        if (b.mouseOver) {
                            buttonOutcome(b.type);
                            break;
                        }
                    }

                    if (currentSlider == null) {
                        for (SliderUI s : sliders) {
                            if (s.overBox(gsm.cursor.x, gsm.cursor.y)) {
                                s.pressed = true;
                                s.setDragPos(gsm.cursor.x);
                                currentSlider = s;
                                break;
                            }
                        }
                    }
                }

                break;
            case MyInputEvent.MOUSE_UP:

                gsm.cursor.setPosition(event.x, event.y);

                if (event.code == MouseEvent.BUTTON1) {
                    if (currentSlider != null) {
                        currentSlider.snapSlider();
                        OutputManager.setSetting(currentSlider.name, currentSlider.current_option);
                        switch (currentSlider.name) {
                            case "sfx_volume":
                            case "music_volume":
                                gsm.audioManager.setVolume(OutputManager.getSetting("sfx_volume"),
                                        OutputManager.getSetting("music_volume"));
                                break;
                            case "quality":
                                window.setQuality(OutputManager.getSetting("quality") == 1);
                                break;
                            case "fix_bugs":
                                currentSlider.set(0);
                                break;
                        }
                        currentSlider = null;
                    }
                }

                break;
        }

    }

}

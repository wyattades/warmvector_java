package GameState;

import Main.OutputManager;
import Main.Window;
import UI.ButtonUI;
import UI.InteractiveWave;
import Util.MyInputEvent;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Directory: WarmVector_Client_Singleplayer/${PACKAGE_NAME}/
 * Created by Wyatt on 1/25/2015.
 * Last modified by ${AUTHOR_NAME} on ${DATE_MODIFIED}
 */
public class StartMenuState extends MenuState {

    // Private constants
    public static final Color ACCENT_COLOR = new Color(22, 20, 22);
    private static final int LOGO_HEIGHT = (int) (210.0 * Window.SCALE);
    private static final Font FONT_LOGO = new Font("Dotum Bold", Font.BOLD, LOGO_HEIGHT),
            FONT_SUBLOGO = new Font("Dotum Bold", Font.BOLD, (int) (LOGO_HEIGHT * 0.18));

    private InteractiveWave interactiveWave;
    private float backgroundHue;

    public StartMenuState(GameStateManager _gsm) {
        super(_gsm, Window.HEIGHT - BORDER_DIST);
        interactiveWave = new InteractiveWave(MENU_WIDTH, ACCENT_COLOR);
    }

    public void load() {
        gsm.assetManager.loadAssets(new String[] { "start_menu.mp3" });
    }

    public void init() {
        super.init();

        backgroundHue = 0.0f;

        interactiveWave.init();

        gsm.cursor.setMouse((int) (Main.Window.WIDTH * 0.5), (int) (Window.HEIGHT * 0.5));

        gsm.audioManager.playSong("start_menu.mp3");
    }

    protected void customMainInit() {
        addButton("NEW GAME", ButtonUI.ButtonType.NEWGAME);
        if (OutputManager.getSetting("level") > 1)
            addButton("CONTINUE", ButtonUI.ButtonType.CONTINUE);
    }

    public void unload() {
    }

    public void draw(Graphics2D g) {

        g.setColor(buttonOver);
        g.fillRect(0, 0, Window.WIDTH, Window.HEIGHT);

        interactiveWave.draw(g);

        super.draw(g);

        int temp_fix = currentPage == CurrentPage.MAIN ? 0 : -800;

        boolean beginButtonHover = false;
        for (ButtonUI b : buttons) {
            if (b.mouseOver && (b.type == ButtonUI.ButtonType.NEWGAME || b.type == ButtonUI.ButtonType.CONTINUE)) {
                beginButtonHover = true;
                break;
            }
        }

        // Draw the "W" & "V" title
        int drawX = (int) (Window.WIDTH - MENU_WIDTH * 0.5); // TODO: change this and fix weird lag phenomenon

        if (beginButtonHover) {
            g.setColor(buttonOver);
            g.setFont(FONT_SUBLOGO);
            g.drawString("ARM", drawX + (int) (76.0 * Window.SCALE), LOGO_HEIGHT + temp_fix);
            g.drawString("ECTOR", drawX + (int) (33.0 * Window.SCALE), 2 * LOGO_HEIGHT + temp_fix);
        } else {
            g.setColor(ButtonUI.COLOR_DEFAULT);
        }

        g.setFont(FONT_LOGO);
        g.drawString("W", drawX - (int) (g.getFontMetrics().getStringBounds("W", g).getWidth() * 0.5),
                LOGO_HEIGHT + temp_fix);
        g.drawString("V", drawX - (int) (g.getFontMetrics().getStringBounds("V", g).getWidth() * 0.5),
                2 * LOGO_HEIGHT + temp_fix);

        gsm.cursor.draw(g);

    }

    public void update(double deltaTime) {

        buttonOver = Color.getHSBColor(backgroundHue, 1.0f, 1.0f);
        backgroundHue += 0.001f;
        if (backgroundHue >= 1.0f)
            backgroundHue = 0;

        interactiveWave.update(gsm.cursor.y);

    }

    public void inputHandle(MyInputEvent event) {
        super.inputHandle(event);

        if (event.type == MyInputEvent.MOUSE_DOWN && event.code == MouseEvent.BUTTON1) {
            interactiveWave.reactClick();
        } else if (event.type == MyInputEvent.KEY_DOWN && event.code == KeyEvent.VK_ESCAPE) {
            if (currentPage == CurrentPage.MAIN)
                setPage(CurrentPage.QUIT);
            else if (currentPage == CurrentPage.QUIT)
                setPage(CurrentPage.MAIN);
        }
    }

}

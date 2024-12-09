package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class EndScreen extends ScreenAdapter {
    private SpaceBattles game;

    private BitmapFont font;
    private boolean gameWon;

    private String creditsText;
    private float currentY;
    private float scrollSpeed = 25f;

    public EndScreen(SpaceBattles game, boolean gameWon) {
        this.game = game;
        this.font = new BitmapFont();
        this.font.setColor(1, 1, 1, 1);
        this.gameWon = gameWon;

        FileHandle file = Gdx.files.internal("credits.txt");
        this.creditsText = file.readString();
        this.currentY = -scrollSpeed;
    }

    @Override
    public void show() {
        System.out.println("Show EndScreen");
        game.batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        game.batch.begin();

        font.draw(game.batch, creditsText, 200, currentY);

        game.batch.setColor(0, 0, 0, 1);
        game.batch.draw(game.getEntity(SpaceBattles.RSC_SQUARE_IMG), 0, Gdx.graphics.getHeight() - 535, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        game.batch.setColor(1, 1, 1, 1);

        String mainText = gameWon ? "Congratulations!" : "Better luck next time!";
        String subText = "Close game to play again...";
        font.draw(game.batch, mainText, 400 - mainText.length() * 4, Gdx.graphics.getHeight() - 240);
        font.draw(game.batch, subText, 400 - subText.length() * 4, Gdx.graphics.getHeight() - 280);

        font.draw(game.batch, "CREDITS: ", 200, Gdx.graphics.getHeight() - 500);

        currentY += scrollSpeed * delta;
        game.batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}

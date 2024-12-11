package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

public class XPBarUI {
    SpaceBattles game;
    private ProgressBar xpBar;
    private Label levelLabel;
    private float experience = 0;

    public XPBarUI(SpaceBattles game, Stage stage) {
        this.game = game;
        xpBar = new ProgressBar(0, 1, 0.01f, true, SpaceBattles.skin);

        xpBar.setPosition(Gdx.graphics.getWidth() - 70, 30);
        xpBar.setSize(50, 200);
        xpBar.setColor(1, 1, 0, 1);

        int playerLevel = game.players[game.id].getLevel();

        Label.LabelStyle style = SpaceBattles.skin.get("default", Label.LabelStyle.class);
        levelLabel = new Label("lvl " + playerLevel, style);
        levelLabel.setPosition(Gdx.graphics.getWidth() - 80, 240);
        levelLabel.setColor(1, 1, 0, 1);

        stage.addActor(xpBar);
        stage.addActor(levelLabel);
    }

    public void update() {
        Player p = game.players[game.id];
        if (p.getExperience() == experience) return;
        experience = p.getExperience();
        int level = p.getLevel();
        if (level == 10) {
            xpBar.setValue(1);
            levelLabel.setText("maxed");
        } else {
            xpBar.setValue(experience / p.xpThreshold(level));
            levelLabel.setText("lvl " + level);
        }
    }
}

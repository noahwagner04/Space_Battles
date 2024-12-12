package nocah.spacebattles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import nocah.spacebattles.netevents.UpgradeEvent;

public class StatUpgradeUI {
    SpaceBattles game;
    private Table table;
    private Label statPointsLabel;
    private int statPoints;
    private final int[] stats = { Player.ATTACK, Player.DEFENCE, Player.SPEED, Player.BASE_DEFENCE, Player.MINIONS };
    private final String[] statNames = { "Attack", "Defence", "Speed", "Base Defence", "Minion Power" };

    public StatUpgradeUI(SpaceBattles game, Stage stage) {
        this.game = game;
        table = new Table();
        table.bottom().left();
        table.setFillParent(true);

        statPoints = game.players[game.id].getStatPoints();

        statPointsLabel = new Label("Stat Points: " + statPoints, SpaceBattles.skin);
        statPointsLabel.setColor(0.5f, 1, 0.5f, 1);
        table.add(statPointsLabel).colspan(1).padBottom(10).padLeft(10).left();
        table.row();

        for (int i = 0; i < stats.length; i++) {
            final int statType = stats[i];
            TextButton button = new TextButton("Upgrade " + statNames[i], SpaceBattles.skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.players[game.id].upgradeStat(statType, false);
                    game.sendEvent(new UpgradeEvent(game.id, (byte)statType));
                    game.playClick();
                }
            });
            button.setColor(0, 1, 0, 1);

            table.add(button).pad(5).width(350);
            table.row();
        }

        stage.addActor(table);
        update();
        table.setVisible(false);
    }

    public void update() {
        int newStatPoints = game.players[game.id].getStatPoints();
        if (newStatPoints == statPoints) return;
        statPoints = newStatPoints;
        statPointsLabel.setText("Stat Points: " + statPoints);
        table.setVisible(statPoints > 0);
    }
}

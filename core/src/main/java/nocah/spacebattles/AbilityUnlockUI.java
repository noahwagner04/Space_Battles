package nocah.spacebattles;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class AbilityUnlockUI {
    private SpaceBattles game;
    private Table table;
    private Label message;
    private int abilityCount = 0;
    public int playerLevel;
    private final String[] abilityNames = {
        "Rapid\nFire",
        "Force\nField",
        "Speed\nBoost",
        "Bomb",
        "Stealth"
    };
    private boolean[] abilitiesUnlocked = { false, false, false, false, false };
    private byte[] abilities = { Ability.RAPID_FIRE, Ability.FORCE_FIELD, Ability.DASH, Ability.BOMB, Ability.INVISIBILITY };

    public AbilityUnlockUI(SpaceBattles game, Stage stage) {
        this.game = game;
        table = new Table();
        table.top();
        table.setFillParent(true);

        message = new Label("Choose an ability!", SpaceBattles.skin);
        message.setColor(0.6f, 0.9f, 1f, 1);
        table.add(message).colspan(5).padTop(50).padBottom(10);
        table.row();

        // Display buttons for unlocking abilities
        for (int i = 0; i < abilityNames.length; i++) {
            if (!abilitiesUnlocked[i]) {
                final int abilityIndex = i;
                TextButton button = new TextButton(abilityNames[i], SpaceBattles.skin);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        unlockAbility(abilityIndex);
                    }
                });
                button.setColor(0, 0, 1, 1);
                table.add(button).pad(5).width(150).height(100);
            }
        }

        playerLevel = game.players[game.id].getLevel();

        stage.addActor(table);
        update();
        table.setVisible(false);
    }

    // Unlocks an ability and updates the UI
    public void unlockAbility(int abilityIndex) {
        if (abilityCount < 2 && !abilitiesUnlocked[abilityIndex]) {
            abilitiesUnlocked[abilityIndex] = true;
            abilityCount++;

            game.players[game.id].setAbility(abilityCount, abilities[abilityIndex]);
            table.setVisible(false);
        }
    }

    public void update() {
        int newPlayerLevel = game.players[game.id].getLevel();

        if (newPlayerLevel == playerLevel) return;
        playerLevel = newPlayerLevel;

        if (playerLevel >= 5 && abilityCount < 1) {
            table.setVisible(true);
            return;
        }

        if (playerLevel == 10 && abilityCount < 2) {
            table.setVisible(true);
        }
    }
}

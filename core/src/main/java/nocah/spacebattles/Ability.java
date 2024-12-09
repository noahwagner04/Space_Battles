package nocah.spacebattles;

import nocah.spacebattles.netevents.AbilityEvent;

public class Ability {
    public static final byte BOMB = 1;
    public static final byte DASH = 2;
    public static final byte FORCE_FIELD = 3;
    public static final byte INVISIBILITY = 4;
    public static final byte RAPID_FIRE = 5;

    byte abilityID;
    byte abilityNum;

    Player player;
    SpaceBattles game;

    float time = 0;
    float cooldown = 0;
    float interval = 0;

    boolean isReady = false;
    boolean isActive = false;

    public Ability(Player player, SpaceBattles game) {
        this.player = player;
        this.game = game;
    }

    public void reset() {
        if (isActive) deactivate();

        time = 0;
        isReady = false;
        isActive = false;
    }

    public void update(float delta) {
        if (time < cooldown) {
            time += delta;
            return;
        }

        isReady = true;

        if (!isActive) return;

        if (time < cooldown + interval) {
            time += delta;
            return;
        }

        time = 0;
        isReady = false;
        deactivate();
    }

    public void activate() {
        if (!isReady || isActive) return;
        isActive = true;
        onActivate();
        game.sendEvent(new AbilityEvent(player.id, abilityID, abilityNum, (byte)1));
    }

    public void deactivate() {
        if (!isActive) return;
        isActive = false;
        onDeactivate();
        game.sendEvent(new AbilityEvent(player.id, abilityID, abilityNum, (byte)0));
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }
}

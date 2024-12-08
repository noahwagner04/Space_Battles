package nocah.spacebattles;

public class Ability {
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
    }

    public void deactivate() {
        if (!isActive) return;
        isActive = false;
        onDeactivate();
    }

    public void onActivate() {
    }

    public void onDeactivate() {
    }
}

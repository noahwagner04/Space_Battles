package nocah.spacebattles;

import com.badlogic.gdx.math.Shape2D;

public interface Damageable {
    float getHealth();
    boolean damage(float amount);
    void heal(float amount);
    Shape2D getDamageArea();
}

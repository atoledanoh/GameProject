package entityComponentSystem.system;

import com.atoledano.producegame.ProduceGame;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import entityComponentSystem.ECSEngine;
import entityComponentSystem.component.B2DComponent;
import entityComponentSystem.component.PlayerComponent;

public class PlayerCameraSystem extends IteratingSystem {
    private final OrthographicCamera gameCamera;

    public PlayerCameraSystem(final ProduceGame context) {
        super(Family.all(PlayerComponent.class, B2DComponent.class).get());
        gameCamera = context.getGameCamera();
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        gameCamera.position.set(ECSEngine.b2DComponentMapper.get(entity).body.getPosition(), 0);
        gameCamera.update();
    }
}

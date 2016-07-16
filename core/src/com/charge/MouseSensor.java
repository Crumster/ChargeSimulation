package com.charge;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

/**
 *
 */
public class MouseSensor {
    private World world;
    private Body body;
    private Fixture fixture;

    public MouseSensor(World world, Vector3 position){
        this.world = world;

        //Body definition
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(position.x, position.y));
        bodyDef.fixedRotation = true;
        bodyDef.bullet = true;
        bodyDef.allowSleep = false;

        body = world.createBody(bodyDef);
        body.setUserData(this);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.05f);
        fixture = body.createFixture(shape, 1f);
        fixture.setUserData("mouseSensor");
        fixture.setSensor(true);

        shape.dispose();
    }

    /**
     *
     * @param position
     */
    public void setPosition(Vector3 position){
        body.setTransform(new Vector2(position.x, position.y), 0);
    }
}

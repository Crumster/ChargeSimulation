package com.charge;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;

/**
 *
 */
public class Charge {
    private static int lastID = 0;
    private int ID;

    private World world;
    private Body body;
    private Fixture fixture;

    private int charge;
    private boolean hovered = false;

    private Vector2 netForce = new Vector2(0, 0);
    private Vector2 resetPosition = new Vector2();

    public Charge(World world, Vector3 position, int charge){
        this.ID = lastID++;

        this.world = world;
        this.charge = charge;

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
        shape.setRadius(0.25f + Math.abs(charge) / 4f);
        fixture = body.createFixture(shape, 1f);
        fixture.setUserData("charge");
        shape.dispose();
    }

    public void update(){

    }

    /**
     *
     * @param shapeRenderer
     */
    public void render(ShapeRenderer shapeRenderer){
        if(getCharge() < 0){
            shapeRenderer.setColor(Color.BLUE);
        } else if(getCharge() > 0){
            shapeRenderer.setColor(Color.RED);
        } else{
            shapeRenderer.setColor(Color.GRAY);
        }
        shapeRenderer.circle(body.getPosition().x, body.getPosition().y, 0.25f + Math.abs(charge) / 4f, 24);

        //draw plus/minus/neutral
        shapeRenderer.setColor(Color.WHITE);
        if(charge != 0){
            shapeRenderer.rectLine(body.getPosition().x - (0.25f + Math.abs(charge) / 4f), body.getPosition().y, body.getPosition().x + (0.25f + Math.abs(charge) / 4f), body.getPosition().y, 0.25f + Math.abs(charge) / 64f);
            if(charge > 0){
                shapeRenderer.rectLine(body.getPosition().x, body.getPosition().y - (0.25f + Math.abs(charge) / 4f), body.getPosition().x, body.getPosition().y + (0.25f + Math.abs(charge) / 4f), 0.25f + Math.abs(charge) / 64f);
            }
        }
    }

    /**
     *
     * @param shapeRenderer
     * @param charges
     */
    public void renderForces(ShapeRenderer shapeRenderer, ArrayList<Charge> charges){
        netForce.set(0, 0);

        if(charge != 0){
            shapeRenderer.setColor(Color.WHITE);

            for(Charge c : charges){
                if(c.getID() != ID){
                    Vector2 f = new Vector2(c.getPosition().x - getPosition().x, c.getPosition().y - getPosition().y).setLength((float) Math.pow(10, 2) * getCharge() * c.getCharge() / (float) Math.pow(getDistance(c), 2));
                    if(Math.signum(getCharge()) == Math.signum(c.getCharge())){ //Repel if charges are the same sign
                        f.x = -f.x;
                        f.y = -f.y;
                    }

                    netForce.add(f);
                    shapeRenderer.line(getPosition().x, getPosition().y, getPosition().x + f.x, getPosition().y + f.y);
                    Vector2 arrow1 = new Vector2(-0.25f, -0.125f);
                    arrow1.rotate(f.angle());
                    Vector2 arrow2 = new Vector2(-0.25f, 0.125f);
                    arrow2.rotate(f.angle());
                    shapeRenderer.line(getPosition().x + f.x, getPosition().y + f.y, getPosition().x + f.x + arrow1.x, getPosition().y + f.y + arrow1.y);
                    shapeRenderer.line(getPosition().x + f.x, getPosition().y + f.y, getPosition().x + f.x + arrow2.x, getPosition().y + f.y + arrow2.y);
                }
            }

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.line(getPosition().x, getPosition().y, getPosition().x + netForce.x, getPosition().y + netForce.y);
            Vector2 arrow1 = new Vector2(-0.5f, -0.25f);
            arrow1.rotate(netForce.angle());
            Vector2 arrow2 = new Vector2(-0.5f, 0.25f);
            arrow2.rotate(netForce.angle());
            shapeRenderer.line(getPosition().x + netForce.x, getPosition().y + netForce.y, getPosition().x + netForce.x + arrow1.x, getPosition().y + netForce.y + arrow1.y);
            shapeRenderer.line(getPosition().x + netForce.x, getPosition().y + netForce.y, getPosition().x + netForce.x + arrow2.x, getPosition().y + netForce.y + arrow2.y);
        }
    }

    public void applyNetForce(){
        body.applyForce(netForce, body.getPosition(), true);
    }

    public void reset(){
        body.setLinearVelocity(0, 0);
        setPosition(new Vector3(resetPosition, 0));
    }

    public void setResetPosition(){
        resetPosition.set(body.getPosition());
    }

    /**
     * Gets the distance between two charges
     * @param c
     * @return
     */
    public double getDistance(Charge c){
        return Math.sqrt(Math.pow(c.getPosition().x - getPosition().x, 2) + Math.pow(c.getPosition().y - getPosition().y, 2));
    }

    /**
     *
     * @param position
     */
    public void setPosition(Vector3 position){
        body.setTransform(new Vector2(position.x, position.y), 0);
    }

    public int getCharge(){
        return charge;
    }

    public int getID(){
        return ID;
    }

    public Vector2 getPosition(){
        return body.getPosition();
    }

    public void setHovered(boolean hovered){
        this.hovered = hovered;
    }

    public boolean isHovered(){
        return hovered;
    }

    public void decreaseCharge(){
        charge--;
        adjustFixture();
    }

    public void increaseCharge(){
        charge++;
        adjustFixture();
    }

    private void adjustFixture(){
        body.destroyFixture(fixture);
        CircleShape shape = new CircleShape();
        shape.setRadius(0.25f + Math.abs(charge) / 4f);
        fixture = body.createFixture(shape, 1f);
        fixture.setUserData("charge");
        shape.dispose();
    }

    public void destroyBody(){
        world.destroyBody(body);
    }
}

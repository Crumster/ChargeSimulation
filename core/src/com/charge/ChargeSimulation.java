package com.charge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;

public class ChargeSimulation extends ApplicationAdapter implements InputProcessor {
	//Box2d stuff
	private Box2DDebugRenderer debugRenderer;
	private World world;

	private ShapeRenderer shapeRenderer;

	private OrthographicCamera camera;
	private FitViewport viewport;

	private boolean debug = false;
	private boolean paused = true;
	private boolean dragging = false;
	private boolean changeChargeMode = false;

	private MouseSensor mouseSensor;
	private Vector3 mousePos = new Vector3();

	private ArrayList<Charge> charges = new ArrayList<Charge>();
	
	@Override
	public void create () {
		//create the camera and setup the viewport
		camera = new OrthographicCamera();
		viewport = new FitViewport(25f, 25f, camera);
		viewport.apply();

		//set the initial position of the camera
		camera.position.set(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f, 0f);

		//setup box2d world
		debugRenderer = new Box2DDebugRenderer();
		world = new World(new Vector2(0f, 0f), true);

		shapeRenderer = new ShapeRenderer();

		mouseSensor = new MouseSensor(world, mousePos);

		Gdx.input.setInputProcessor(this);
		world.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				if(contact.getFixtureA().getUserData() == "mouseSensor" && contact.getFixtureB().getUserData() == "charge"){
					Charge c = (Charge) contact.getFixtureB().getBody().getUserData();
					if(!dragging) {
						c.setHovered(true);
					}
				}
				if(contact.getFixtureB().getUserData() == "mouseSensor" && contact.getFixtureA().getUserData() == "charge"){
					Charge c = (Charge) contact.getFixtureA().getBody().getUserData();
					if(!dragging) {
						c.setHovered(true);
					}
				}
			}

			@Override
			public void endContact(Contact contact) {
				if(contact.getFixtureA().getUserData() == "mouseSensor" && contact.getFixtureB().getUserData() == "charge"){
					Charge c = (Charge) contact.getFixtureB().getBody().getUserData();
					c.setHovered(false);
				}
				if(contact.getFixtureB().getUserData() == "mouseSensor" && contact.getFixtureA().getUserData() == "charge"){
					Charge c = (Charge) contact.getFixtureA().getBody().getUserData();
					c.setHovered(false);
				}
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {

			}
		});
	}

	public void update(){
		mouseSensor.setPosition(mousePos);

		for(Charge c : charges){
			c.update();
		}

		if(dragging){
			for(Charge c : charges){
				if(c.isHovered()){
					c.setPosition(mousePos);
				}
			}
		}

		if(!paused){
			for(Charge c : charges){
				c.applyNetForce();
			}
		}

		world.step(Gdx.graphics.getDeltaTime(), 6, 2);
		camera.update();
	}

	@Override
	public void render () {
		update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		for(Charge c : charges){
			c.render(shapeRenderer);
		}
		shapeRenderer.end();
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		for(Charge c : charges){
			c.renderForces(shapeRenderer, charges);
		}
		shapeRenderer.end();

		if(debug){
			debugRenderer.render(world, camera.combined);
		}
	}

	@Override
	public void resize(int width, int height){
		viewport.update(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose(){
		world.dispose();
		shapeRenderer.dispose();
		debugRenderer.dispose();
	}

	public boolean isChargeHovered(){
		boolean hovered = false;

		for(Charge c : charges){
			if(c.isHovered()){
				hovered = true;
			}
		}

		return hovered;
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.ESCAPE){
			Gdx.app.exit();
		}
		if(keycode == Input.Keys.GRAVE){
			debug = !debug;
		}
		if(keycode == Input.Keys.SPACE){
			if(paused){
				for(Charge c : charges) {
					c.setResetPosition();
				}
			}else{
				for(Charge c : charges){
					c.reset();
				}
			}

			paused = !paused;
		}
		if(paused && keycode == Input.Keys.Z && charges.size() > 0){
			charges.get(charges.size() - 1).destroyBody();
			charges.remove(charges.size() - 1);
		}
		if(keycode == Input.Keys.SHIFT_LEFT){
			changeChargeMode = true;
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if(keycode == Input.Keys.SHIFT_LEFT){
			changeChargeMode = false;
		}

		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		camera.unproject(mousePos.set(screenX, screenY, 0));

		if(paused){
			if(button == Input.Buttons.LEFT){
				if(changeChargeMode){
					for(Charge c : charges){
						if(c.isHovered()){
							c.decreaseCharge();
						}
					}
				} else{
					dragging = true;
				}
			}

			if(button == Input.Buttons.RIGHT){
				if(changeChargeMode){
					for(Charge c : charges){
						if(c.isHovered()){
							c.increaseCharge();
						}
					}
				} else{
					if(!isChargeHovered()){
						charges.add(new Charge(world, new Vector3(mousePos.x, mousePos.y, 0), 0));
					}
				}
			}
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		camera.unproject(mousePos.set(screenX, screenY, 0));

		if(button == Input.Buttons.LEFT){
			dragging = false;
		}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(dragging){
			camera.unproject(mousePos.set(screenX, screenY, 0));
		}

		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		camera.unproject(mousePos.set(screenX, screenY, 0));
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}

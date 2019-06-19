package com.cars;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Efekt jakiegoś buffa/debuffa. Istotne jest żeby
 * przeciążyć metody turnOn oraz turnOff. Reszta działa
 * tak jakby tego oczekiwano.
 * @author bartek
 *
 */
public class GameEffect extends Actor {
	protected int cyclesLeft;
	protected GameThread gameThread;
	private volatile boolean done = false;
	
	public GameEffect(int cycles) {
		cyclesLeft = cycles;
	}
	
	/**
	 * DO NADPISANIA
	 * @param gt
	 * @param actor
	 * @return
	 */
	public Actor turnOn(GameThread gt, Actor actor) { return this; }
	
	/**
	 * DO NADPISANIA
	 */
	public void turnOff() {}
	
	private void decrementCycles() {
		if(done)
			throw new IllegalStateException();
		
		if(--cyclesLeft <= 0) {
			turnOff();
			gameThread.onEventGame(EventType.EFFECTS_GONE, this); // informuje główną pętlę o zakończeniu efektu
			done = true;
		}
	}

	public void draw(Canvas c, int x, int y) {
		previous.draw(c, x, y);
		decrementCycles();
	}

	public void draw(Canvas c, Rect destination) {
		previous.draw(c, destination);
		decrementCycles();
	}

	public void drawNTimes(Canvas c, int startX, int startY, int repeatCount,
			int spaceBetween, boolean left) {
		previous.drawNTimes(c, startX, startY, repeatCount, spaceBetween, left);
		decrementCycles();
	}

	public Rect getRect() {
		return previous.getRect();
	}
	
	
	@Override
	public void dispose() {
		previous.dispose();
	}
	
	
}

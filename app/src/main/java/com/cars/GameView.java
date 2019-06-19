package com.cars;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Callback {

	private SurfaceHolder surfaceHolder;
	protected GameThread gameThread;
	protected GameActivity gameActivity;
	volatile boolean dimensionsKnown = false;

	public GameView(GameActivity context) {
		super(context);

		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);

		gameActivity = context;
//		gameThread = new GameThread(surfaceHolder, this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gameThread.onInputGame(event);
		
		//TODO: dodać performClick()
		
		return true;
	}
	
	/**
	 * Rozpoczyna grę (odpala GameThread).
	 */
	public void startGame() {
		gameThread.setObFactory(gameThread.getColumnsCount());
		gameThread.setRunning(true);
		gameThread.start();
	}
	
	/**
	 * Zatrzymuje grę (może nie będzie potrzebne).
	 */
	public void stopGame() {
		gameThread.setRunning(false);
		gameThread.interrupt();
	}
	
	/**
	 * Wczytuje zasoby.
	 */
	public void loadAssets() {
		gameThread.onStartupGame();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		dimensionsKnown = true;
	}

	/**
	 * Zwalnia zasoby.
	 */
	public void unloadAssets() {
		gameThread.onShutdownGame();
	}
	
	public void remakeGameThread(Bundle savedState) {
		if(gameThread != null)
			gameThread.setRunning(false);
		gameThread = new GameThread(surfaceHolder, this, savedState);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}

package com.cars;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;

public class GameActivity extends Activity {
	volatile static Bundle savedState = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameView = new GameView(this);

		setContentView(gameView);
		Log.d("GA", "onCreate finished");
		
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("GameActivity.onResume", "onResume");
		gameView.remakeGameThread(savedState);
		savedState = null;
		gameView.startGame();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("GameActivity.onPause", "onPause");
		gameView.stopGame();
		if(gameView.gameThread.players_lives > 0)
			savedState = gameView.gameThread.saveGame(null);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i("GameActivity.onRestart", "onRestart");
//		gameView.loadAssets();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i("GameActivity.onStop", "onStop");
		gameView.unloadAssets();
		finish();
	}
	
	
	public void endGameSaveScore(int score) {
		Intent intent = new Intent(this, ScoreActivity.class);
		intent.putExtra(ScoreActivity.SCORE_EXTRA_PARAM, score);
		startActivity(intent);
		finish();
	}


	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.i("GameActivity.onRestoreInstanceState", "onRestoreInstanceState");
		
		gameView.remakeGameThread(savedState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		Log.i("GameActivity.onSaveInstanceState", "onSaveInstanceState");
		gameView.gameThread.saveGame(outState);
		
	}
	

	private GameView gameView;
}

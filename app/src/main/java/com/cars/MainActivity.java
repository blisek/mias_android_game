package com.cars;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d("MainActivity.onCreate", "DONE.");
    }
    
    public void loadBuffsAndDebuffs() {
    	// ładowanie buffów/debuffów
    	// Przykład: dodatkowe życie
    	Resources res = getResources();
    	BuffOrDebuff.addBuffOrDebuff(res, R.drawable.heart, 1000, new GameEffectFactory() {
			
			@Override
			public GameEffect makeNew(final int cycles) {
				return new GameEffect(0) {
		    		
		    		@Override
		    		public Actor turnOn(GameThread gameThread, Actor actor) {
		    			++gameThread.players_lives;
		    			return this;
		    		}
		    		
		    		@Override
		    		public void turnOff() {} // nic nie robi
		    		
		    	};
			}
		} , true);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	Button btn = (Button)findViewById(R.id.main_start_game);
    	
    	if(GameActivity.savedState != null) {
    		btn.setText(getResources().getString(R.string.main_resume));
    	} else {
    		btn.setText(getResources().getString(R.string.main_start_game));
    	}
    }
    
    
    public void startGame(View v) {
    	Intent intent = new Intent(this, GameActivity.class);
    	startActivity(intent);
    }
    
    public void showHighscores(View v) {
    	Intent intent = new Intent(this, HighScoresActivity.class);
    	startActivity(intent);
    }
    
    public void endGame(View v) {
    	System.exit(0);
    }
}

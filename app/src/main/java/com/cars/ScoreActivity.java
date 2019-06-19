package com.cars;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ScoreActivity extends Activity {
	public static final String SCORE_EXTRA_PARAM = "score";
	private int score;
	private String playerName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score);
		score = getIntent().getIntExtra(SCORE_EXTRA_PARAM, 0);
		
		TextView tv = (TextView)findViewById(R.id.score_message);
		tv.setText(String.format(tv.getText().toString(), score));
	}
	
	public void saveBtnClick(View view) {
		// pobranie danych od użytkownika
		EditText eText = (EditText)findViewById(R.id.score_player_name);
		playerName = eText.getText().toString();
		if(playerName.length() == 0) return;
		
		// zapisanie wyniku w bazie danych
		GameDB gDB = new GameDB(this);
		gDB.addScore(playerName, score);
		
		// wyczyszczenie stosu aktywności i rozpoczęcie MainActivity
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}

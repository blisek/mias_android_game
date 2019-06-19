package com.cars;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Obsługuje bazę danych gry
 * @author bartek
 *
 */
public class GameDB extends SQLiteOpenHelper {
	
	public GameDB(Context context) {
		super(context, "game.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(highscoresTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// STRUKTURA BAZY NIE ZOSTANIE ZMIENIONA
	}
	
	/**
	 * Zwraca najlepsze wyniki w grze.
	 * @return lista wyników
	 */
	public List<ScoreInfo> getBestScores() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(false, highscoreTableName, highscoreColumns, 
				null, null, null, null, highscoreTableOrderBy, ""+highscoreTableLimit);
		
		List<ScoreInfo> scores = new ArrayList<ScoreInfo>(highscoreTableLimit);
		while(cursor.moveToNext())
			scores.add(new ScoreInfo(cursor.getString(0), cursor.getInt(1)));
		
		return scores;
	}
	
	public void addScore(String playerName, int score) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues cv = new ContentValues(2);
		cv.put(highscoreColumns[0], playerName);
		cv.put(highscoreColumns[1], score);
		db.insert(highscoreTableName, null, cv);
	}

	private static final String highscoresTable = 
			"CREATE TABLE scores(player varchar(100) not null, score int not null default 0);";
	private static final String[] highscoreColumns = { "player", "score" };
	private static final String highscoreTableName = "scores";
	private static final String highscoreTableOrderBy = "score DESC";
	private static final int highscoreTableLimit = 10;
}

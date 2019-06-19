package com.cars;

import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HighScoresActivity extends Activity {
	static volatile GameDB gameDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.highscores_view);
		
		if(gameDatabase == null)
			gameDatabase = new GameDB(this);

		List<ScoreInfo> scores = gameDatabase.getBestScores();
		ListView lv = (ListView) findViewById(R.id.highscores_listview);
		HighScoresAdapter hsa = new HighScoresAdapter(scores);
		lv.setAdapter(hsa);
	}

	class HighScoresAdapter extends BaseAdapter {

		private List<ScoreInfo> scores;

		public HighScoresAdapter(List<ScoreInfo> scores) {
			super();
			this.scores = scores;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return scores.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return scores.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View V = convertView;

			if (V == null) {
				LayoutInflater vi = (LayoutInflater) HighScoresActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				V = vi.inflate(R.layout.highscores_listview_element, null);
			}

			ScoreInfo sInfo = scores.get(position);
			TextView playerNameView = (TextView) V
					.findViewById(R.id.highscores_element_playername);
			TextView scoreView = (TextView) V
					.findViewById(R.id.highscores_element_score);

			playerNameView.setText(sInfo.playerName);
			scoreView.setText("" + sInfo.score);

			return V;
		}

	}
}

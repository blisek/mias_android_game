package com.cars;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Background {

	private Bitmap image;
	private int x, y, dy;
	private GameView gameView;

	public Background(Bitmap res, GameView gameView) {
		image = res;
		this.gameView = gameView;
	}

	public void update() {
		y += dy;
		if (y < -gameView.getHeight()) {
			y = 0;
		}

	}

	public void draw(Canvas canvas) {
		canvas.drawBitmap(image, x, y, null);
		if (y < 0) {
			canvas.drawBitmap(image, x, y + gameView.getHeight(), null);
			
		}
	}

	public void setVector(int dy) {
		this.dy = dy;
	}
	
	public void dispose() {
		image.recycle();
		image = null;
	}
}
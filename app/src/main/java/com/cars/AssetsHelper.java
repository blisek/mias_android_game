package com.cars;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class AssetsHelper {
	public static final float EPS = 0.00001f;
	
	/**
	 * Ładuje statycznego aktora (np. cegiełkę). Aktor będzie miał taki
	 * sam identyfikator jak id zasobu.
	 * @param res resource manager
	 * @param id identyfikator zasobu.
	 * @return obiekt StaticActor
	 */
	public static StaticActor loadStaticActor(Resources res, int id) {
		return loadStaticActor(res, id, 0, 0, false);
	}
	
	/**
	 * To samo co wyżej, ale przy każdym rysowaniu skaluje bitmapę do danych rozmiarów.
	 * @param res
	 * @param id
	 * @param scaleWidth
	 * @param scaleHeight
	 * @param keepRatio zachowuje proporcję między wymiarami jak w oryginalnej bitmapie.
	 * @return
	 */
	public static StaticActor loadStaticActor(Resources res, int id, int scaleWidth, int scaleHeight, boolean keepRatio) {
		Bitmap bmp = BitmapFactory.decodeResource(res, id);
		if(bmp == null)
			throw new NullPointerException(String.format("Bitmap id: %d failed to load", id));
		int width = scaleWidth <= 0 ? bmp.getWidth() : scaleWidth;
		int height = scaleHeight <= 0 ? bmp.getHeight() : scaleHeight;
		if(keepRatio) { // skaluje obraz jeśli został podany tylko jeden z wymiarów
			if (scaleWidth <= 0) {
				float ratio = (float)bmp.getWidth() / bmp.getHeight();
				width = Math.round(height * ratio);
			}
			else if (scaleHeight <= 0) {
				float ratio = (float)bmp.getHeight() / bmp.getWidth();
				height = Math.round(width * ratio);
			}
		}
		float scWidth = (float)width / bmp.getWidth();
		float scHeight = (float)height / bmp.getHeight();
		
		if(!((1 <= scWidth && scWidth <= 1 + EPS) || (1 <= scHeight && scHeight <= 1 + EPS))) {
			Bitmap tmp = scalePermanently(bmp, scWidth, scHeight);
			bmp.recycle();
			bmp = tmp;
		}
		return new StaticActor(bmp, width, height);
	}
	
	/**
	 * Wczytuje bitmapę traktując ją jako animację (ułożone w poziomie jedna za drugą
	 * klatki o takich samych wymiarach każda) i tworzy instancję AnimatedActor.
	 * @param res menadżer zasobów
	 * @param id identyfikator zasobu
	 * @param frameCount liczba klatek animacji
	 * @return obiekt AnimatedActor
	 */
	public static AnimatedActor loadAnimatedActor(Resources res, int id, int frameCount) {
		return loadAnimatedActor(res, id, frameCount, 0, 0, false);
	}
	
	/**
	 * J.w., ale dodatkowo skaluje każdą klatkę do danych wymiarów.
	 * @param res
	 * @param id
	 * @param frameCount
	 * @param scale
	 * @param scaleToWidth
	 * @param scaleToHeight
	 * @param keepRatio zachowuje proporcję między wymiarami jak w oryginalnej bitmapie.
	 * @return
	 */
	public static AnimatedActor loadAnimatedActor(Resources res, int id, int frameCount, int scaleToWidth, int scaleToHeight, boolean keepRatio) {
		Bitmap bmp = BitmapFactory.decodeResource(res, id);
		if(bmp == null)
			throw new NullPointerException(String.format("Bitmap id: %d failed to load", id));
		int frameWidth = bmp.getWidth() / frameCount;
		Log.i("AnimatedActor.loadAnimatedActor", String.format("bmpWidth: %d, bmpHeight: %d, frameWidth: %d", 
				bmp.getWidth(), bmp.getHeight(), frameWidth
		));
		int width = scaleToWidth <= 0 ? frameWidth : scaleToWidth;
		Log.i("AnimatedActor.loadAnimatedActor", "width: " + width);
		int height = scaleToHeight <= 0 ? bmp.getHeight() : scaleToHeight;
		Log.i("AnimatedActor.loadAnimatedActor", "height: " + height);
		if(keepRatio) { // skaluje obraz jeśli został podany tylko jeden z wymiarów
			if (scaleToWidth <= 0) {
				float ratio = (float)frameWidth / bmp.getHeight();
				width = Math.round(height * ratio);
			}
			else if (scaleToHeight <= 0) {
				float ratio = (float)bmp.getHeight() / frameWidth;
				height = Math.round(width * ratio);
			}
		}

		return new AnimatedActor(bmp, frameCount, width, height);
	}
	
	
	private static Bitmap scalePermanently(Bitmap bmp, float scaleWidth, float scaleHeight) {
		Matrix m = new Matrix();
		m.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
	}
}

package com.cars;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

/**
 * Reprezentuje rysowalny obiekt (np. cegiełka, bohater)
 * @author bartek
 *
 */
public abstract class Actor {
	protected Bitmap bitmap;
	protected Paint paint;
	protected int bmpWidth, bmpHeight;
	private boolean _static;
	protected Actor previous, next;
	
	protected Actor() {}
	
	public Actor(Bitmap bmp, boolean staticc) {
		this(bmp, null, staticc);
	}
	
	public Actor(Bitmap bmp, Paint paint, boolean staticc) {
		this(bmp, paint, staticc, bmp.getWidth(), bmp.getHeight());
	}
	
	public Actor(Bitmap bmp, Paint paint, boolean staticc, int scaleWidth, int scaleHeight) {
		if(bmp == null)
			throw new NullPointerException();
		bitmap = bmp;
		this.paint = paint;
		_static = staticc;
		bmpWidth = scaleWidth;
		bmpHeight = scaleHeight;
	}
	
	/**
	 * Rysuje siebie na przekazanym płótnie
	 * @param c płótno
	 * @param x współrzędna x
	 * @param y współrzędna y
	 */
	public abstract void draw(Canvas c, int x, int y);
	public abstract void draw(Canvas c, Rect destination);
	/**
	 * Rysuje na raz kilka kopii tej samej bitmapy
	 * @param c płótno
	 * @param startX współrzędna x pierwszej kopii
	 * @param startY współrzędna y pierwszej kopii
	 * @param repeatCount liczba powtórzeń
	 * @param spaceBetween odstęp w pikselach pomiędzy kopiami
	 * @param left jeśli true kopie rysowane w lewo, domyślnie w prawo.
	 */
	public abstract void drawNTimes(Canvas c, int startX, int startY, int repeatCount, int spaceBetween, boolean left);
	
	public abstract Rect getRect();
	
	/**
	 * Informuje, czy jest animowana
	 * @return true jeśli animowana, false inaczej
	 */
	public final boolean isStatic() {
		return _static;
	}
	
	public void dispose() {
		if(bitmap == null) {
			Log.i("Actor.dispose", "bitmap null for " + getClass());
		}
		bitmap.recycle();
		bitmap = null;
	}
}

/**
 * Aktor statyczny (elementy tj. ściany)
 * @author bartek
 *
 */
class StaticActor extends Actor {

	public StaticActor(Bitmap bmp) {
		super(bmp, true);
	}
	
	public StaticActor(Bitmap bmp, int scaleWidth, int scaleHeight) {
		super(bmp, null, true, scaleWidth, scaleHeight);
	}
	
	@Override
	public void draw(Canvas c, int x, int y) {
		c.drawBitmap(bitmap, null, 
				new Rect(x, y, x + bmpWidth, y + bmpHeight), paint);
	}
	
	@Override
	public void draw(Canvas c, Rect destination) {
		c.drawBitmap(bitmap, null, destination, paint);
	}

	@Override
	public void drawNTimes(Canvas c, int startX, int startY, int repeatCount,
			int spaceBetween, boolean left) {
		int width = bitmap.getWidth();
		int heightBottom = startY + bmpHeight;
		Rect tmp = new Rect();
		for(int i = 0, x = startX; i < repeatCount; ++i, x += left ? (-width - spaceBetween) : (width + spaceBetween)) {
			tmp.left = x;
			tmp.top = startY;
			tmp.right = x + bmpWidth;
			tmp.bottom = heightBottom;
			c.drawBitmap(bitmap, null, tmp, paint);
		}
	}

	@Override
	public Rect getRect() {
		return new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	}
	
}

/**
 * Aktor z własną animacją.
 * @author bartek
 *
 */
class AnimatedActor extends Actor {
	protected final int frameCount;
	protected volatile int currentFrame = 0;
	private final Rect rect;
	
	public AnimatedActor(Bitmap bmp, int frameCount) {
//		super(bmp, false);
		this(bmp, frameCount, bmp.getWidth() / frameCount, bmp.getHeight());
	}
	
	public AnimatedActor(Bitmap bmp, int frameCount, int frameScaleWidth, int frameScaleHeight) {
		super(bmp, null, false, frameScaleWidth, frameScaleHeight);
		if(frameCount < 1)
			throw new IllegalArgumentException("frameCount");
		this.frameCount = frameCount;
		rect = new Rect();
	}

	@Override
	public void draw(Canvas c, int x, int y) {
		rect.left = bmpWidth * currentFrame;
		rect.top = 0;
		rect.right = bmpWidth * (currentFrame + 1);
		rect.bottom = bmpHeight;
		c.drawBitmap(bitmap, rect,
				new Rect(x, y, x + bmpWidth, y + bmpHeight), 
				paint);
		currentFrame = (currentFrame + 1) % frameCount;
	}

	@Override
	public void draw(Canvas c, Rect destination) {
		rect.left = bmpWidth * currentFrame;
		rect.top = 0;
		rect.right = bmpWidth * (currentFrame + 1);
		rect.bottom = bmpHeight;
		c.drawBitmap(bitmap, rect, destination, paint);
		currentFrame = (currentFrame + 1) % frameCount;
	}

	@Override
	public void drawNTimes(Canvas c, int startX, int startY, int repeatCount,
			int spaceBetween, boolean left) {
		int bWidth = bitmap.getWidth();
		int frameWidth = bWidth / frameCount;
		int bHeight = bitmap.getHeight();
		Rect frameRect = new Rect(bWidth * currentFrame, 0, bWidth * currentFrame + frameWidth, bHeight);
		int heightBottom = startY + bmpHeight;
		Rect tmp = new Rect();
		for(int i = 0, x = startX; i < repeatCount; ++i, x += left ? (-bmpWidth - spaceBetween) : (bmpWidth + spaceBetween)) {
			tmp.left = x;
			tmp.top = startY;
			tmp.right = x + bmpWidth;
			tmp.bottom = heightBottom;
			c.drawBitmap(bitmap, frameRect, tmp, paint);
		}
		
		currentFrame = (currentFrame + 1) % frameCount;
	}

	@Override
	public Rect getRect() {
		return new Rect(0, 0, bmpWidth, bmpHeight);
	}
	
}


class PartiallyTransparentActorDecorator extends Actor {
	private Paint previousPaint;
	private GameThread gameThread;
	private int cycle;
	private int increaseAlphaBy;
	private boolean ended = false;
	
	public PartiallyTransparentActorDecorator(Actor actor, GameThread gt, int cycles, int alpha) {
		if(actor == null || gt == null)
			throw new NullPointerException();
		if(cycles < 1)
			throw new IllegalArgumentException();
		this.previous = actor;
		this.gameThread = gt;
		this.cycle = cycles;
		Paint p = new Paint();
		p.setAlpha(alpha);
		this.previousPaint = actor.paint;
		actor.paint = p;
		increaseAlphaBy = 255 / cycles;
		Log.i("PartiallyTransparentActorDecorator.new", "new decorator created for " + actor);
	}

	@Override
	public void draw(Canvas c, int x, int y) {
		previous.draw(c, x, y);
		checkCycles();
	}

	@Override
	public void draw(Canvas c, Rect destination) {
		previous.draw(c, destination);
		checkCycles();
	}

	@Override
	public void drawNTimes(Canvas c, int startX, int startY, int repeatCount,
			int spaceBetween, boolean left) {
		previous.drawNTimes(c, startX, startY, repeatCount, spaceBetween, left);
		checkCycles();
	}

	@Override
	public Rect getRect() {
		return previous.getRect();
	}
	
	private void checkCycles() {
		if(ended)
			throw new IllegalStateException();
		if(--cycle <= 0) {
			previous.paint = previousPaint;
			gameThread.onEventGame(EventType.PROTECTION_OVER, this);
			ended = true;
		} else {
			previous.paint.setAlpha(previous.paint.getAlpha() + increaseAlphaBy);
		}
	}

	@Override
	public void dispose() {
		Log.i("PartiallyTransparentActorDecorator.dispose", "dispose called for actor: " + previous);
		previous.dispose();
	}
	
	
	
}

package com.cars;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public final class GameThread extends Thread {

	public final static long ONE_FRAP_TIME_IN_MILIS = 1000l / 15l; // fps
	public final static int PLAYER_FRAME_COUNT = 4;
	public final static int COLUMNS_COUNT = 20;
	public final static int LINES_COUNT = 40;
	public static final int PROTECTION_TIME_VALUE = 10;
	public static int VX_MAX = 25;

	private volatile int Pdx;
	// private int protectionTime=0;
	private volatile boolean collisionProtected;

	private boolean assetsLoaded;
	private SurfaceHolder surfaceHolder;
	private GameView gameView;
	private volatile boolean running;
	private ObstacleGenerator obFactory;
	volatile boolean is_started;
	
	private Background background;
	// aktorzy
	private volatile Actor wall;
	private volatile Actor player;
	private volatile Actor heart;

	// dane pomocnicze
	private int column_width;
	private int line_height;
	boolean[][] board;
	volatile int boardIndicator;

	// współrzędne
	volatile Rect playerRect;
	// wskazuje gdzie ma znaleźć się ŚRODEK aktora gracz
	private volatile Point playerDestination;

	// status gracza
	volatile int players_lives = 3;
	volatile int players_score = 0;

	// prędkość
	private volatile int scroll_speed;
	private volatile int player_speed;

	public GameThread(SurfaceHolder surfaceHolder, GameView gameView,
			Boolean start) {
		if (surfaceHolder == null || gameView == null)
			throw new NullPointerException();
		setRunning(start);
		this.surfaceHolder = surfaceHolder;
		this.gameView = gameView;
		board = new boolean[LINES_COUNT][COLUMNS_COUNT];
		playerDestination = new Point();
	}

	public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
		this(surfaceHolder, gameView, false);
	}

	public GameThread(SurfaceHolder surfaceHolder, GameView gameView,
			Bundle savedState) {
		this(surfaceHolder, gameView, false);

		if (savedState != null)
			loadGame(savedState);

	}

	public void setRunning(boolean run) {
		running = run;
		Log.d("GameThread.setRunning", run + " has been set");
	}

	public Boolean getRunning() {
		return this.running;
	}

	@Override
	public void start() {
		if (!is_started) {
			super.start();
			is_started = true;
		}
	}

	@Override
	public void run() {
		// zawiesza wątek dopóki nie są znane wymiary GameView
		while (!gameView.dimensionsKnown)
			Thread.yield();

		onStartupGame();
		if (!assetsLoaded) {
			Log.e("GT.run", "Asset's aren't loaded");
			throw new IllegalStateException("assets not loaded");
		}

		Canvas tmpCanvas;
		long stopwatch = System.nanoTime();
		long prev_stopwatch;

		while (running) {
			prev_stopwatch = stopwatch;
			stopwatch = System.nanoTime();

			try {
				onUpdateGame(stopwatch - prev_stopwatch); // dt
				if (surfaceHolder.getSurface().isValid()) {
					Log.i("GameThread.run", "surface valid");
					tmpCanvas = surfaceHolder.lockCanvas();
					onDrawGame(tmpCanvas);
					surfaceHolder.unlockCanvasAndPost(tmpCanvas);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("GTerror", e.getMessage());
			}

			try {
				Thread.sleep(ONE_FRAP_TIME_IN_MILIS
						- (System.nanoTime() - stopwatch) / 1000000l);
			} catch (InterruptedException e) {
				Log.w("GameThread.run() -> sleep", "Sleep interupped");
			} catch (IllegalArgumentException e) {
				Log.w("GameThread.run() -> sleep", "No sleep, game is too slow");
			}

			// while(paused)
			// Thread.yield();

		}

		is_started = false;
	}

	public boolean areAssetsLoaded() {
		return assetsLoaded;
	}

	/**
	 * Używana do zainicjowania danych.
	 * 
	 * @throws NullPointerException
	 *             jeżeli nie znaleziono jednego z zasobów.
	 */
	public void onStartupGame(Object... extraParams) {
		if (assetsLoaded)
			return;
		Resources res = gameView.getResources();

		background = new Background(BitmapFactory.decodeResource(res,
				R.drawable.background), gameView);

		player = AssetsHelper.loadAnimatedActor(res, R.drawable.player,
				PLAYER_FRAME_COUNT);

		column_width = gameView.getWidth() / COLUMNS_COUNT;
		line_height = gameView.getHeight() / LINES_COUNT;

		wall = AssetsHelper.loadStaticActor(res, R.drawable.wall, column_width,
				line_height, false);

		if (playerRect == null) {
			playerRect = new Rect(0, 0, column_width * 2, line_height * 3);
			playerRect.offsetTo((COLUMNS_COUNT / 2 - 1) * column_width,
					(LINES_COUNT - 3) * line_height);
		}

		// żeby nie zaczął się sam przesuwać
		playerDestination.x = playerRect.left + playerRect.width() / 2;
		playerDestination.y = playerRect.top + playerRect.height() / 2;

		// Log.i("GameThread.onStartupGame", String.format(
		// "left: %d, top: %d, right: %d, bottom: %d", playerRect.left,
		// playerRect.top, playerRect.right, playerRect.bottom));

		heart = AssetsHelper.loadStaticActor(res, R.drawable.heart,
				column_width, line_height, false);

		assetsLoaded = true;
	}

	public int getColumnsCount() {
		return COLUMNS_COUNT;
	}

	/**
	 * Zwalnianie zasobów.
	 */
	public void onShutdownGame(Object... extraParams) {

		// zwolnienie pamięci trzymanej przez bitmapy
		if (assetsLoaded) {
			wall.dispose();
			player.dispose();
			background.dispose();
		}

		playerRect = null;

		assetsLoaded = false;
	}

	/**
	 * Aktualizacja stanu gry (określenie nowych położeń etc.)
	 */
	public void onUpdateGame(long timeDiff, Object... extraParams) {
		// if(protectionTime>0)
		// protectionTime--;
		movePlayer();
//		playerRect.offset(Pdx, 0);
		background.update();
		shiftBoard();
		checkCollisions();
		checkPlayerStatus();
	}

	private void movePlayer(){
		if(Pdx < 0){
			if(playerRect.left/column_width > 1){
				playerRect.offset(Pdx, 0);
			}
			else {
				Pdx = 0;
			}
		}else if(Pdx > 0){
//			if(playerRect.right/column_width < column_width-5){
			if(playerRect.right/column_width < COLUMNS_COUNT-1){
				playerRect.offset(Pdx, 0);
			}
			else {
				Pdx = 0;
			}
		}
	}
	/**
	 * Rysowanie sceny.
	 */
	public void onDrawGame(Canvas canvas) {
		background.draw(canvas);
		player.draw(canvas, playerRect);
		for (int counter = LINES_COUNT, i = boardIndicator; counter >= 0; --counter, i = (i + 1)
				% LINES_COUNT) {
			boolean[] line = board[i];
			for (int column = 0; column < COLUMNS_COUNT; ++column) {
				if (line[column])
					wall.draw(canvas, column * column_width, counter
							* line_height);
			}
		}
		heart.drawNTimes(canvas, (COLUMNS_COUNT - 1) * column_width, 0,
				players_lives, 0, true);
	}

	/**
	 * Obsługa wejścia. Uruchamiane poza główną pętlą.
	 */
	public void onInputGame(MotionEvent mEvent, Object... extraParams) {

		// if(paused) { // jeśli pauza odblokuj grę i wyjdź z metody
		// paused = false;
		// return;
		// }

		switch (mEvent.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mEvent.getX() < gameView.getWidth() / 2) {
				Pdx = -VX_MAX;
			} else {
				Pdx = VX_MAX;
			}
			break;
		case MotionEvent.ACTION_UP:
			Pdx = 0;
			break;
		}
	}

	/**
	 * Obsługa zdarzeń.
	 */
	public void onEventGame(EventType eventType, Object... extraParams) {
		Log.i("GameThread.onEventGame", "event occured: " + eventType);
		Actor tmp;
		switch (eventType) {
		case COLLISION_WITH_BRICK:
			if (!collisionProtected) {
				--players_lives;
				if (players_lives <= 0)
					break;
				player = new PartiallyTransparentActorDecorator(player, this,
						PROTECTION_TIME_VALUE, 50);
				collisionProtected = true;
			}
			break;
		case LOST:
			setRunning(false);
			gameView.gameActivity.endGameSaveScore(players_score);
			break;
		case PROTECTION_OVER:
			tmp = (Actor) extraParams[0];
			if(tmp.next != null) {
				tmp.previous.next = tmp.next;
				tmp.next.previous = tmp.previous;
			} else {
				player = tmp.previous;
			}
			collisionProtected = false;
			break;
		case EFFECTS_GONE:
			tmp = (Actor)extraParams[0];
			if(tmp.next != null) {
				tmp.previous.next = tmp.next;
				tmp.next.previous = tmp.previous;
			} else {
				player = tmp.previous;
			}
			break;
		}

	}

	public void setObFactory(int columnsCount) {
		this.obFactory = new ObstacleGenerator(columnsCount);

		// TODO: to jest ilość 'klatek' potrzebnych graczowi na spokojne
		// przeniesienie się w bok.
		// Na razie na sztywno, potem będzie się pobierało z prędkości X-MAX
		// gracza
		this.obFactory.setPdx(3);
	}

	private void loadGame(Bundle b) {
		Bundle savedInstanceState = b;
		if (savedInstanceState == null)
			return;
		Log.i("GameActivity.saveGame", "savedInstanceState not null");

		board = new boolean[LINES_COUNT][COLUMNS_COUNT];
		byte[] tmpBoard = savedInstanceState.getByteArray(STATE_BOARD);
		if (tmpBoard != null) {
			for (int line = 0; line < LINES_COUNT; ++line) {
				boolean[] lineArr = new boolean[COLUMNS_COUNT];
				int shift = line * COLUMNS_COUNT;
				for (int column = 0; column < COLUMNS_COUNT; ++column) {
					lineArr[column] = (tmpBoard[shift + column] == 1) ? true
							: false;
				}
				board[line] = lineArr;
			}
		}
		boardIndicator = savedInstanceState.getInt(STATE_BOARD_INDICATOR);

		playerRect = new Rect();
		playerRect.left = savedInstanceState.getInt(SPR_LEFT);
		playerRect.top = savedInstanceState.getInt(SPR_TOP);
		playerRect.right = savedInstanceState.getInt(SPR_RIGHT);
		playerRect.bottom = savedInstanceState.getInt(SPR_BOTTOM);

		players_lives = savedInstanceState.getInt(SP_LIVES);
		players_score = savedInstanceState.getInt(SP_SCORE);
	}

	public Bundle saveGame(Bundle b) {
		Bundle outState = (b == null) ? new Bundle() : b;
		// zapisanie stanu planszy
		byte[] board = new byte[LINES_COUNT * COLUMNS_COUNT];
		boolean tmp;
		for (int line = 0; line < LINES_COUNT; ++line) {
			for (int column = 0; column < COLUMNS_COUNT; ++column) {
				tmp = gameView.gameThread.board[line][column];
				board[line * COLUMNS_COUNT + column] = tmp ? (byte) 1
						: (byte) 0;
			}
		}
		outState.putByteArray(STATE_BOARD, board);
		outState.putInt(STATE_BOARD_INDICATOR, boardIndicator);

		outState.putInt(SPR_LEFT, playerRect.left);
		outState.putInt(SPR_TOP, playerRect.top);
		outState.putInt(SPR_RIGHT, playerRect.right);
		outState.putInt(SPR_BOTTOM, playerRect.bottom);

		outState.putInt(SP_LIVES, players_lives);
		outState.putInt(SP_SCORE, players_score);

		return outState;
	}

	private void shiftBoard() {
		board[boardIndicator] = obFactory.getNextObstacleRow();
		boardIndicator = (boardIndicator + 1) % LINES_COUNT;
		players_score += 1;
	}

	private void checkCollisions() {
		// wystarczy sprawdzić bliskie otoczenie
		float insideColumn = (float) playerRect.left / column_width;
		int leftColumn = Math.min((int) Math.floor(insideColumn + 0.05f), COLUMNS_COUNT - 3);
		float insideLine = (float) playerRect.top / line_height;
		int topLine = (int) Math.floor(insideLine + 0.05f);

		// sprawdzenie czy w punkcie o wsp (leftColumn, topLine) jest ściana
		int rightColumn = (int) Math.ceil(insideColumn
				+ (float) playerRect.width() / column_width - 0.05f);
		rightColumn = Math.min(COLUMNS_COUNT-1, rightColumn);
		boolean crushed = false;

		// Zderzenie czołowo, następnie lewym i prawym bokiem
		Log.i("GameThread.checkCollisions", "leftColumn: " + leftColumn
				+ ", rightColumn: " + rightColumn);
		int columnsCount = Math.max(rightColumn - leftColumn, 0);
		int player_line_height = (int) Math.ceil((float) playerRect.height()
				/ line_height - 0.05f);
		for (int i = 0; i < player_line_height; ++i) {
			int lineNumber = (boardIndicator + (LINES_COUNT - topLine - i))
					% LINES_COUNT;
			boolean[] line = board[lineNumber];
			for (int j = 0; j < columnsCount; ++j) {
				if (crushed)
					break;
				crushed = crushed || line[leftColumn + j];
			}
			if (crushed)
				break;
		}
		if (crushed) {
			onEventGame(EventType.COLLISION_WITH_BRICK);
			return;
		}

	}

	private void checkPlayerStatus() {
		if (players_lives <= 0)
			onEventGame(EventType.LOST);
	}
	
	
	private void turnOnBuffOrDebuff(BuffOrDebuff bod, int cycles) {
		if(bod == null)
			throw new NullPointerException();
		GameEffect ge = bod.effectFactory.makeNew(cycles);
		player.next = ge;
		ge.previous = player;
		ge.turnOn(this, player);
		player = ge;
	}

	static final String STATE_BOARD = "board_state";
	static final String STATE_BOARD_INDICATOR = "board_ind_state";
	static final String SPR_LEFT = "spr_left"; // playerRect.left
	static final String SPR_TOP = "spr_top";
	static final String SPR_RIGHT = "spr_right";
	static final String SPR_BOTTOM = "spr_bottom";
	static final String SP_LIVES = "sp_lives";
	static final String SP_SCORE = "sp_score";
}

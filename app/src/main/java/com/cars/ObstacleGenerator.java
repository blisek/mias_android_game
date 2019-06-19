package com.cars;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import android.util.Log;

public class ObstacleGenerator {

	private int columnCount;
	private float level;
	private int ignoreAccessFromFieldQuantity;
	private boolean[] lastRow;
	private LinkedList<boolean[]> nextList;
	private int Pdx;

	Random rnd;

	/**
	 * Generator przeszkód zwykłych
	 * 
	 * @param columns
	 */
	public ObstacleGenerator(int columns) {
		this.columnCount = columns;
		this.level = 0.5f;
		this.rnd = new Random();
		this.ignoreAccessFromFieldQuantity = 0;
		this.nextList = new LinkedList<boolean[]>();
		Pdx=1;

		lastRow = new boolean[columns];
		for (int i = 0; i < columns; i++) {
			lastRow[i] = false;
		}
	}

	/**
	 * Współczynnik zmieniający ilość przeszkód w wierszu Poziomy od 1 do 10
	 */
	public void incLevel() {
		if (level < 1.5) {
			level += 0.1f;
		}
	}

	public void decLevel() {
		if (level > 0.5) {
			level -= 0.1f;
		}
	}

	private int getObstacleForRowCount() {
		return Math.max((int) (columnCount * (level / 2)), 1);
	}

	private void fillArray(boolean[] arr, boolean b) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = b;
		}
	}

	private boolean[] copyArray(boolean[] arr) {
		boolean[] res = new boolean[arr.length];
		for (int i = 0; i < res.length; i++) {
			res[i] = arr[i];
		}
		return res;
	}

	private void fillRow(boolean[] row, int obstacles) {
		int i;
		
		// Long s1 = System.nanoTime();
		// for(i=0; i<obstacles; i++){
		// row[i]=true;
		// }
		// for(; i<row.length; i++){
		// row[i]=false;
		// }
		//
		// // Fisher–Yates shuffle
		// for (i = row.length - 1; i > 0; i--) {
		// int index = rnd.nextInt(i + 1);
		//
		// boolean b = row[index];
		// row[index] = row[i];
		// row[i] = b;
		// }
		// Long s2 = System.nanoTime();
		ArrayList<Boolean> vec = new ArrayList<Boolean>();

		for (i = 0; i < obstacles; i++) {
			vec.add(new Boolean(false));
		}
		for (; i < row.length; i++) {
			vec.add(new Boolean(true));
		}

		for (i = 0; i < row.length && vec.size() > 0; i++) {
			if (vec.remove(rnd.nextInt(vec.size()))) {
				row[i] = true;
			} else {
				row[i] = false;
				if (i + 2 < row.length) {
					row[i + 1] = false;
					row[i + 2] = false;
					i+=2;
				}else{
					try{ row[i-1]=false; } catch(NullPointerException e){}
					try{ row[i-2]=false; } catch(NullPointerException e){}
				}
			}
		}
		// Long s3 = System.nanoTime();
		// Log.i("Comparison",
		// "1st="+(s2-s1)/1000000.0f+", 2nd="+(s3-s2)/1000000.0f);
	}

	/**
	 * Funkcja zwracająca tablicę bool'owską. True oznacza przeszkodę w danym
	 * miejscu.
	 * 
	 * @return
	 */
	public boolean[] getNextObstacleRow() {

		if (nextList.size() > 0) {
			return nextList.removeFirst();
		}

		boolean[] row;
		boolean[] nextRow = new boolean[columnCount];
		fillRow(nextRow, getObstacleForRowCount());

		int distance = checkIfNotBlocked(nextRow);
		if (distance > 0) {
			nextList.addLast(nextRow);
			row = new boolean[columnCount];

			for (int i = 0; i < distance; i++) {
				row = new boolean[columnCount];
				fillArray(row, false);
				nextList.addFirst(row);
			}

		} else {
			row = new boolean[columnCount];
			fillArray(row, false);
			nextList.addFirst(row);
			nextList.addLast(nextRow);
		}

		this.lastRow = copyArray(nextRow);
		return nextList.removeFirst();
	}

	public int test(boolean[] last, boolean[] next) {
		this.lastRow = last;
		boolean[] _nextRow = next;
		return checkIfNotBlocked(_nextRow);

	}

	private int checkIfNotBlocked(boolean[] nextRow) {
		int distance = columnCount;
		try {
			int obstacleCount = getObstacleForRowCount() - this.ignoreAccessFromFieldQuantity;

			boolean[] _nextRow = copyArray(nextRow);

			for (distance = 0; obstacleCount > 0; distance++) {
				for (int i = 0; i < _nextRow.length; i++) {
					if (!lastRow[i]) {
						for (int j = Math.max(i - 2 - distance, 0); j < Math.min(_nextRow.length, i + 2 + distance) && !lastRow[i]; j++) {
							if (!_nextRow[j]) {
								lastRow[i] = true;
								obstacleCount--;
							}
						}
					}
				}
			}
			distance++;
			distance*=Pdx;
		} catch (Exception e) {
			Log.e("OG.CheckIfNotBlocked", "Error -> " + e.getMessage());
		}
		return distance;
	}

	public int getEmptyAllowedQuantitiy() {
		return ignoreAccessFromFieldQuantity;
	}

	public void setEmptyAllowedQuantitiy(int emptyAllowedQuantitiy) {
		if (0 <= emptyAllowedQuantitiy && emptyAllowedQuantitiy <= this.columnCount)
			this.ignoreAccessFromFieldQuantity = emptyAllowedQuantitiy;
	}

	public float getLevel() {
		return level;
	}

	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	public int getPdx() {
		return Pdx;
	}

	public void setPdx(int pdx) {
		Pdx = pdx;
	}

}

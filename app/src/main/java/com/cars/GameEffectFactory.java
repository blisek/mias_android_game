package com.cars;

/**
 * Do tworzenia nowych obiektów GameEffect.
 * @author bartek
 *
 */
public interface GameEffectFactory {
	public GameEffect makeNew(final int cycles);
}

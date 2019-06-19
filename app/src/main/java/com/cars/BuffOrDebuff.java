package com.cars;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;

/**
 * Klasa opisująca buffy i debuffy. Udostępnia metody statyczne
 * do dodawania zarówno buffów jak i debuffów (implementacyjnie nie
 * ma różnicy). Ze statycznych list buffs i debuffs można pobrać 
 * załadowe do tej pory obiekty.
 * @author bartek
 *
 */
public final class BuffOrDebuff {
	// na buffy
	public final static List<BuffOrDebuff> buffs = new ArrayList<BuffOrDebuff>();
	// na debuffy
	public final static List<BuffOrDebuff> debuffs = new ArrayList<BuffOrDebuff>();
	
	/**
	 * Dodaje buffa/debuffa jako nieanimowanego aktora.
	 * @param res menadżer zasobów.
	 * @param res_id id zasobu.
	 * @param points punkty za zebranie (może być wartość ujemna).
	 * @param effect efekt, który zostanie zastosowany dla gracza.
	 * @param isBuff jeśli true stworzony obiekt jest traktowany jako buff
	 * w przeciwnym wypadku jako debuff.
	 */
	public static void addBuffOrDebuff(Resources res, int res_id, int points, GameEffectFactory effectFactory, boolean isBuff) {
		Actor actor = AssetsHelper.loadStaticActor(res, res_id);
		BuffOrDebuff b = new BuffOrDebuff(actor, points, effectFactory);
		if(isBuff)
			buffs.add(b);
		else
			debuffs.add(b);
	}
	
	/**
	 * Dodaje buffa/debuffa z animacją.
	 * @param res menadżer zasobów.
	 * @param res_id id zasobu.
	 * @param frameCount liczba klatek animacji.
	 * @param points liczba punktów.
	 * @param effect efekt, który zostanie zastosowany na playerze.
	 * @param isBuff jeśli true trafia na listę buffów, false - na listę
	 * debuffów.
	 */
	public static void addBuffOrDebuff(Resources res, int res_id, int frameCount, int points, GameEffectFactory effectFactory, boolean isBuff) {
		Actor actor = AssetsHelper.loadAnimatedActor(res, res_id, frameCount);
		BuffOrDebuff b = new BuffOrDebuff(actor, points, effectFactory);
		if(isBuff)
			buffs.add(b);
		else
			debuffs.add(b);
	}
	
	public BuffOrDebuff(Actor actor, int points, GameEffectFactory effectFactory) {
		if(actor == null || effectFactory == null)
			throw new NullPointerException();
		
		this.actor = actor;
		this.points = points;
		this.effectFactory = effectFactory;
	}
	
	public final Actor actor;
	public final int points;
	public final GameEffectFactory effectFactory;
}

package com.braids.coffeebombermen.client.gamecore.model.level;

import java.util.ArrayList;

import com.braids.coffeebombermen.client.gamecore.model.FireModel;
import com.braids.coffeebombermen.client.gamecore.model.IterableObject;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.OptConsts.Walls;

/**
 * A component of the level (the level consists of level components).
 */
public class LevelComponent extends IterableObject {

	/** What kind of wall is associated to the component. */
	private Walls                wall         = Walls.EMPTY;
	/**
	 * What kind of item is optionally contained by the component. Accessible
	 * only if wall is Walls.EMPTY (null means no item is on the component).
	 */
	private Items                item;
	/** The vector of fire models taking place on this component. */
	private ArrayList<FireModel> lstFireModel = new ArrayList<FireModel>();

	/**
	 * Sets the wall of the component.
	 * 
	 * @param wall
	 *            the wall to be set
	 */
	public void setWall(final Walls wall) {
		this.wall = wall;
	}

	/**
	 * Sets the item of the component.
	 * 
	 * @param item
	 *            the item to be set
	 */
	public void setItem(final Items item) {
		this.item = item;
	}

	/**
	 * Returns the wall of the component.
	 * 
	 * @return the wall of the component
	 */
	public Walls getWall() {
		return wall;
	}

	/**
	 * Returns the item of the component.
	 * 
	 * @return the item of the component
	 */
	public Items getItem() {
		return item;
	}

	public boolean hasFire() {
		return !lstFireModel.isEmpty();
	}

	public int getFireCount() {
		return lstFireModel.size();
	}

	public void addFire(FireModel fire) {
		lstFireModel.add(fire);
	}

	public void removeFire(FireModel fire) {
		lstFireModel.remove(fire);
	}

	public void removeAllFire() {
		lstFireModel.clear();
	}

	public FireModel getLastFire() {
		return lstFireModel.get(lstFireModel.size() - 1);
	}

	public FireModel getFire(int index) {
		return lstFireModel.get(index);
	}

}

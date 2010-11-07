package com.braids.coffeebombermen.utils;

import java.awt.Point;

public class Position {

	private int x;
	private int y;

	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			Position pt = (Position) obj;
			return (x == pt.x) && (y == pt.y);
		}
		return super.equals(obj);
	}
}

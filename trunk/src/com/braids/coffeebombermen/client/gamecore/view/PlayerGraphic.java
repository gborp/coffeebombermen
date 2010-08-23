package com.braids.coffeebombermen.client.gamecore.view;

import java.awt.Image;

import com.braids.coffeebombermen.client.gamecore.model.PlayerModel;
import com.braids.coffeebombermen.client.graphics.ImageHandler;

public class PlayerGraphic {

	private final ImageHandler[][][] bombermanPhaseHandlers;

	public PlayerGraphic(ImageHandler[][][] bombermanPhaseHandlers) {
		this.bombermanPhaseHandlers = bombermanPhaseHandlers;
	}

	public Image getImage(PlayerModel playerModel, float scaleFactor) {
		final int phasesCount = bombermanPhaseHandlers[playerModel.getActivity().ordinal()][playerModel.getDirection().ordinal()].length;
		final Image bombermanImage = bombermanPhaseHandlers[playerModel.getActivity().ordinal()][playerModel.getDirection().ordinal()][phasesCount
		        * playerModel.getIterationCounter() / playerModel.getActivity().activityIterations].getScaledImage(scaleFactor);
		return bombermanImage;
	}

	public int getOriginalWidth() {
		return bombermanPhaseHandlers[0][0][0].getOriginalWidth();
	}
}

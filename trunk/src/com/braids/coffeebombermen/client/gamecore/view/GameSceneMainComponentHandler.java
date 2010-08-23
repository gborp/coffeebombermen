package com.braids.coffeebombermen.client.gamecore.view;

import javax.swing.SwingUtilities;

import com.braids.coffeebombermen.MainComponentHandler;
import com.braids.coffeebombermen.MainFrame;
import com.braids.coffeebombermen.client.Client;
import com.braids.coffeebombermen.options.OptionsManager;
import com.braids.coffeebombermen.options.model.ClientOptions;

/**
 * A main component handler where the main comonent is the game scene component.
 */
public class GameSceneMainComponentHandler implements MainComponentHandler {

	/** Reference to the main frame. */
	private final MainFrame          mainFrame;
	/** Component for the game scene. */
	private final GameSceneComponent gameSceneComponent;
	private final Client             client;

	/**
	 * Creates a new GameSceneMainComponentHandler.
	 * 
	 * @param client
	 * @param mainFrame
	 *            reference to the main frame
	 * @param clientOptionsManager
	 *            reference to the client options manager
	 */
	public GameSceneMainComponentHandler(Client client, final MainFrame mainFrame, final OptionsManager<ClientOptions> clientOptionsManager) {
		this.client = client;
		this.mainFrame = mainFrame;
		gameSceneComponent = new GameSceneComponent(client, clientOptionsManager);
	}

	/**
	 * Returns the game scene component.
	 * 
	 * @return the game scene component
	 */
	public GameSceneComponent getGameSceneComponent() {
		return gameSceneComponent;
	}

	/**
	 * Called when reinitiation of main component is needed.
	 */
	public void reinitMainComponent() {
		mainFrame.setMainComponent(gameSceneComponent);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				gameSceneComponent.requestFocus();
			}
		});
	}

	/**
	 * Called when a new graphical theme has been loaded. We do not store or
	 * work with graphical datas, simply delegate the call to the game scene
	 * component.
	 */
	public void graphicalThemeChanged() {
		gameSceneComponent.graphicalThemeChanged();
	}

	/**
	 * Called when handler of main component is being replaced. There is nothing
	 * to do.
	 */
	public void releaseMainComponent() {}

}

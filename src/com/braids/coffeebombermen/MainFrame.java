package com.braids.coffeebombermen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.braids.coffeebombermen.utils.GeneralStringTokenizer;

/**
 * This is the main window, the main frame of the game. It contains references
 * to the components of the main frame.
 */
public class MainFrame extends JFrame implements ActionListener, KeyListener, MouseListener {

	/**
	 * Split pane to slit the messages text area and the rest of the window
	 * horizontally.
	 */
	private final JSplitPane  splitPane1          = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	/** Text area for holding the players and system messages. */
	private final JTextArea   messagesTextArea    = new JTextArea();
	/** Text field to enter new messages. */
	private final JTextField  newMessageTextField = new JTextField();
	/** The main menu bar of the game. */
	private final MainMenuBar mainMenuBar;
	/** Stores the bounds of the main frame in window mode. */
	private final Rectangle   windowModeBounds    = new Rectangle();
	/** Reference to the main component. */
	private JComponent        mainComponent;

	/**
	 * Message handler to handle new messages entered in the new message text
	 * field.
	 */
	private MessageHandler    messageHandler;

	/**
	 * Creates a new MainFrame.
	 * 
	 * @param mainMenuHandler
	 *            reference to the main menu handler
	 */
	MainFrame(final MainMenuHandler mainMenuHandler) {
		super(Consts.APPLICATION_NAME);
		mainMenuBar = new MainMenuBar(mainMenuHandler);
		buildGUI();
		addWindowListener(new WindowAdapter() {

			public void windowClosing(final WindowEvent we) {
				mainMenuHandler.exit();
			}
		});
	}

	/**
	 * Builds the graphical user interface of the main frame.
	 */
	private void buildGUI() {
		setJMenuBar(mainMenuBar);

		setMainComponent(new JLabel("No graphical theme has been loaded...", JLabel.CENTER));

		messagesTextArea.setBackground(Color.GRAY);
		messagesTextArea.setForeground(Color.WHITE);
		messagesTextArea.setRows(5);
		messagesTextArea.setEditable(false);
		messagesTextArea.setLineWrap(true);
		newMessageTextField.addActionListener(this);
		final JPanel messagePanel = new JPanel(new BorderLayout());
		messagePanel.add(new JScrollPane(messagesTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
		        BorderLayout.CENTER);
		newMessageTextField.addKeyListener(this);
		messagePanel.add(newMessageTextField, BorderLayout.SOUTH);

		splitPane1.setDividerSize(8);
		splitPane1.setBottomComponent(messagePanel);
		splitPane1.setResizeWeight(1);

		getContentPane().add(splitPane1, BorderLayout.CENTER);
	}

	/**
	 * Sets the main component of the main frame.
	 * 
	 * @param mainComponent
	 *            the main component to be set
	 */
	public void setMainComponent(final JComponent mainComponent) {
		if (this.mainComponent != null) {
			this.mainComponent.removeKeyListener(this);
			this.mainComponent.removeMouseListener(this);
		}
		this.mainComponent = mainComponent;
		splitPane1.setTopComponent(mainComponent);
		this.mainComponent.addKeyListener(this);
		this.mainComponent.addMouseListener(this);
		this.mainComponent.requestFocusInWindow();
	}

	/**
	 * Returns the main menu bar.
	 * 
	 * @return the main menu bar
	 */
	public MainMenuBar getMainMenuBar() {
		return mainMenuBar;
	}

	/**
	 * Sets the full screen window status.
	 * 
	 * @param fullScreen
	 *            true indicates to be in fullscreen mode; false to be in window
	 *            mode
	 */
	public void setFullScreenMode(final boolean fullScreen) {
		if (isVisible() && (getExtendedState() == NORMAL)) {
			getBounds(windowModeBounds);
		}

		final boolean wasVisible = isVisible();
		if (isDisplayable()) {
			dispose();
		}

		setUndecorated(fullScreen == true);
		setExtendedState(fullScreen ? MAXIMIZED_BOTH : NORMAL);
		if (!fullScreen) {
			setBounds(windowModeBounds);
		}

		if (wasVisible) {
			setVisible(true);
		}
	}

	/**
	 * Handles the action event of new message text field.
	 * 
	 * @param ae
	 *            details of the action event
	 */
	public void actionPerformed(final ActionEvent ae) {
		if (newMessageTextField.getText().length() > 0) {
			if (messageHandler != null) {
				messageHandler.handleMessage(newMessageTextField.getText());
			}
			newMessageTextField.setText("");
		}
	}

	/**
	 * Called when a key has been pressed (at a component where this listener
	 * has been added). Not used.
	 * 
	 * @param ke
	 *            details of the key event
	 */
	public void keyPressed(final KeyEvent ke) {}

	/**
	 * Called when a key has been released (at a component where this listener
	 * has been added). Not used.
	 * 
	 * @param ke
	 *            details of the key event
	 */
	public void keyReleased(final KeyEvent ke) {}

	/**
	 * Called when a key has been typed (at a component where this listener has
	 * been added). Handles transfering input focus between the main component
	 * and the new message text field when typing ENTER and ESCAPE.
	 * 
	 * @param ke
	 *            details of the key event
	 */
	public void keyTyped(final KeyEvent ke) {
		final Object source = ke.getSource();
		if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
			if (source == mainComponent) {
				newMessageTextField.requestFocusInWindow();
			}
		}
		if (ke.getKeyChar() == KeyEvent.VK_ESCAPE) {
			if (source == newMessageTextField) {
				if (mainComponent != null) {
					mainComponent.requestFocusInWindow();
				}
			}
		}
	}

	/**
	 * Called when mouse is clicked on the component. Transfers the focus to the
	 * main component.
	 * 
	 * @param mouseEvent
	 *            details of the mouse event
	 */
	public void mouseClicked(final MouseEvent mouseEvent) {
		if (mainComponent != null) {
			mainComponent.requestFocusInWindow();
		}
	}

	/**
	 * Called when mouse is pressed on the component.
	 * 
	 * @param mouseEvent
	 *            details of the mouse event
	 */
	public void mousePressed(final MouseEvent mouseEvent) {}

	/**
	 * Called when mouse is released on the component.
	 * 
	 * @param mouseEvent
	 *            details of the mouse event
	 */
	public void mouseReleased(final MouseEvent mouseEvent) {}

	/**
	 * Called when mouse is entered to the component.
	 * 
	 * @param mouseEvent
	 *            details of the mouse event
	 */
	public void mouseEntered(final MouseEvent mouseEvent) {}

	/**
	 * Called when mouse is left from the component.
	 * 
	 * @param mouseEvent
	 *            details of the mouse event
	 */
	public void mouseExited(final MouseEvent mouseEvent) {}

	/**
	 * Sets the message handler.
	 * 
	 * @param messageHandler
	 *            messageHandler to be set
	 */
	public void setMessageHandler(final MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	/**
	 * Receives a message, appends it to the messages text area.
	 * 
	 * @param message
	 *            message to be appended
	 */
	public void receiveMessage(final String message) {
		messagesTextArea.append("\n");
		messagesTextArea.append(message);
		messagesTextArea.setCaretPosition(messagesTextArea.getDocument().getLength());
	}

	/**
	 * Clears the messages text area.
	 */
	public void clearMessages() {
		messagesTextArea.setText("");
	}

	/**
	 * Packs the bounds of main frame and the positions of the splitters to a
	 * String object.
	 * 
	 * @return a String object holding the bounds of main frame and the
	 *         positions of the splitters
	 */
	public String packWindowAndSplitterPositions() {
		if (isVisible() && (getExtendedState() == NORMAL)) {
			getBounds(windowModeBounds);
		}

		StringBuilder buffer = new StringBuilder();

		buffer.append(windowModeBounds.x);
		buffer.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		buffer.append(windowModeBounds.y);
		buffer.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		buffer.append(windowModeBounds.width);
		buffer.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		buffer.append(windowModeBounds.height);
		buffer.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);

		buffer.append(splitPane1.getDividerLocation());
		buffer.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		buffer.append("N/A");
		buffer.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);

		return buffer.toString();
	}

	/**
	 * Sets the positions of the main frame and the splitters.
	 * 
	 * @param positions
	 *            the packed positions of the main frame and the splitters
	 */
	public void setWindowAndSplitterPositions(final String positions) {
		final GeneralStringTokenizer optionsTokenizer = new GeneralStringTokenizer(positions);
		final int x = optionsTokenizer.nextIntToken(), y = optionsTokenizer.nextIntToken(), width = optionsTokenizer.nextIntToken(), height = optionsTokenizer
		        .nextIntToken();

		windowModeBounds.setBounds(x, y, width, height);

		splitPane1.setDividerLocation(optionsTokenizer.nextIntToken());

		// unused
		optionsTokenizer.nextStringToken();
	}

	/**
	 * Restores the default positions of the main frame and the splitters.
	 */
	public void restoreDefaultWindowAndSplitterPositions() {
		setWindowAndSplitterPositions("10" + GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR + "10" + GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR + "650"
		        + GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR + "490" + GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR + "340"
		        + GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR + "400" + GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
	}

}

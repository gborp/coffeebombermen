package com.braids.coffeebombermen.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.braids.coffeebombermen.Consts;
import com.braids.coffeebombermen.client.Client;
import com.braids.coffeebombermen.options.OptionsChangeListener;
import com.braids.coffeebombermen.options.OptionsManager;
import com.braids.coffeebombermen.options.model.PublicClientOptions;
import com.braids.coffeebombermen.options.model.ServerOptions;
import com.braids.coffeebombermen.utils.ConnectionStub;
import com.braids.coffeebombermen.utils.GeneralStringTokenizer;

/**
 * The Player collector of the server. Handles player collecting.<br>
 * <br>
 * The joining protocol is the following:<br>
 * <ol>
 * <li>The server sends the server identification string
 * (PlayerCollector.SERVER_IDENTIFICATION_STRING).
 * <li>The server waits for the client identification string
 * (Client.CLIENT_IDENTIFICATION_STRING). If it is missing or it is not the
 * expected value, the server closes the connection.
 * <li>The server sends the servers application version.
 * <li>The server waits for the clients application version. If it is missing or
 * it is not the expected value (our version), closes the connection.
 * <li>The server waits for the game password. If there is a game password, and
 * we receive an unmatching game password, the server sends the
 * PlayerCollector.PASSWORD_REJECTED message and closes the connection.
 * Otherwise the server sends the PlayerCollector.PASSWORD_ACCEPTED message.
 * <li>The server waits for the public client options of the new client
 * (Server.Commands.SENDING_PUBLIC_CLIENT_OPTIONS command).
 * <li>The server sends the number of the clients connected including the new
 * client.
 * <li>The server sends the public client options of all clients including the
 * new client on the last place. Each line is a clients public client options.
 * <li>The client is now officially an accepted Bombermen client of the game.
 * Can send and receive commands, messages and can participate in games.
 * </ol>
 */
public class PlayerCollector implements OptionsChangeListener<ServerOptions> {

	/** Identification string of the Bombermen server. */
	public static final String                  SERVER_IDENTIFICATION_STRING = Consts.APPLICATION_NAME + " server";
	/** Message to the client that the given password was accepted. */
	public static final String                  PASSWORD_ACCEPTED            = "Password accepted";
	/** Message to the client that the given password was rejected. */
	public static final String                  PASSWORD_REJECTED            = "Password rejected";

	/** Reference to the server. */
	private final Server                        server;
	/** Reference to the server options manager. */
	private final OptionsManager<ServerOptions> serverOptionsManager;
	/** Reference to the vector of client contacts. */
	private final List<ClientContact>           clientContacts;
	/** Reference to the main frame for displaying message dialogs. */
	private final JFrame                        mainFrame;

	/** Server socket which through the players can connect. */
	private volatile ServerSocket               serverSocket;
	/** Tells whether this player collector is closed. */
	private volatile boolean                    closed                       = false;
	/**
	 * When a new client has been accepted, its contact object will be stored
	 * here.
	 */
	private volatile ClientContact              newClientContact;

	/**
	 * Creates a new PalyerCollector. Creates the server socket which through
	 * the players will connect.
	 * 
	 * @param server
	 *            reference to the server
	 * @param serverOptionsManager
	 *            reference to the server options manager
	 * @param clientContacts
	 *            vector of client contacts have already and to use to store new
	 *            client contacts
	 * @param mainFrame
	 *            reference to the main frame
	 */
	public PlayerCollector(final Server server, final OptionsManager<ServerOptions> serverOptionsManager, final List<ClientContact> clientContacts,
	        final JFrame mainFrame) {
		this.server = server;
		this.serverOptionsManager = serverOptionsManager;
		this.clientContacts = clientContacts;
		this.mainFrame = mainFrame;
		createServerSocket(serverOptionsManager.getOptions().getGamePort());
		startAcceptingClients();
		this.serverOptionsManager.registerOptionsChangeListener(this);
	}

	/**
	 * Creates the server socket which through the players will connect.
	 * 
	 * @param port
	 *            on which port to create server socket
	 */
	private void createServerSocket(final int port) {
		try {
			serverSocket = new ServerSocket(port);
		} catch (final IOException ie) {
			// Server thread cannot be blocked (messages can arrive while this
			// message is displayed), we show error message in a new thread
			new Thread() {

				public void run() {
					JOptionPane.showMessageDialog(mainFrame, new String[] { "Player collector:", "Can't create server socket on port " + port + "!" }, "Error",
					        JOptionPane.ERROR_MESSAGE);
				}
			}.start();
		}
	}

	/**
	 * Closes the server socket if it's opened. Also could be known as:
	 * stopAcceptingClients()
	 */
	private void closeServerSocket() {
		if (serverSocket != null) {
			try {
				final ServerSocket serverSocket_copy = serverSocket;
				serverSocket = null; // First we have to set to null the
				// reference, else NullPointerException
				// can be occur!
				// Because after closing server socket goes into a loop where
				// the serverSocket != null can be true!
				serverSocket_copy.close();
			} catch (final IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Check and returns whether server socket is created.
	 * 
	 * @return true if server socket is created; false otherwise
	 */
	public boolean isServerSocketCreated() {
		return serverSocket != null;
	}

	/**
	 * Called systematically, so it can check and watch the collection of
	 * players.<br>
	 * When this method is called, inserting to clientStubs is allowed, no
	 * iterator is existing on it.
	 */
	public void nextIteration() {
		if (newClientContact != null) {
			try {
				// Still part of the joining potocol
				// Client must send
				// Server.Commands.SENDING_PUBLIC_CLIENT_OPTIONS
				final GeneralStringTokenizer commandTokenizer = new GeneralStringTokenizer(newClientContact.connectionStub.receiveMessage());
				commandTokenizer.nextIntToken();
				newClientContact.publicClientOptions = PublicClientOptions.parseFromString(commandTokenizer.remainingString());
				server.broadcastCommand(Client.Commands.A_CLIENT_HAS_JOINED_THE_GAME.ordinal() + GeneralStringTokenizer.GENERAL_SEPARATOR_STRING
				        + newClientContact.publicClientOptions.packToString());
				clientContacts.add(newClientContact);

				newClientContact.connectionStub.sendMessage("" + clientContacts.size());
				for (final ClientContact clientContact : clientContacts) {
					newClientContact.connectionStub.sendMessage(clientContact.publicClientOptions.packToString());
					// End of joining protocol
				}

				server.broadcastMessage(Server.SERVER_CHAT_NAME + newClientContact.publicClientOptions.clientName + " has joined the game.");
			} catch (final IOException ie) {
				ie.printStackTrace();
			}
			newClientContact = null;
		}
	}

	/**
	 * Starts accepting clients in a new trhead.<br>
	 * Runs until server socket is opened.
	 */
	private void startAcceptingClients() {
		new Thread() {

			public void run() {
				closed = false;
				while (serverSocket != null) {
					if (newClientContact == null) {
						ConnectionStub connectionStub_ = null;
						try {
							// Start of the joining protocol
							final ConnectionStub connectionStub = new ConnectionStub(serverSocket.accept());
							connectionStub_ = connectionStub; // We need this
							// outside the
							// try block but
							// want it to be
							// final here...

							// Introducing ourself...
							connectionStub.sendMessage(SERVER_IDENTIFICATION_STRING);
							// Authentication of the client...
							if (!connectionStub.receiveMessage().equals(Client.CLIENT_IDENTIFICATION_STRING)) {
								throw new AcceptingClientFailedException("Client is not a " + Consts.APPLICATION_NAME + " cilent");
							}
							connectionStub.sendMessage(Consts.APPLICATION_VERSION);
							if (!connectionStub.receiveMessage().equals(Consts.APPLICATION_VERSION)) {
								throw new AcceptingClientFailedException("Incompatible versions");
							}
							final String gamePassword = serverOptionsManager.getOptions().getPassword();
							final String receivedGamePassword = connectionStub.receiveMessage();
							if (gamePassword.equals("") || gamePassword.equals(receivedGamePassword)) {
								connectionStub.sendMessage(PASSWORD_ACCEPTED);
								newClientContact = new ClientContact(connectionStub);
							} else {
								connectionStub.sendMessage(PASSWORD_REJECTED);
								throw new AcceptingClientFailedException("Incorrect game password");
							}
							// The joining protocol is finished in the
							// nextIteration() method
						} catch (final AcceptingClientFailedException ae) {
							if (connectionStub_ != null) {
								connectionStub_.close();
							}
						} catch (final IOException ie) {}
					} else {
						try {
							Thread.sleep(10l);
						} catch (final InterruptedException ie) {
							ie.printStackTrace();
						}
					}
				}
				closed = true;
			}
		}.start();
	}

	/**
	 * Closes the player collector, ends the collecting of players (if it's in
	 * progress).
	 */
	public void close() {
		serverOptionsManager.unregisterOptionsChangeListener(this);
		closeServerSocket();
		try {
			// Not join(), because PlayerCollector is not a thread!
			while (!closed) {
				Thread.sleep(10l);
			}
		} catch (final InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * Method to be called when server options may have been changed.
	 * 
	 * @param oldOptions
	 *            the old server options before the change signed by calling
	 *            this method
	 * @param newOptions
	 *            the new server options are about to become effective
	 */
	public void optionsChanged(final ServerOptions oldOptions, final ServerOptions newOptions) {
		if (oldOptions.getGamePort() != newOptions.getGamePort()) {
			closeServerSocket();
			createServerSocket(newOptions.getGamePort());
			startAcceptingClients();
		}
	}

}

package com.braids.coffeebombermen.server;

import com.braids.coffeebombermen.options.model.PublicClientOptions;
import com.braids.coffeebombermen.utils.ConnectionStub;

/**
 * Represents a contact with a client. This includes storing the connection stub
 * to the client and all the informations must be known about the client ( for
 * example the names of the players playing through this client; the client is
 * ready for the next iteration etc).
 */
class ClientContact {

	/** The connection stub to the client. */
	public final ConnectionStub connectionStub;
	/** The public client options of the cilent. */
	public PublicClientOptions  publicClientOptions;
	/**
	 * Own index of this client at the client machines. We have to store this,
	 * clients later may be removed when they leave.
	 */
	public int                  ownIndex;
	/** New, unprocessed client actions. */
	public String               newClientActions;

	/**
	 * Creates a new ClientContact.
	 * 
	 * @param connectionStub
	 *            the connection stub to the client
	 */
	public ClientContact(final ConnectionStub connectionStub) {
		this.connectionStub = connectionStub;
	}

}

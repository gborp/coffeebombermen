package com.braids.coffeebombermen.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Represents a stub which is used to communicate with another computer.
 */
public class ConnectionStub {

	/** Socket representing the communication end point of the connection. */
	private final Socket         socket;
	/** Buffered reader used to read/receive messages from the connection. */
	private final BufferedReader input;
	/** Print writer used to write/send messages to the connection. */
	private final PrintWriter    output;

	/**
	 * Creates a new ConnectionStub.
	 * 
	 * @param socket
	 *            socket representing the communication end point of the
	 *            connection
	 * @throws IOException
	 *             if I/O error occurs during the connection initialization
	 */
	public ConnectionStub(final Socket socket) throws IOException {
		this.socket = socket;
		input = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), Charset.forName("utf-8")));
		output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), Charset.forName("utf-8"))), true);
	}

	/**
	 * Sends a message through this connection.
	 * 
	 * @param message
	 *            message to be sent
	 * @throws IOException
	 *             if I/O error occurs during sending the message
	 */
	public void sendMessage(final String message) throws IOException {
		output.println(message);
	}

	/**
	 * Checks whether new message is ready to be read/received.
	 * 
	 * @return true if new message is ready to be read/received; false otherwise
	 */
	public boolean hasNewMessage() {
		try {
			return input.ready(); // Well... this would return true if we don't
			// have a new line just a few characters
			// without '\n'... but this implementation is
			// the simpliest
		} catch (final IOException ie) {
			return false;
		}
	}

	/**
	 * Receives and returns the next message from this connection.
	 * 
	 * @return the received message
	 * @throws IOException
	 *             if I/O error occurs during sending the message
	 */
	public String receiveMessage() throws IOException {
		return input.readLine();
	}

	/**
	 * Closes this connection stub.
	 */
	public void close() {
		try {
			socket.close();
		} catch (final IOException ie) {
			ie.printStackTrace();
		}
	}

}

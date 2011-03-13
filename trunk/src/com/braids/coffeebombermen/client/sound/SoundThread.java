package com.braids.coffeebombermen.client.sound;

import java.util.concurrent.LinkedBlockingQueue;

public class SoundThread implements Runnable {

	private static SoundThread               singleton;

	private LinkedBlockingQueue<SoundEffect> queue;

	public SoundThread() {
		queue = new LinkedBlockingQueue<SoundEffect>();

		Thread thSound = new Thread(this);
		thSound.setDaemon(true);
		thSound.start();
	}

	private static synchronized SoundThread getInstance() {
		if (singleton == null) {
			singleton = new SoundThread();
		}

		return singleton;
	}

	public static void play(SoundEffect se) {
		getInstance().addSoundToQueue(se);
	}

	private void addSoundToQueue(SoundEffect se) {
		queue.offer(se);
	}

	public void run() {

		try {
			while (true) {
				SoundEffect se = queue.take();
				se.doPlay();
			}
		} catch (InterruptedException ex) {}

	}

}

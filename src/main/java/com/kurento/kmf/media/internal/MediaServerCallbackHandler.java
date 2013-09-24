package com.kurento.kmf.media.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.kurento.kmf.media.events.MediaEvent;
import com.kurento.kmf.media.objects.MediaObject;

public class MediaServerCallbackHandler {

	private final Map<Long, Map<String, MediaEventListener<? extends MediaEvent>>> listenerMap;

	MediaServerCallbackHandler() {
		this.listenerMap = new ConcurrentHashMap<Long, Map<String, MediaEventListener<? extends MediaEvent>>>();
	}

	public void onEvent(String callbackToken, Long id, MediaEvent kmsEvent) {
		Map<String, MediaEventListener<? extends MediaEvent>> listeners = this.listenerMap
				.get(id);
		fireEvent(callbackToken, listeners, kmsEvent);
	}

	public void onError(String callbackToken, MediaError kmsError) {
		// TODO Call the appropriate handler
	}

	public <T extends MediaEvent> MediaEventListener<T> addListener(
			MediaObject mediaObject, MediaEventListener<T> listener) {

		Long id = mediaObject.getObjectRef().getId();
		Map<String, MediaEventListener<? extends MediaEvent>> listeners = this.listenerMap
				.get(id);
		// TODO Sequence of calls may not be atomic here
		if (listeners == null) {
			listeners = new HashMap<String, MediaEventListener<? extends MediaEvent>>();
			this.listenerMap.put(id, listeners);
		}
		listeners.put(listener.getCallbackToken(), listener);
		return listener;
	}

	public <T extends MediaEvent> boolean removeListener(
			MediaObject mediaObject, MediaEventListener<T> listener) {
		MediaEventListener<? extends MediaEvent> removed = null;
		Map<String, MediaEventListener<? extends MediaEvent>> listeners = this.listenerMap
				.get(mediaObject.getObjectRef().getId());
		// TODO Sequence of calls may not be atomic here
		if (listeners != null) {
			removed = listeners.remove(listener.getCallbackToken());
			if (listeners.isEmpty()) {
				this.listenerMap.remove(mediaObject.getObjectRef().getId());
			}
		}

		return (removed != null);
	}

	public boolean removeAllListeners(MediaObject mediaObject) {
		return this.listenerMap.remove(mediaObject.getObjectRef().getId()) != null;
	}

	private void fireEvent(String callbackToken,
			Map<String, MediaEventListener<? extends MediaEvent>> listeners,
			MediaEvent event) {
		if (listeners != null) {
			MediaEventListener<? extends MediaEvent> listener = listeners
					.get(callbackToken);
			listener.internalOnEvent(event);
		}
	}

}
package com.fongmi.android.ltv.source;

import android.os.Handler;
import android.os.Looper;

import com.fongmi.android.ltv.App;
import com.fongmi.android.ltv.impl.AsyncCallback;
import com.fongmi.android.ltv.utils.Token;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tvbus.engine.TVCore;
import com.tvbus.engine.TVListener;
import com.tvbus.engine.TVService;

public class TvBus implements TVListener {

	private final Handler handler;
	private AsyncCallback callback;
	private TVCore tvcore;
	private String url;

	private static class Loader {
		static volatile TvBus INSTANCE = new TvBus();
	}

	public static TvBus get() {
		return Loader.INSTANCE;
	}

	public TvBus() {
		this.handler = new Handler(Looper.getMainLooper());
	}

	public void init() {
		tvcore = TVCore.getInstance();
		tvcore.setAuthUrl(Token.getAuth());
		tvcore.setUsername(Token.getName());
		tvcore.setPassword(Token.getPass());
		tvcore.setTVListener(this);
		TVService.start(App.get());
	}

	public void start(AsyncCallback callback, String url) {
		setCallback(callback);
		tvcore.start(url);
		setUrl(url);
	}

	public void stop() {
		tvcore.stop();
	}

	public static void destroy() {
		TVService.stop(App.get());
	}

	private void setCallback(AsyncCallback callback) {
		this.callback = callback;
	}

	private void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void onPrepared(String result) {
		JsonObject json = new Gson().fromJson(result, JsonObject.class);
		if (json.get("hls") == null || callback == null) return;
		handler.post(() -> callback.onResponse(json.get("hls").getAsString()));
	}

	@Override
	public void onStop(String result) {
		JsonObject json = new Gson().fromJson(result, JsonObject.class);
		int errno = json.get("errno").getAsInt();
		if (errno < 0) tvcore.start(url);
	}

	@Override
	public void onInited(String result) {
	}

	@Override
	public void onStart(String result) {
	}

	@Override
	public void onInfo(String result) {
	}

	@Override
	public void onQuit(String result) {
	}
}

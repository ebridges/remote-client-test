package com.bpcreates.remoteclient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemoteClientAdapter extends Thread {
  private final String hostname;
  private final Integer portnumber;
  private final RemoteClient remoteClient;
  private final RemoteClientCallback remoteClientCallback;
  private final AtomicBoolean open;

  public RemoteClientAdapter(RemoteClientCallback callback, String hostname, Integer portnumber) {
    this.remoteClientCallback = callback;
    this.remoteClient = RemoteClientFactory.i(hostname, portnumber);
    this.hostname = hostname;
    this.portnumber = portnumber;
    this.open = new AtomicBoolean(false);
  }

  @Override
  public void run() {
    try {
        this.remoteClient.open();
        this.remoteClientCallback.onOpen(hostname, portnumber);
        this.open.set(true);
    } catch (IOException e) {
      this.remoteClientCallback.onError("error opening remote client", e);
    }
  }

  public void submitMessage(String message) {
    Request request = new Request(message);
    try {
      Response response = this.remoteClient.submitRequest(request);
      this.remoteClientCallback.onMessage(response.getPayload());
    } catch (IOException e) {
      this.remoteClientCallback.onError("error submitting request", e);
    }
  }

  public void shutdown() {
    try {
      synchronized (this.remoteClient) {
        this.remoteClient.close();
        this.open.set(false);
        this.remoteClientCallback.onClose(hostname, portnumber);
      }
    } catch (IOException e) {
      this.remoteClientCallback.onError("error closing remote client", e);
    }
  }

  public boolean isOpen() {
    return this.open.get();
  }
}

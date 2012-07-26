package com.bpcreates.remoteclient;

/**
 * User: ebridges
 * Date: 7/26/12
 * Time: 7:19 AM
 */
public interface RemoteClientCallback {
    void onMessage(String message);
    void onOpen(String hostname, Integer portNumber);
    void onClose(String hostname, Integer portNumber);
    void onError(String s, Throwable e);
}

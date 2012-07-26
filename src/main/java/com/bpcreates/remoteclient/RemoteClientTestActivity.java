package com.bpcreates.remoteclient;

import static com.bpcreates.remoteclient.R.layout.main;
import static com.bpcreates.remoteclient.Util.notEmpty;
import static java.lang.String.format;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class RemoteClientTestActivity extends Activity implements RemoteClientCallback {

  private static String TAG = RemoteClientTestActivity.class.getName();

  private RemoteClientAdapter remoteClientAdapter;
  private final Handler responseHandler = new Handler();
  private final Handler statusHandler = new Handler();

  public void onStartChatButtonClicked(View view) {
    String host = getHostname();
    Integer port = getPortNumber();
    this.remoteClientAdapter = new RemoteClientAdapter(this, host, port);
    this.remoteClientAdapter.start();
    this.enableNewMessages();
  }

  public void onSubmitButtonClicked(View view) {
    if(this.remoteClientAdapter.isOpen()) {
      String message = getRequest();
      if(null != message) {
        this.remoteClientAdapter.submitMessage(message);
      } else {
        Log.i(TAG, "no message entered!");
        updateStatus("Message cannot be empty.");
      }
    } else {
      Log.w(TAG, "no active connection");
      updateStatus("Not connected!");
    }
  }

  private String getRequest() {
    EditText editText = (EditText) findViewById(R.id.ctl_mesg_field);
    Editable editable = editText.getEditableText();
    String mesg = null;
    if (null != editable) {
      mesg = editable.toString();
    }
    if (notEmpty(mesg)) {
      assert mesg != null;
      return mesg.trim();
    } else {
      return null;
    }
  }

  private String getHostname() {
    EditText editText = (EditText) findViewById(R.id.ctl_host_field);
    Editable editable = editText.getEditableText();
    String host = null;
    if (null != editable) {
      host = editable.toString();
    }
    if (notEmpty(host)) {
      return host;
    } else {
      return Constants.DEFAULT_HOST;
    }
  }

  private Integer getPortNumber() {
    EditText editText = (EditText) findViewById(R.id.ctl_port_field);
    Editable editable = editText.getEditableText();
    String port = null;
    if (null != editable) {
      port = editable.toString();
    }
    if (notEmpty(port)) {
      return Integer.valueOf(port);
    } else {
      return Constants.DEFAULT_PORT;
    }
  }

  private void updateStatus(String mesg) {
    Log.d(TAG, "updateStatus() called.");
    if (notEmpty(mesg)) {
      TextView statusArea = (TextView) findViewById(R.id.ctl_status_area);
      statusArea.setText(mesg);
    }
  }

  private void disableNewMessages() {
    Log.d(TAG, "disableNewMessages() called.");
    EditText mesgField = (EditText) findViewById(R.id.ctl_mesg_field);
    if (null != mesgField) {
      mesgField.setEnabled(false);
    }
    Button submitButton = (Button) findViewById(R.id.ctl_submit_button);
    if (null != submitButton) {
      submitButton.setEnabled(false);
    }
  }

  private void enableNewMessages() {
    Log.d(TAG, "enableNewMessages() called.");
    EditText mesgField = (EditText) findViewById(R.id.ctl_mesg_field);
    if (null != mesgField) {
      mesgField.setEnabled(true);
    }
    Button submitButton = (Button) findViewById(R.id.ctl_submit_button);
    if (null != submitButton) {
      submitButton.setEnabled(true);
    }
  }

  /**
   * Called when the activity is first created.
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this
   * Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is
   * null.</b>
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.i(TAG, "onCreate");
    setContentView(main);
    disableNewMessages();
    this.remoteClientAdapter = null;
  }

  @Override
  public void onStart() {
    Log.d(TAG, "onStart() called.");
    super.onStart();
  }

  @Override
  public void onResume() {
    Log.d(TAG, "onResume() called.");
    super.onResume();
    disableNewMessages();
  }

  @Override
  public void onPause() {
    Log.d(TAG, "onPause() called.");
    super.onPause();
    disableNewMessages();
  }

  @Override
  public void onStop() {
    Log.d(TAG, "onStop() called");
    super.onStop();
    disableNewMessages();
  }

  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy() called");
    super.onDestroy();
    if (null != this.remoteClientAdapter) {
      this.remoteClientAdapter.shutdown();
    }
  }

  /* here */
  @Override
  public void onMessage(String message) {
    if(notEmpty(message)) {
      EditText responseArea = (EditText) findViewById(R.id.ctl_response_area);
      ResponseUpdateRunnable responseUpdate = new ResponseUpdateRunnable(responseArea, message);
      this.responseHandler.post(responseUpdate);
    } else {
      postStatusMessage("received an empty message");
    }
  }

  @Override
  public void onOpen(String hostname, Integer portnumber) {
    String message = format("Opened connection to %s:%s", hostname, portnumber);
    Log.i(TAG, message);
    postStatusMessage(message);
  }

  @Override
  public void onClose(String hostname, Integer portnumber) {
    String message = format("Closed connection to %s:%s", hostname, portnumber);
    Log.i(TAG, message);
    postStatusMessage(message);
  }

  @Override
  public void onError(String mesg, Throwable e) {
    String message = format("ERROR %s: %s", mesg, e.getMessage());
    Log.e(TAG, message, e);
    postStatusMessage(message);
  }
  /* to here */

  private void postStatusMessage(String message){
    TextView statusArea = (TextView) findViewById(R.id.ctl_status_area);
    StatusUpdateRunnable statusUpdate = new StatusUpdateRunnable(statusArea, message);
    this.statusHandler.post(statusUpdate);
  }

  class StatusUpdateRunnable implements Runnable {
    private TextView statusArea;
    private String message;

    StatusUpdateRunnable(TextView statusArea, String message) {
      this.statusArea = statusArea;
      this.message = message;
    }

    @Override
    public void run() {
      statusArea.setText(message);
    }
  }

  class ResponseUpdateRunnable implements Runnable {
    private EditText responseArea;
    private String message;

    ResponseUpdateRunnable(EditText responseArea, String message) {
      this.responseArea = responseArea;
      this.message = message;
    }

    @Override
    public void run() {
      responseArea.getText().append(format("%s\n", message));
    }
  }
}


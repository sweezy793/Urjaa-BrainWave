package com.github.pwittchen.neurosky.app;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.github.pwittchen.neurosky.library.NeuroSky;
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException;
import com.github.pwittchen.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.github.pwittchen.neurosky.library.message.enums.BrainWave;
import com.github.pwittchen.neurosky.library.message.enums.Signal;
import com.github.pwittchen.neurosky.library.message.enums.State;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

  private final static String LOG_TAG = "NeuroSky";
  private NeuroSky neuroSky;

  @BindView(R.id.tv_state) TextView tvState;
  @BindView(R.id.tv_attention) TextView tvAttention;
  @BindView(R.id.tv_temp) TextView tvTemp;
  @BindView(R.id.tv_meditation) TextView tvMeditation;
  @BindView(R.id.tv_blink) TextView tvBlink;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    neuroSky = createNeuroSky();
  }

  @Override protected void onResume() {
    super.onResume();
    if (neuroSky != null && neuroSky.isConnected()) {
      neuroSky.start();
    }
  }

  @Override protected void onPause() {
    super.onPause();
    if (neuroSky != null && neuroSky.isConnected()) {
      neuroSky.stop();
    }
  }

  @NonNull private NeuroSky createNeuroSky() {
    return new NeuroSky(new ExtendedDeviceMessageListener() {
      @Override public void onStateChange(State state) {
        handleStateChange(state);
      }

      @Override public void onSignalChange(Signal signal) {
        handleSignalChange(signal);
      }

      @Override public void onBrainWavesChange(Set<BrainWave> brainWaves) {
        handleBrainWavesChange(brainWaves);
      }
    });
  }

  private void handleStateChange(final State state) {
    if (neuroSky != null && state.equals(State.CONNECTED)) {
      neuroSky.start();
    }

    tvState.setText(state.toString());
    Log.d(LOG_TAG, state.toString());
  }

  private void handleSignalChange(final Signal signal) {
    switch (signal) {
      case ATTENTION:
        tvAttention.setText(getFormattedMessage("Attention: %d", signal));
        int sig=signal.getValue();
        if(sig<50&&sig>0)
        {
          tvTemp.setText("Danger");
          ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
          toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 50000);
          Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
          v.vibrate(400);
        }
        else
        {
          tvTemp.setText("You're Safe");
        }
        break;
      case MEDITATION:
        tvMeditation.setText(getFormattedMessage("Meditation: %d", signal));
        break;
      case BLINK:
        tvBlink.setText(getFormattedMessage("Blink: %d", signal));
        break;
    }

    Log.d(LOG_TAG, String.format("%s: %d", signal.toString(), signal.getValue()));
  }

  private String getFormattedMessage(String messageFormat, Signal signal) {
    return String.format(Locale.getDefault(), messageFormat, signal.getValue());
  }

  private void handleBrainWavesChange(final Set<BrainWave> brainWaves) {
    for (BrainWave brainWave : brainWaves) {
      Log.d(LOG_TAG, String.format("%s: %d", brainWave.toString(), brainWave.getValue()));
    }
  }

  @OnClick(R.id.btn_connect) void connect() {
    try {
      neuroSky.connect();
    } catch (BluetoothNotEnabledException e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
      Log.d(LOG_TAG, e.getMessage());
    }
  }

  @OnClick(R.id.btn_disconnect) void disconnect() {
    neuroSky.disconnect();
  }

  @OnClick(R.id.btn_start_monitoring) void startMonitoring() {
    neuroSky.start();
  }

  @OnClick(R.id.btn_stop_monitoring) void stopMonitoring() {
    neuroSky.stop();
  }
}

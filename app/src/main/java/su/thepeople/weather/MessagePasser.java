package su.thepeople.weather;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.MainThread;

import java.util.function.Consumer;

/**
 * Helper class to handle passing messages to the main thread from any other thread.
 */
public class MessagePasser {
    private final Consumer<WeatherReport> weatherReportAcceptor;
    private final Handler internalHandler;

    @MainThread
    public MessagePasser(Looper mainThreadLooper, Consumer<WeatherReport> weatherReportAcceptor) {
        this.weatherReportAcceptor = weatherReportAcceptor;
        internalHandler = new Handler(mainThreadLooper, this::processMessage);
    }

    // Calling this method (from any thread) will deliver the report to the main thread.
    public void sendNewWeatherReport(WeatherReport newReport) {
        Message msg = Message.obtain();
        msg.obj = newReport;
        internalHandler.sendMessage(msg);
    }

    private boolean processMessage(Message message) {
        WeatherReport report = (WeatherReport) message.obj;
        weatherReportAcceptor.accept(report);
        return true;
    }
}

package su.thepeople.weather;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.function.Consumer;

public class MessagePasser {
    private final Consumer<WeatherReport> weatherReportAcceptor;
    private final Handler internalHandler;

    public MessagePasser(Looper mainThreadLooper, Consumer<WeatherReport> weatherReportAcceptor) {

        // must be on main thread!
        this.weatherReportAcceptor = weatherReportAcceptor;

        internalHandler = new Handler(mainThreadLooper, this::processMessage);
    }

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

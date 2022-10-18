package su.thepeople.weather;

import com.google.android.gms.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class implements all the boilerplate code that we need to handle network requests and responses.
 *
 * refresh()
 * - Sends a new request, if one isn't already pending.
 *
 * on request send
 * - Call supplied sender function
 *
 */
public class RequestLifecycle<T> {

    boolean requestUnderway = false;

    private Runnable requestFcn;
    private Consumer<T> responseFcn;

    // TODO: add error handling!

    private T lastResponse;
    private LocalDateTime lastResponseTime;

    private Duration maxResponseAge;
    private Duration responseTimeout;
    private Duration retryTime;

    private Timer timer;

    private TimerTask retryTask;
    private TimerTask refreshTask;

    private static TimerTask taskOf(Runnable fcn) {
        return new TimerTask() {
            public void run() {
                fcn.run();
            }
        };
    }

    private RequestLifecycle(Duration maxAge, Duration responseTimeout, Duration retryTime) {
        this.maxResponseAge = maxAge;
        this.responseTimeout = responseTimeout;
        this.retryTime = retryTime;
    }

    private void init(Runnable requestFcn, Consumer<T> responseFcn) {
        this.requestFcn = requestFcn;
        this.responseFcn = responseFcn;
    }

    public void forceRefresh() {
        if (!requestUnderway) {
            dispatchRequest();
        }
    }

    private void refreshIfNecessary() {
        if (isLastResponseTooOld()) {
            forceRefresh();
        }
    }

    private boolean isLastResponseTooOld() {
        if (lastResponse == null) return true;
        Duration age = Duration.between(lastResponseTime, LocalDateTime.now());
        return age.compareTo(maxResponseAge) > 0;
    }

    private void dispatchRequest() {
        requestUnderway = true;
        requestFcn.run();
        retryTask = taskOf(this::dispatchRequest);
        timer.schedule(retryTask, responseTimeout.toMillis());
    }

    public void onSuccessfulResult(T response) {
        if (retryTask != null) {
            retryTask.cancel();
        }
        responseFcn.accept(response);
        refreshTask = taskOf(this::refreshIfNecessary);
        timer.schedule(refreshTask, maxResponseAge.toMillis() + Duration.ofSeconds(30).toMillis());
    }


    public static <T> RequestLifecycle<T> forGmsTask(Supplier<Task<T>> requester, Consumer<T> responseProcessor, Duration maxAge, Duration responseTimeout, Duration retryTime) {
        RequestLifecycle<T> lc = new RequestLifecycle<>(maxAge, responseTimeout, retryTime);
        Runnable doRequest = () -> {
            Task<T> task = requester.get();
            task.addOnSuccessListener(lc::onSuccessfulResult);
        };
        lc.init(doRequest, responseProcessor);
        return lc;
    }
}

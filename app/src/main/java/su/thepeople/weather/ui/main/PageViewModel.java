package su.thepeople.weather.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {

    private final MutableLiveData<Integer> mIndex = new MutableLiveData<>();

    public void setIndex(int index) {
        mIndex.setValue(index);
    }
}
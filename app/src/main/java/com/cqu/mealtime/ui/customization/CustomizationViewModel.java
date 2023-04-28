package com.cqu.mealtime.ui.customization;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CustomizationViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CustomizationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Customization fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
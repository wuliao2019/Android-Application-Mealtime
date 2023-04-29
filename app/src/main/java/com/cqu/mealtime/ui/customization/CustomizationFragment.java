package com.cqu.mealtime.ui.customization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cqu.mealtime.databinding.FragmentCustomizationBinding;

public class CustomizationFragment extends Fragment {

    private FragmentCustomizationBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("customCreate!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CustomizationViewModel notificationsViewModel = new ViewModelProvider(this).get(CustomizationViewModel.class);

        binding = FragmentCustomizationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCustomization;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
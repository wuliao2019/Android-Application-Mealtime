package com.cqu.mealtime.ui.customization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cqu.mealtime.databinding.FragmentCustomizationBinding;

public class CustomizationFragment extends Fragment {

    private FragmentCustomizationBinding binding;
    ScaleAnimation scaleAnimation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        System.out.println("customCreate!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CustomizationViewModel notificationsViewModel = new ViewModelProvider(this).get(CustomizationViewModel.class);

        binding = FragmentCustomizationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//        ImageButton imageView = binding.imageButton;
//        Button button = binding.button;
//        imageView.setAnimation(scaleAnimation);
//        button.setOnClickListener(v -> imageView.startAnimation(scaleAnimation));
//        final TextView textView = binding.textCustomization;
//        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
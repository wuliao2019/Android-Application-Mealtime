package com.cqu.mealtime.ui.customization;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.cqu.mealtime.LoginActivity;
import com.cqu.mealtime.databinding.FragmentCustomizationBinding;

public class CustomizationFragment extends Fragment {

    private FragmentCustomizationBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CustomizationViewModel notificationsViewModel = new ViewModelProvider(this).get(CustomizationViewModel.class);

        binding = FragmentCustomizationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView t1 = binding.nameArea, t2 = binding.idArea, t3 = binding.mapNumArea;
        SharedPreferences sp = getContext().getSharedPreferences("user_inf", Context.MODE_PRIVATE);
        t1.setText(sp.getString("name", "---"));
        t2.setText(String.valueOf(sp.getInt("id", 0)));
        t3.setText(sp.getString("map_num", "---"));
        Button button = binding.buttonLogout;
        button.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getContext().getSharedPreferences("user_inf", Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
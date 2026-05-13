package com.example.studyflow.ui.deadlines;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.gridlayout.widget.GridLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.studyflow.R;
import com.example.studyflow.databinding.SheetFlagColorBinding;

public class FlagColorSheet extends BottomSheetDialogFragment {

    public interface OnFlagColorSelectedListener {
        void onFlagColorSelected(@Nullable String flagColor);
    }

    private static final String[] FLAG_COLORS = {
            null, "#EA4335", "#FF8A00", "#FBBC04", "#34A853", "#5E92F3", "#9C27B0"
    };
    private static final String[] FLAG_LABELS = {
            "Không cờ", "Đỏ", "Cam", "Vàng", "Xanh lá", "Xanh dương", "Tím"
    };

    private SheetFlagColorBinding binding;
    private OnFlagColorSelectedListener listener;
    private String currentColor;

    public static FlagColorSheet newInstance(@Nullable String currentColor) {
        FlagColorSheet sheet = new FlagColorSheet();
        Bundle args = new Bundle();
        args.putString("current", currentColor);
        sheet.setArguments(args);
        return sheet;
    }

    public FlagColorSheet setOnFlagColorSelectedListener(OnFlagColorSelectedListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialog);
        if (getArguments() != null) {
            currentColor = getArguments().getString("current");
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SheetFlagColorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btnClose.setOnClickListener(v -> dismiss());
        buildColorGrid();
    }

    private void buildColorGrid() {
        binding.gridFlagColors.removeAllViews();
        int cellSize = (int) (72 * getResources().getDisplayMetrics().density);
        int margin = (int) (6 * getResources().getDisplayMetrics().density);
        int iconSize = (int) (28 * getResources().getDisplayMetrics().density);

        for (int i = 0; i < FLAG_COLORS.length; i++) {
            final int index = i;
            String color = FLAG_COLORS[i];
            boolean selected = (color == null && (currentColor == null || currentColor.isEmpty()))
                    || (color != null && color.equals(currentColor));

            LinearLayout cell = new LinearLayout(requireContext());
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER_HORIZONTAL);

            FrameLayout iconWrap = new FrameLayout(requireContext());
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor("#F4F4F5"));
            if (selected) {
                circle.setStroke((int) (2 * getResources().getDisplayMetrics().density),
                        Color.parseColor("#FF8A00"));
            }
            iconWrap.setBackground(circle);

            ImageView flagIcon = new ImageView(requireContext());
            flagIcon.setImageResource(R.drawable.ic_flag);
            int tint = color == null ? Color.parseColor("#C7C7CC") : Color.parseColor(color);
            flagIcon.setColorFilter(tint);
            FrameLayout.LayoutParams iconLp = new FrameLayout.LayoutParams(iconSize, iconSize, Gravity.CENTER);
            iconWrap.addView(flagIcon, iconLp);

            int wrapSize = (int) (52 * getResources().getDisplayMetrics().density);
            cell.addView(iconWrap, new LinearLayout.LayoutParams(wrapSize, wrapSize));

            TextView label = new TextView(requireContext());
            label.setText(FLAG_LABELS[i]);
            label.setTextSize(11f);
            label.setTextColor(Color.parseColor("#8E8E93"));
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, (int) (6 * getResources().getDisplayMetrics().density), 0, 0);
            cell.addView(label, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            cell.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFlagColorSelected(FLAG_COLORS[index]);
                }
                dismiss();
            });

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = cellSize;
            lp.height = GridLayout.LayoutParams.WRAP_CONTENT;
            lp.setMargins(margin, margin, margin, margin);
            lp.columnSpec = GridLayout.spec(i % 4);
            lp.rowSpec = GridLayout.spec(i / 4);
            binding.gridFlagColors.addView(cell, lp);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

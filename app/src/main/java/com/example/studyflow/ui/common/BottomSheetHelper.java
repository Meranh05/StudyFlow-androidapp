package com.example.studyflow.ui.common;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public final class BottomSheetHelper {

    private static final float MAX_SCREEN_FRACTION = 0.92f;

    private BottomSheetHelper() {}

    /** Sheet cao vừa nội dung; nếu dài hơn ~92% màn hình thì cuộn bên trong. */
    public static void fitToContent(BottomSheetDialogFragment fragment, View contentRoot) {
        if (!(fragment.getDialog() instanceof BottomSheetDialog)) return;
        BottomSheetDialog dialog = (BottomSheetDialog) fragment.getDialog();

        FrameLayout sheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null || contentRoot == null) return;

        applyBottomInset(contentRoot);

        sheet.post(() -> {
            int maxHeight = (int) (sheet.getResources().getDisplayMetrics().heightPixels
                    * MAX_SCREEN_FRACTION);

            int widthSpec = View.MeasureSpec.makeMeasureSpec(sheet.getWidth(), View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            contentRoot.measure(widthSpec, heightSpec);
            int contentHeight = contentRoot.getMeasuredHeight();
            int sheetHeight = Math.min(contentHeight, maxHeight);

            ViewGroup.LayoutParams contentParams = contentRoot.getLayoutParams();
            if (contentHeight > maxHeight) {
                contentParams.height = maxHeight;
            } else {
                contentParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            contentRoot.setLayoutParams(contentParams);

            ViewGroup.LayoutParams sheetParams = sheet.getLayoutParams();
            sheetParams.height = sheetHeight;
            sheet.setLayoutParams(sheetParams);

            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(sheet);
            behavior.setFitToContents(true);
            behavior.setSkipCollapsed(true);
            behavior.setPeekHeight(sheetHeight);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });
    }

    private static void applyBottomInset(View contentRoot) {
        ViewCompat.setOnApplyWindowInsetsListener(contentRoot, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    bars.bottom + view.getResources().getDimensionPixelSize(
                            com.example.studyflow.R.dimen.sheet_bottom_inset_extra));
            return insets;
        });
        ViewCompat.requestApplyInsets(contentRoot);
    }
}

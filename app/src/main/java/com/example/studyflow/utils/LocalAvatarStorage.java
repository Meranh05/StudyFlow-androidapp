package com.example.studyflow.utils;

import android.content.Context;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.studyflow.R;
import java.io.*;

public final class LocalAvatarStorage {
    private static final String DIR = "avatars";

    private LocalAvatarStorage() {}

    private static File avatarFile(Context context, String userId) {
        File dir = new File(context.getFilesDir(), DIR);
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, userId + ".jpg");
    }

    public static boolean exists(Context context, String userId) {
        return avatarFile(context, userId).exists();
    }

    public static File getFile(Context context, String userId) {
        File file = avatarFile(context, userId);
        return file.exists() ? file : null;
    }

    public static void save(Context context, String userId, byte[] jpegBytes) throws IOException {
        File file = avatarFile(context, userId);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(jpegBytes);
            out.flush();
        }
    }

    public static void delete(Context context, String userId) {
        File file = avatarFile(context, userId);
        if (file.exists()) file.delete();
    }

    public static void loadInto(Fragment fragment, String userId, ImageView target) {
        File file = getFile(fragment.requireContext(), userId);
        if (file != null) {
            Glide.with(fragment)
                    .load(file)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .into(target);
        } else {
            target.setImageResource(R.drawable.ic_default_avatar);
        }
    }
}

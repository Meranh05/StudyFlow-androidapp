package com.example.studyflow.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {
    public static String validateEmail(String email) {
        if (TextUtils.isEmpty(email)) return "Vui lòng nhập email";
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Email không hợp lệ";
        return null;
    }

    public static String validatePassword(String password) {
        if (TextUtils.isEmpty(password)) return "Vui lòng nhập mật khẩu";
        if (password.length() < 6) return "Mật khẩu tối thiểu 6 ký tự";
        return null;
    }

    public static String validateRequired(String value, String fieldName) {
        if (TextUtils.isEmpty(value)) return fieldName + " không được để trống";
        return null;
    }
}
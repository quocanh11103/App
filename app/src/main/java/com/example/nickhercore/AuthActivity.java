package com.example.nickhercore;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AuthActivity extends AppCompatActivity {

    private AppDatabaseHelper db;

    private boolean isLoginMode = true;

    private LinearLayout formLayout;
    private TextView tabLogin;
    private TextView tabRegister;

    private EditText edtName;
    private EditText edtEmail;
    private EditText edtPassword;
    private EditText edtConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new AppDatabaseHelper(this);

        if (db.isLoggedIn()) {
            openMain();
            return;
        }

        createLayout();
        renderForm();
    }

    private void createLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#07111F"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(38), dp(24), dp(24));
        scrollView.addView(root);

        TextView logo = new TextView(this);
        logo.setText("N");
        logo.setTextSize(40);
        logo.setGravity(Gravity.CENTER);
        logo.setTextColor(Color.parseColor("#00D68F"));
        logo.setTypeface(null, Typeface.BOLD);
        logo.setBackground(roundBg("#10233A", "#00D68F", 2, 18));
        root.addView(logo, new LinearLayout.LayoutParams(dp(94), dp(94)));
        ((LinearLayout.LayoutParams) logo.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;

        TextView title = text("NickherCore", 34, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(20), 0, dp(2));
        root.addView(title);

        TextView subtitle = text("World Cup 2026  •  Live Scores", 17, Color.parseColor("#B8C7D9"), false);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        TextView welcome = text("Chào mừng bạn trở lại! Đăng nhập để theo dõi\nkết quả, lịch thi đấu và nhiều hơn nữa.", 15, Color.parseColor("#9FB2C7"), false);
        welcome.setGravity(Gravity.CENTER);
        welcome.setPadding(0, dp(24), 0, dp(24));
        root.addView(welcome);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackground(roundBg("#10233A", "#26384F", 1, 16));

        tabLogin = text("Đăng nhập", 17, Color.WHITE, true);
        tabLogin.setGravity(Gravity.CENTER);
        tabRegister = text("Đăng ký", 17, Color.parseColor("#9FB2C7"), true);
        tabRegister.setGravity(Gravity.CENTER);

        tabs.addView(tabLogin, new LinearLayout.LayoutParams(0, dp(58), 1));
        tabs.addView(tabRegister, new LinearLayout.LayoutParams(0, dp(58), 1));

        root.addView(tabs);

        tabLogin.setOnClickListener(v -> {
            isLoginMode = true;
            renderForm();
        });

        tabRegister.setOnClickListener(v -> {
            isLoginMode = false;
            renderForm();
        });

        formLayout = new LinearLayout(this);
        formLayout.setOrientation(LinearLayout.VERTICAL);
        formLayout.setPadding(dp(18), dp(22), dp(18), dp(18));
        formLayout.setBackground(roundBg("#10233A", "#26384F", 1, 18));

        LinearLayout.LayoutParams formParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        formParams.topMargin = dp(20);
        root.addView(formLayout, formParams);

        TextView secure = text("🛡  Bảo mật local SQLite  •  Dữ liệu dùng cho bài nộp", 13, Color.parseColor("#8FA8C5"), false);
        secure.setGravity(Gravity.CENTER);
        secure.setPadding(0, dp(22), 0, 0);
        root.addView(secure);

        setContentView(scrollView);
    }

    private void renderForm() {
        if (formLayout == null) return;

        formLayout.removeAllViews();

        tabLogin.setTextColor(isLoginMode ? Color.parseColor("#00D68F") : Color.parseColor("#9FB2C7"));
        tabRegister.setTextColor(isLoginMode ? Color.parseColor("#9FB2C7") : Color.parseColor("#00D68F"));

        if (!isLoginMode) {
            formLayout.addView(label("Họ tên"));
            edtName = input("Nhập họ tên", false);
            formLayout.addView(edtName);
        }

        formLayout.addView(label("Email hoặc số điện thoại"));
        edtEmail = input("Nhập email hoặc số điện thoại", false);
        formLayout.addView(edtEmail);

        formLayout.addView(label("Mật khẩu"));
        edtPassword = input("Nhập mật khẩu", true);
        formLayout.addView(edtPassword);

        if (!isLoginMode) {
            formLayout.addView(label("Nhập lại mật khẩu"));
            edtConfirmPassword = input("Xác nhận mật khẩu", true);
            formLayout.addView(edtConfirmPassword);
        }

        if (isLoginMode) {
            TextView forgot = text("Quên mật khẩu?", 14, Color.parseColor("#00D68F"), true);
            forgot.setGravity(Gravity.RIGHT);
            forgot.setPadding(0, dp(10), 0, dp(18));
            forgot.setOnClickListener(v -> Toast.makeText(this, "Chức năng quên mật khẩu sẽ thêm khi dùng Firebase.", Toast.LENGTH_LONG).show());
            formLayout.addView(forgot);
        } else {
            addSpace(formLayout, 18);
        }

        Button mainButton = new Button(this);
        mainButton.setText(isLoginMode ? "Đăng nhập" : "Đăng ký");
        mainButton.setTextColor(Color.WHITE);
        mainButton.setTextSize(17);
        mainButton.setTypeface(null, Typeface.BOLD);
        mainButton.setBackground(roundBg("#00A86B", "#00D68F", 0, 16));
        mainButton.setOnClickListener(v -> handleAuth());

        formLayout.addView(mainButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
        ));

        TextView or = text("hoặc", 14, Color.parseColor("#8FA8C5"), false);
        or.setGravity(Gravity.CENTER);
        or.setPadding(0, dp(22), 0, dp(14));
        formLayout.addView(or);

        LinearLayout socialRow = new LinearLayout(this);
        socialRow.setOrientation(LinearLayout.HORIZONTAL);

        socialRow.addView(socialButton("Google"), new LinearLayout.LayoutParams(0, dp(48), 1));
        socialRow.addView(socialButton("Apple"), new LinearLayout.LayoutParams(0, dp(48), 1));
        socialRow.addView(socialButton("Facebook"), new LinearLayout.LayoutParams(0, dp(48), 1));

        formLayout.addView(socialRow);

        TextView switchText = text(
                isLoginMode ? "Chưa có tài khoản? Đăng ký ngay" : "Đã có tài khoản? Đăng nhập",
                14,
                Color.parseColor("#00D68F"),
                true
        );
        switchText.setGravity(Gravity.CENTER);
        switchText.setPadding(0, dp(24), 0, 0);
        switchText.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            renderForm();
        });
        formLayout.addView(switchText);
    }

    private void handleAuth() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoginMode) {
            boolean ok = db.loginUser(email, password);

            if (ok) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                openMain();
            } else {
                Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_LONG).show();
            }

            return;
        }

        String name = edtName.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean registered = db.registerUser(name, email, password);

        if (!registered) {
            Toast.makeText(this, "Email đã tồn tại", Toast.LENGTH_LONG).show();
            return;
        }

        db.loginUser(email, password);
        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
        openMain();
    }

    private TextView label(String text) {
        TextView tv = text(text, 14, Color.parseColor("#B8C7D9"), false);
        tv.setPadding(0, dp(12), 0, dp(8));
        return tv;
    }

    private EditText input(String hint, boolean password) {
        EditText edt = new EditText(this);
        edt.setHint(hint);
        edt.setTextColor(Color.WHITE);
        edt.setHintTextColor(Color.parseColor("#7E93AA"));
        edt.setTextSize(15);
        edt.setSingleLine(true);
        edt.setPadding(dp(14), 0, dp(14), 0);
        edt.setBackground(roundBg("#07111F", "#26384F", 1, 14));

        if (password) {
            edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        edt.setLayoutParams(params);

        return edt;
    }

    private TextView socialButton(String text) {
        TextView tv = text(text, 14, Color.WHITE, true);
        tv.setGravity(Gravity.CENTER);
        tv.setBackground(roundBg("#07111F", "#26384F", 1, 12));
        tv.setOnClickListener(v -> Toast.makeText(this, text + " sẽ thêm sau khi dùng Firebase/Auth SDK.", Toast.LENGTH_SHORT).show());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1);
        params.setMargins(dp(4), 0, dp(4), 0);
        tv.setLayoutParams(params);

        return tv;
    }

    private void addSpace(LinearLayout layout, int h) {
        TextView s = new TextView(this);
        layout.addView(s, new LinearLayout.LayoutParams(1, dp(h)));
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(size);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }

    private GradientDrawable roundBg(String color, String stroke, int strokeWidth, int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(color));
        gd.setCornerRadius(dp(radius));
        if (strokeWidth > 0) {
            gd.setStroke(dp(strokeWidth), Color.parseColor(stroke));
        }
        return gd;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
package com.whale.share;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private EditText etPostLink, etGroupLinks;
    private TextView tvTimer;
    private Button btnStartShare;

    private List<String> groupsList = new ArrayList<>();
    private int currentGroupIndex = 0;
    private CountDownTimer countDownTimer;
    private String postUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPostLink = findViewById(R.id.etPostLink);
        etGroupLinks = findViewById(R.id.etGroupLinks);
        tvTimer = findViewById(R.id.tvTimer);
        btnStartShare = findViewById(R.id.btnStartShare);

        btnStartShare.setOnClickListener(v -> {
            postUrl = etPostLink.getText().toString().trim();
            String groupsInput = etGroupLinks.getText().toString().trim();

            if (postUrl.isEmpty()) {
                Toast.makeText(MainActivity.this, "الرجاء إدخال رابط المنشور أولاً", Toast.LENGTH_SHORT).show();
                return;
            }

            if (groupsInput.isEmpty()) {
                Toast.makeText(MainActivity.this, "الرجاء إدخال روابط المجموعات", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] lines = groupsInput.split("\n");
            groupsList = new ArrayList<>(Arrays.asList(lines));
            groupsList.removeIf(String::isEmpty);

            if (groupsList.isEmpty()) {
                Toast.makeText(MainActivity.this, "تأكد من كتابة الروابط بشكل صحيح", Toast.LENGTH_SHORT).show();
                return;
            }

            currentGroupIndex = 0;
            startProcessForNextGroup();
        });
    }

    private void startProcessForNextGroup() {
        if (currentGroupIndex >= groupsList.size()) {
            tvTimer.setText("اكتملت جميع المشاركات! 🎉");
            Toast.makeText(this, "تم الانتهاء من جميع المجموعات بنجاح", Toast.LENGTH_LONG).show();
            return;
        }

        // توجيه النظام لفتح شاشة النشر المباشرة داخل فيسبوك للجروب الحالي
        openFacebookNativeShare(groupsList.get(currentGroupIndex), postUrl);

        currentGroupIndex++;

        // بدء العداد التنازلي التلقائي لـ 10 ثوانٍ
        startCountdown();
    }

    private void openFacebookNativeShare(String groupUrl, String postUrlToShare) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, postUrlToShare);
            
            // إجبار النظام على استخدام تطبيق فيسبوك الرسمي لفتح الواجهة المطلوبة فوراً
            intent.setPackage("com.facebook.katana");
            
            // محاولة توجيه النشر لداخل الجروب المستهدف
            intent.putExtra("com.facebook.platform.extra.APPLICATION_ID", groupUrl); 
            
            startActivity(intent);
        } catch (Exception e) {
            // متصفح احتياطي في حال عدم وجود التطبيق الرسمي
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupUrl));
            startActivity(browserIntent);
        }
    }

    private void startCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("الانتقال للجروب التالي بعد: " + (millisUntilFinished / 1000) + " ثوانٍ ⏱️");
            }

            @Override
            public void onFinish() {
                startProcessForNextGroup();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

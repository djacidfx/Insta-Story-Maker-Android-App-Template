package com.example.storymaker.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;

import com.example.storymaker.R;
import com.example.storymaker.help.ConnectionDetector;
import com.example.storymaker.models.Draft;
import com.example.storymaker.utils.AppUtil;
import com.example.storymaker.utils.ScreenUtil;
import com.google.gson.Gson;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PreviewActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;
    public static final String mypreference = "myprefadmob";
    ConnectionDetector connectionDetector;

    int whichActivitytoStart = 0;
    boolean isActivityLeft;

    AppCompatActivity activity;

    @BindView(R.id.cv_wrapper)
    CardView cvWrapper;
    private Draft draft;
    @BindView(R.id.iv_saved)
    ImageView imageSaved;
    private File imgFile;
    private Uri imgUri;
    private Intent intent;
    private boolean isFromCreation;

    private long mLastClickTime = System.currentTimeMillis();

    @SuppressLint({"SetTextI18n"})
    private void init() {
        int i;
        int i2;
        this.draft = (Draft) new Gson().fromJson(getIntent().getStringExtra("draft"), Draft.class);
        this.imgFile = (File) getIntent().getExtras().get("savedImageFile");
        this.isFromCreation = getIntent().getBooleanExtra("FromCreation", false);

        if (VERSION.SDK_INT > 24) {
            Context applicationContext = getApplicationContext();
            String sb5 = getPackageName() +
                    ".provider";
            this.imgUri = FileProvider.getUriForFile(applicationContext, sb5, this.imgFile);
        } else {
            this.imgUri = Uri.fromFile(this.imgFile);
        }
        this.cvWrapper.post(new Runnable() {
            public void run() {
                int measuredHeight = PreviewActivity.this.cvWrapper.getMeasuredHeight();
                if (measuredHeight > ScreenUtil.getScreenWidth(PreviewActivity.this)) {
                    measuredHeight = ScreenUtil.getScreenWidth(PreviewActivity.this);
                }
                double d = (double) measuredHeight;
                Double.isNaN(d);
                int i = (int) (d * 0.5625d);
                LayoutParams layoutParams = PreviewActivity.this.cvWrapper.getLayoutParams();
                layoutParams.width = i;
                layoutParams.height = measuredHeight;
                PreviewActivity.this.cvWrapper.setLayoutParams(layoutParams);
            }
        });
        imageSaved.setImageURI(this.imgUri);
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);
        AdAdmob adAdmob = new AdAdmob( this);
        adAdmob.FullscreenAd(this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.banner), this);

        sharedpreferences = getSharedPreferences(mypreference, MODE_PRIVATE);
        isActivityLeft = false;
        activity = PreviewActivity.this;

        connectionDetector = new ConnectionDetector(getApplicationContext());
        boolean isInternetPresent = connectionDetector.isConnectingToInternet();

        init();


    }

    public void isClickable() {

        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - this.mLastClickTime >= 3000) {
            this.mLastClickTime = currentTimeMillis;
        }
    }

    @OnClick({R.id.tb_edit})
    public void onEditClick() {
        isClickable();
        if (this.imgFile.delete()) {
            sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(this.imgFile)));
        }

        AppUtil.removeDraft(this.draft);
        this.isFromCreation = true;

        onBackPressed();
    }

    @OnClick({R.id.tb_close})
    public void onCloseClick() {
        isClickable();
        onBackPressed();
    }

    @OnClick({R.id.tb_home})
    public void onHomeClick() {
        whichActivitytoStart = 1;
        replaceScreen();
    }

    @OnClick({R.id.tb_trash})
    public void onTrashClick() {
        isClickable();
        final Dialog dialog = new Dialog(this, R.style.BottomDialog);
        View inflate = LayoutInflater.from(this).inflate(R.layout.wg_delete_alert_dialog, null);
        dialog.setCanceledOnTouchOutside(false);
        AppUtil.showBottomDialog(this, dialog, inflate, true);
        inflate.findViewById(R.id.btn_negative).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        inflate.findViewById(R.id.btn_positive).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (PreviewActivity.this.imgFile.delete()) {
                    PreviewActivity previewActivity = PreviewActivity.this;
                    previewActivity.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(previewActivity.imgFile)));
                }
                PreviewActivity.this.onBackPressed();
            }
        });
    }

    public void onBackPressed() {
        if (this.isFromCreation) {
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
            finish();
            return;

        }

        final Dialog dialog = new Dialog(this, R.style.BottomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.wg_bottom_alert_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);

        CardView btn_neutral = (CardView) dialog.findViewById(R.id.btn_neutral);
        CardView btn_negative = (CardView) dialog.findViewById(R.id.btn_negative);
        CardView btn_positive = (CardView) dialog.findViewById(R.id.btn_positive);
        TextView text_positive = (TextView) dialog.findViewById(R.id.text_positive);

        btn_neutral.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btn_negative.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();
                PreviewActivity.this.onHomeClick();
            }
        });

        text_positive.setText(getString(R.string.BTN_EDIT));
        btn_positive.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();
                PreviewActivity.this.onEditClick();
            }
        });

        dialog.show();
    }

    @OnClick({R.id.fab_instagram})
    public void onInstagramClick() {
        String sb = "Create by " +
                getString(R.string.app_name) +
                " " +
                "https://play.google.com/store/apps/details?id=" +
                getPackageName();
        AppUtil.shareIntent(this, "com.instagram.android", sb, this.imgUri);
    }

    @OnClick({R.id.fab_facebook})
    public void onFacebookClick() {
        String sb = "Create by " +
                getString(R.string.app_name) +
                " " +
                "https://play.google.com/store/apps/details?id=" +
                getPackageName();
        AppUtil.shareIntent(this, "com.facebook.katana", sb, this.imgUri);
    }

    @OnClick({R.id.fab_whatsapp})
    public void onWhatsappClick() {
        String sb = "Create by " +
                getString(R.string.app_name) +
                " " +
                "https://play.google.com/store/apps/details?id=" +
                getPackageName();
        AppUtil.shareIntent(this, "com.whatsapp", sb, this.imgUri);
    }

    @OnClick({R.id.fab_other})
    public void onOtherClick() {
        this.intent = new Intent();
        this.intent.setAction("android.intent.action.SEND");
        this.intent.putExtra("android.intent.extra.STREAM", this.imgUri);
        Intent intent2 = this.intent;
        String sb = "Create by https://play.google.com/store/apps/details?id=" +
                getPackageName();
        intent2.putExtra("android.intent.extra.TEXT", sb);
        this.intent.setType("image/jpeg");
        startActivity(Intent.createChooser(this.intent, getResources().getText(R.string.APD_SEND_TO)));
    }

    @OnClick({R.id.tv_rateus})
    public void onRateClick() {
        Context applicationContext = getApplicationContext();
        String sb = "https://play.google.com/store/apps/details?id=" +
                getPackageName();
        AppUtil.openUrl(applicationContext, sb);
    }

    @SuppressLint("WrongConstant")
    @OnClick({R.id.tv_setas})
    public void onSetAsClick() {
        this.intent = new Intent("android.intent.action.ATTACH_DATA");
        this.intent.addCategory("android.intent.category.DEFAULT");
        String str = "image/*";
        this.intent.setDataAndType(this.imgUri, str);
        this.intent.putExtra("mimeType", str);
        this.intent.addFlags(1);
        startActivityForResult(Intent.createChooser(this.intent, "Set As"), Callback.DEFAULT_DRAG_ANIMATION_DURATION);
    }


    public void onPause() {
        super.onPause();
        this.isActivityLeft = true;
    }

    public void onResume() {
        super.onResume();
        this.isActivityLeft = false;
    }

    protected void onStop() {
        super.onStop();
        this.isActivityLeft = true;
    }

    protected void onDestroy() {
        super.onDestroy();
        this.isActivityLeft = true;
    }

    @SuppressLint("WrongConstant")
    private void replaceScreen() {

        isClickable();
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);

    }
}

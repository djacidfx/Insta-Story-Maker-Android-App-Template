package com.example.storymaker.Activity;

import static android.os.Build.VERSION.SDK_INT;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.storymaker.R;
import com.example.storymaker.adapters.VpMainAdapter;
import com.example.storymaker.fragments.MyDraftsFrag;
import com.example.storymaker.fragments.MyStoriesFrag;
import com.example.storymaker.fragments.TemplatesDetailFrag;
import com.example.storymaker.fragments.TemplatesFrag;
import com.example.storymaker.help.ConnectionDetector;
import com.example.storymaker.utils.AnimationsUtil;
import com.example.storymaker.utils.AppUtil;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sharedpreferences;
    public static final String mypreference = "myprefadmob";
    ConnectionDetector connectionDetector;

    public static final int ITEM_PER_AD = 5;


    int whichActivitytoStart = 0;
    boolean isActivityLeft;

    AppCompatActivity activity;

    private FragmentManager fm;
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private SharedPreferences prefs;

    private Editor spEditor;
    private VpMainAdapter vpMainAdapter;

    ViewPager vpMain;

    String A;
    String B;
    String C;

    TabLayout tabLayout;
    private DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        AdAdmob adAdmob = new AdAdmob(this);
        adAdmob.FullscreenAd(this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.banner), this);

        sharedpreferences = getSharedPreferences(mypreference, MODE_PRIVATE);
        isActivityLeft = false;
        activity = MainActivity.this;
        connectionDetector = new ConnectionDetector(getApplicationContext());
        boolean isInternetPresent = connectionDetector.isConnectingToInternet();
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        this.fm = getSupportFragmentManager();
        AnimationsUtil.initAnimationsValue(this);
        this.prefs = getSharedPreferences("prefs", 0);
        this.spEditor = this.prefs.edit();
        vpMain = findViewById(R.id.vp_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(MainActivity.this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(activity, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        setTabBar();

        toolbar.setNavigationIcon(R.drawable.ic_menu_black);


    }


    private void setTabBar() {
        this.mFragments = new ArrayList<>();
        this.mFragments.add(TemplatesFrag.getInstance(this));
        this.mFragments.add(MyStoriesFrag.getInstance());
        this.mFragments.add(MyDraftsFrag.getInstance(this));

        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.TITLE_TEMPLATES)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.TITLE_MY_STORIES)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.TITLE_MY_DRAFTS)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        String str = "#000000";
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor(str));
        tabLayout.setTabTextColors(Color.parseColor("#9e9e9e"), Color.parseColor(str));

        this.vpMainAdapter = new VpMainAdapter(getSupportFragmentManager(), this.mFragments);
        this.vpMain.setAdapter(this.vpMainAdapter);
        this.vpMain.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpMain.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        this.vpMain.setCurrentItem(0);
        this.vpMain.setOffscreenPageLimit(3);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.sb_rate_us:
                sbRate();
                break;
            case R.id.sb_share:
                sbShare();
                break;
            case R.id.sb_privacy:
                sbPrivacy();
                break;

        }
        this.drawerLayout.closeDrawer((int) GravityCompat.START);
        return true;
    }

    public void selectTempCategory(String str) {
        addFragment(TemplatesDetailFrag.getInstance(this, str), R.id.fl_templates_detail, R.anim.slide_in_top, R.anim.slide_in_top);
        findViewById(R.id.fl_templates_detail).setVisibility(View.VISIBLE);
    }

    public void selectTemplate(String str, String str2) {

        loading(true);
        Intent intent = new Intent(MainActivity.this, EditorActivity.class);
        Log.e("category", "==>" + str);
        Log.e("hhhh", "==>" + str2);
        intent.putExtra("category", str);
        intent.putExtra("template", str2);
        intent.putExtra("draft", false);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

    }

    public void selectDraft(String str, String str2, String str3) {
        loading(true);
        if (str.toLowerCase().contains("card")) {
            Toasty.warning(this, "Sorry this new update does not support this template we will work to fix the issue soon, Thanks", 0).show();
            return;
        }
        if (SDK_INT >= 33) {
            if (AppUtil.permissionGranted(this, "android.permission.READ_MEDIA_IMAGES")) {
                Intent intent = new Intent(this, EditorActivity.class);
                intent.putExtra("category", str);
                intent.putExtra("template", str2);
                intent.putExtra("savePath", str3);
                intent.putExtra("draft", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        } else if (SDK_INT >= 30) {
            if (AppUtil.permissionGranted(this, "android.permission.READ_EXTERNAL_STORAGE") && AppUtil.permissionGranted(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                Intent intent = new Intent(this, EditorActivity.class);
                intent.putExtra("category", str);
                intent.putExtra("template", str2);
                intent.putExtra("savePath", str3);
                intent.putExtra("draft", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        } else {
            if (AppUtil.permissionGranted(this, "android.permission.READ_EXTERNAL_STORAGE") && AppUtil.permissionGranted(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                Intent intent = new Intent(this, EditorActivity.class);
                intent.putExtra("category", str);
                intent.putExtra("template", str2);
                intent.putExtra("savePath", str3);
                intent.putExtra("draft", true);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        }

    }


    public void swipeVP(int i) {
        this.vpMain.setCurrentItem(i);
    }

    private void addFragment(Fragment fragment, int i, int i2, int i3) {
        this.fm.executePendingTransactions();
        FragmentTransaction beginTransaction = this.fm.beginTransaction();
        if (!(i2 == 0 && i3 == 0)) {
            beginTransaction.setCustomAnimations(i2, i3);
        }
        beginTransaction.replace(i, fragment);
        beginTransaction.commitAllowingStateLoss();
    }

    public void onPermissionGranted(String str, String str2, String str3) {
        if (str3 == null) {
            A = str;
            B = str2;
            whichActivitytoStart = 1;
            replaceScreen();
        } else {
            A = str;
            B = str2;
            C = str3;
            whichActivitytoStart = 2;
            replaceScreen();
        }
    }

    public void loading(boolean z) {
        if (z) {
            findViewById(R.id.wg_loading).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.wg_loading).setVisibility(View.GONE);
        }
    }


    public void sbRate() {
        Intent in = new Intent(Intent.ACTION_VIEW);
        in.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
        startActivity(in);
    }

    public void sbShare() {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        String sb = getString(R.string.app_name) +
                " Free Application https://play.google.com/store/apps/details?id=" +
                getPackageName();
        intent.putExtra("android.intent.extra.TEXT", sb);
        startActivity(Intent.createChooser(intent, getResources().getText(R.string.APD_SEND_TO)));
    }



    public void sbPrivacy() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.PrivacyPolicy)));
        startActivity(intent);
    }


    public void onPause() {
        super.onPause();
        this.isActivityLeft = true;
    }

    public void onResume() {
        super.onResume();
        loading(false);
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

    private void replaceScreen() {
        if (whichActivitytoStart == 1) {
            selectTemplate(A, B);
        } else if (whichActivitytoStart == 2) {
            selectDraft(A, B, C);
        }
    }

    public void onBackPressed() {

        if (this.drawerLayout.isDrawerOpen((int) GravityCompat.START)) {
            this.drawerLayout.closeDrawer((int) GravityCompat.START);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.PositiveButtonStyle);
            alertDialogBuilder.setTitle("Exit");
            alertDialogBuilder
                    .setMessage("Do you really want to exit?")
                    .setCancelable(false)

                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finishAffinity();
                                }
                            })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
}

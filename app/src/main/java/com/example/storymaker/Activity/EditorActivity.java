package com.example.storymaker.Activity;

import static android.os.Build.VERSION.SDK_INT;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.Layout.Alignment;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Registry;
import com.example.storymaker.R;
import com.example.storymaker.adapters.RvColorsAdapter;
import com.example.storymaker.adapters.RvFontAdapter;
import com.example.storymaker.adapters.RvGradientAdapter;
import com.example.storymaker.fragments.FiltersFrag;
import com.example.storymaker.help.ConnectionDetector;
import com.example.storymaker.mediapicker.Gallery;
import com.example.storymaker.models.Draft;
import com.example.storymaker.models.Draft.Photo;
import com.example.storymaker.models.Draft.Text;
import com.example.storymaker.models.Font;
import com.example.storymaker.models.Glob_Sticker;
import com.example.storymaker.models.Template;
import com.example.storymaker.models.Template.Layout;
import com.example.storymaker.utils.AnimationsUtil;
import com.example.storymaker.utils.AppUtil;
import com.example.storymaker.utils.BitmapUtil;
import com.example.storymaker.utils.ContractsUtil;
import com.example.storymaker.utils.DensityUtil;
import com.example.storymaker.utils.FontProvider;
import com.example.storymaker.utils.OnOneOffClickListener;
import com.example.storymaker.utils.ScreenUtil;
import com.example.storymaker.widgets.ImageStickerView;
import com.example.storymaker.widgets.PhotoView;
import com.example.storymaker.widgets.TextStickerView;
import com.github.florent37.shapeofview.ShapeOfView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinner.OnItemSelectedListener;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devlight.io.library.ntb.NavigationTabBar;
import devlight.io.library.ntb.NavigationTabBar.Model;
import devlight.io.library.ntb.NavigationTabBar.Model.Builder;
import devlight.io.library.ntb.NavigationTabBar.OnTabBarSelectedIndexListener;
import es.dmoral.toasty.Toasty;

public class EditorActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;
    public static final String mypreference = "myprefadmob";

    ConnectionDetector connectionDetector;
    boolean isInternetPresent;


    int whichActivitytoStart = 0;
    boolean isActivityLeft;

    AppCompatActivity activity;

    File A;
    String B;

    protected static final Queue<Callable> actionQueue = new ConcurrentLinkedQueue();

    protected static final Handler handler = new Handler();

    protected static final Runnable runnable = new Runnable() {
        public void run() {
            Callable callable;
            do {
                callable = (Callable) EditorActivity.actionQueue.poll();
                if (callable != null) {
                    try {
                        callable.call();
                        continue;
                    } catch (Exception unused) {
                        continue;
                    }
                }
            } while (callable != null);
            EditorActivity.handler.postDelayed(this, 250);
        }
    };

    private ArrayList<PhotoView> addedPhotos = new ArrayList<>();
    private final ArrayList<ImageStickerView> allImageSticker = new ArrayList<>();
    private final ArrayList<View> allLayouts = new ArrayList<>();
    private final ArrayList<TextStickerView> allTextSticker = new ArrayList<>();

    @BindViews({R.id.sb_bg_scale, R.id.sb_bg_blur})
    List<IndicatorSeekBar> bgSeekBars;
    private int canvasHeight;
    private int canvasWidth;
    private int centreX;
    private int centreY;

    private ImageStickerView currentImgSticker;
    private TextStickerView currentTextSticker;

    private final String[] directionsMenu;
    private Draft draft;
    private String draftFolder;
    private String draftJson;

    private final ArrayList<String> draftPhotoIds = new ArrayList<>();
    private ArrayList<Text> draftTexts = new ArrayList<>();

    private String draftsPath;
    @BindView(R.id.et_text_editor)
    EditText etTextEditor;


    private final ArrayList<ViewGroup> fabControllers = new ArrayList<>();
    private boolean firstLaunch;
    @BindView(R.id.fl_img_sticker)
    FrameLayout flImageSticker;
    @BindView(R.id.fl_layout)
    FrameLayout flLayout;
    @BindView(R.id.fl_text_sticker)
    FrameLayout flTextSticker;
    @BindView(R.id.fl_wrapper)
    FrameLayout flWrapper;
    private FragmentManager fm;
    private ArrayList<String> fontCategories = new ArrayList<>();

    private FontProvider fontProvider;

    private final ArrayList<View> frameLayouts = new ArrayList<>();
    private final String gradientTile;

    private String gradientType;

    private String gradientTypeBg;

    private ImageStickerView imgSticker;

    private boolean isDraft;

    private boolean isSaved;
    @BindView(R.id.iv_background)
    ImageView ivBackground;

    private String linearDirection;
    private String linearDirectionBg;

    private long mLastClickTime;

    private final ArrayList<View> mediaMasks = new ArrayList<>();

    @BindView(R.id.sp_bg_gType)
    MaterialSpinner msgBgType;
    @BindView(R.id.sp_gType)
    MaterialSpinner msgType;
    @BindView(R.id.sp_pRepeat)
    MaterialSpinner mspRepeat;
    @BindView(R.id.sp_pTile)
    MaterialSpinner mspTile;
    @BindView(R.id.ntb_bg_editor)
    NavigationTabBar ntbBgEditor;
    @BindView(R.id.ntb_text_editor)
    NavigationTabBar ntbTextEditor;

    private String outputName;

    private RelativeLayout overlayClicked;
    private final LayoutParams params;

    private String patternPath;

    private final int patternRepeats;

    private final TileMode patternTile1;

    private PhotoView photoClicked;

    private int photoTag;
    private ArrayList<JSONObject> photos;

    private SharedPreferences prefs;
    @BindView(R.id.rl_container)
    RelativeLayout rlContainer;

    @BindView(R.id.rv_bg_color)
    RecyclerView rvBgColors;

    private RvGradientAdapter rvBgGradientAdapter;
    @BindView(R.id.rv_bg_gradients)
    RecyclerView rvBgGradients;
    private RvColorsAdapter rvColorAdapter;
    @BindView(R.id.rv_text_color)
    RecyclerView rvColors;

    private RvFontAdapter rvFontAdapter;
    @BindView(R.id.rv_font_detail)
    RecyclerView rvFontDetail;

    private RvGradientAdapter rvGradientAdapter;

    @BindView(R.id.rv_gradients)
    RecyclerView rvGradients;
    @BindView(R.id.rv_text_pattern)
    RecyclerView rvPatterns;
    @BindView(R.id.rv_text_pattern_menu)
    RecyclerView rvPatternsMenu;

    private int saveHeight;
    private String savePath;
    private int saveWidth;
    private int screenHeight;
    private int screenWidth;
    private String selectedBgColor;
    private String[] selectedBgGradient;
    private String selectedBgPattern;
    private int selectedBtn;

    private ArrayList<Integer> selectedViewRatio = new ArrayList<>();

    @BindViews({R.id.sb_text_font_size, R.id.sb_text_opacity, R.id.sb_text_width_size, R.id.sb_text_height_size, R.id.sb_text_padding_left, R.id.sb_text_padding_right})
    List<IndicatorSeekBar> teSeekBars;

    private Template template;

    private String templateCategory;
    private String templateJson;
    private String templateName;

    private ViewGroup templateViewGroup;
    private ArrayList<Template.Text> templeteTexts = new ArrayList<>();

    private TextStickerView textSticker;

    private final String[] tilesMenu;
    @BindView(R.id.tl_font_categories)
    TabLayout tlFontCategories;

    private int totalMemory;

    private TextStickerView touchTextSticker;

    private boolean txBtnClicked;
    @BindView(R.id.v_background)
    View vBgColor;
    @BindViews({R.id.wg_main_menu, R.id.wg_text_edit})
    List<View> vWidgets;

    @BindView(R.id.wg_text_edit)
    View wgTextEditor;

    Alignment alignment;
    private Drawable d;

    final String[] colorList = new String[]{"#ffffff", "#d9d9d9", "#c4c4c4", "#9d9d9d", "#7b7b7b", "#555555", "#434343", "#262626", "#e1bee7", "#ce93d8", "#ba68c8", "#ab47bc", "#9c27b0", "#8e24aa", "#7b1fa2", "#6a1b9a", "#4a148c", "#d1c4e9", "#b39ddb", "#9575cd", "#7e57c2", "#673ab7", "#5e35b1", "#512da8", "#4527a0", "#311b92", "#c5cae9", "#9fa8da", "#7986cb", "#5c6ac0", "#3f50b5", "#3948ab", "#303e9f", "#283493", "#1a227e", "#bbdefb", "#91cbf9", "#65b6f6", "#43a6f5", "#2397f3", "#1f89e5", "#1a77d2", "#1666c0", "#0d48a1", "#b3e5fc", "#82d5fa", "#50c4f7", "#2bb7f6", "#08aaf4", "#069ce5", "#0389d1", "#0378bd", "#01589b", "#b2ebf2", "#80dfea", "#4dd1e1", "#26c7da", "#00bdd4", "#00adc1", "#0098a7", "#00848f", "#006164", "#b2dfdb", "#80cbc4", "#4db6ac", "#26a69a", "#009688", "#00897b", "#00796b", "#00695c", "#004d40", "#c8e6c9", "#a5d6a7", "#81c784", "#66bb6a", "#4caf4f", "#43a046", "#388e3b", "#2e7d31", "#1b5e1f", "#dcedc8", "#c5e1a5", "#aed581", "#9ccc65", "#8bc34a", "#7cb342", "#689f38", "#558b2f", "#33691e", "#f0f4c3", "#e6ee9c", "#dce775", "#d4e157", "#cddc39", "#c0ca33", "#afb42b", "#9e9d24", "#827717", "#fff9c4", "#fff59d", "#fddb00", "#fceb55", "#fae635", "#fdd835", "#fbc02d", "#f9a825", "#f57f17", "#ffecb3", "#ffe082", "#ffd54f", "#ffca28", "#ffc106", "#ffb300", "#ffa000", "#ff8f00", "#ff6f00", "#ffe0b2", "#ffcc80", "#ffb74d", "#ffa726", "#ff9800", "#fb8c00", "#f57c00", "#ef6c00", "#e65100", "#ffccbc", "#ffab91", "#ff8a65", "#ff7043", "#ff5722", "#f4511e", "#e64a19", "#d84315", "#bf360c", "#ffccbc", "#ffab91", "#ff8a65", "#ff7043", "#ff5722", "#f4511e", "#e64a19", "#d84315", "#bf360c", "#ffcdd2", "#ef9a9a", "#e57373", "#ef5350", "#f44336", "#e53935", "#d32f2f", "#c62828", "#b71c1c", "#f8bbd0", "#f48fb1", "#f06292", "#ec407a", "#e91e63", "#d81b60", "#c2185b", "#ad1457", "#880e4f", "#d7ccc8", "#bcaaa4", "#a1887f", "#8d6e63", "#795548", "#6d4c41", "#5d4037", "#4e342e", "#3e2723", "#cfd8dc", "#b0bec5", "#90a4ae", "#78909c", "#607d8b", "#546e7a", "#455a64", "#37474f", "#263238"};

    static class AnonymousClass67 {
        static final int[] SwitchMapandroidtextLayoutAlignment = new int[Alignment.values().length];


        static {
            SwitchMapandroidtextLayoutAlignment[Alignment.ALIGN_NORMAL.ordinal()] = 1;
            SwitchMapandroidtextLayoutAlignment[Alignment.ALIGN_CENTER.ordinal()] = 2;
            SwitchMapandroidtextLayoutAlignment[Alignment.ALIGN_OPPOSITE.ordinal()] = 3;
        }
    }


    @BindView(R.id.iv_align_left)
    ImageView iv_align_left;
    @BindView(R.id.iv_align_center)
    ImageView iv_align_center;
    @BindView(R.id.iv_align_right)
    ImageView iv_align_right;

    @BindView(R.id.iv_text_underline)
    ImageView iv_text_underline;
    @BindView(R.id.iv_text_strikethrough)
    ImageView iv_text_strikethrough;

    public class DragListener implements OnDragListener {

        public DragListener() {
        }

        public boolean onDrag(View view, DragEvent dragEvent) {
            View view2 = (View) dragEvent.getLocalState();
            int action = dragEvent.getAction();
            if (action != 1) {
                if (action == 3) {
                    ViewGroup viewGroup = (ViewGroup) view2.getParent().getParent();
                    FrameLayout frameLayout = (FrameLayout) viewGroup.getChildAt(0);
                    View childAt = viewGroup.getChildAt(1);
                    viewGroup.removeAllViews();
                    FrameLayout frameLayout2 = (FrameLayout) view;
                    ViewGroup viewGroup2 = (ViewGroup) frameLayout2.getParent();
                    if (viewGroup2 != null) {
                        View childAt2 = viewGroup2.getChildAt(1);
                        viewGroup2.removeAllViews();
                        viewGroup2.addView(frameLayout);
                        viewGroup2.addView(childAt);
                        viewGroup.addView(frameLayout2);
                        viewGroup.addView(childAt2);
                    } else {
                        viewGroup.addView(frameLayout);
                        viewGroup.addView(childAt);
                    }
                    view2.setVisibility(View.VISIBLE);
                    EditorActivity.this.addedPhotos = new ArrayList();
                    EditorActivity.this.photoTag = 0;
                    setMediaOptions(templateViewGroup, true);
                    updateMediaOrder(templateViewGroup);

                } else if (action == 5) {
                    view2.setVisibility(View.VISIBLE);
                }
            }
            return true;
        }
    }

    private class SaveDraft extends AsyncTask<String, String, JSONObject> {

        private Bitmap btmDraw;
        private String coverName;
        private final boolean isPhoto;
        private final String traceName;
        private final int videoQuality;

        public SaveDraft(int i, boolean z, String str) {
            this.videoQuality = i;
            this.isPhoto = z;
            this.traceName = str;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            setCurrentTextStickerEdit(false, currentTextSticker);
            setCurrentImgStickerEdit(false, currentImgSticker);
            Iterator it = EditorActivity.this.fabControllers.iterator();
            while (it.hasNext()) {
                ((ViewGroup) it.next()).setVisibility(View.INVISIBLE);
            }
            EditorActivity.this.loading(true, this.isPhoto);
            EditorActivity.this.flWrapper.setDrawingCacheEnabled(true);
            EditorActivity.this.flWrapper.buildDrawingCache(true);
            String sb = EditorActivity.this.templateName +
                    "-thumbnail-" +
                    AppUtil.getCurrentTime() +
                    ".png";
            this.coverName = sb;
            Bitmap drawingCache = EditorActivity.this.flWrapper.getDrawingCache();
            String str = "/";
            String sb2 = EditorActivity.this.draftsPath +
                    str +
                    EditorActivity.this.draftFolder +
                    str;
            BitmapUtil.savePhoto(drawingCache, sb2, this.coverName, 600, 1066, true);
            if (EditorActivity.this.isDraft) {
                File file = new File(EditorActivity.this.draft.thumbnail);
                if (file.exists()) {
                    file.delete();
                }
                File file2 = new File(EditorActivity.this.draft.save_path);
                if (file2.exists()) {
                    file2.delete();
                }
            }
            if (EditorActivity.this.isSaved) {
                this.btmDraw = BitmapUtil.createScaledBitmap(EditorActivity.this.flWrapper.getDrawingCache(), EditorActivity.this.saveWidth, EditorActivity.this.saveHeight, true);
            }
        }

        protected JSONObject doInBackground(String... strArr) {
            String str;
            String str2;
            String str3;
            String str4;
            String str5;
            String str6;
            String str7;
            String str8 = "gradient_type";
            String str9 = "/";
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("draft_name", (Object) EditorActivity.this.draftFolder);
                String sb = EditorActivity.this.draftsPath +
                        str9 +
                        EditorActivity.this.draftFolder +
                        str9 +
                        this.coverName;
                jSONObject.put("thumbnail", (Object) sb);
                jSONObject.put("template_name", (Object) EditorActivity.this.templateName);
                jSONObject.put("template_category", (Object) EditorActivity.this.templateCategory);
                jSONObject.put("background_color", (Object) EditorActivity.this.selectedBgColor);
                jSONObject.put("background_gradient", (Object) EditorActivity.this.selectedBgGradient);
                jSONObject.put("gradient_linear_direction", (Object) EditorActivity.this.linearDirectionBg);
                jSONObject.put(str8, (Object) EditorActivity.this.gradientTypeBg);
                jSONObject.put("background_photo", (Object) EditorActivity.this.selectedBgPattern);
                jSONObject.put("photo_scale", (Object) EditorActivity.this.ivBackground.getScaleX());
                jSONObject.put("photo_blur", (Object) ((IndicatorSeekBar) EditorActivity.this.bgSeekBars.get(1)).getProgress());
                jSONObject.put("saved", (Object) EditorActivity.this.isSaved);
                ArrayList arrayList = new ArrayList();
                Iterator it = EditorActivity.this.allTextSticker.iterator();
                while (true) {
                    str = "rotate";
                    str2 = "layout_y";
                    str3 = "layout_x";
                    str4 = "scale";
                    str5 = "id";
                    if (!it.hasNext()) {
                        break;
                    }
                    TextStickerView textStickerView = (TextStickerView) it.next();
                    JSONObject jSONObject2 = new JSONObject();
                    jSONObject2.put(str5, textStickerView.getTag());
                    jSONObject2.put(str3, (Object) textStickerView.getLayoutX());
                    jSONObject2.put(str2, (Object) textStickerView.getLayoutY());
                    jSONObject2.put(str, (Object) textStickerView.getRotateAngle());
                    jSONObject2.put(str4, (Object) textStickerView.getScale());
                    jSONObject2.put("paddingLeft", (Object) textStickerView.getPaddingLeft());
                    jSONObject2.put("paddingRight", (Object) textStickerView.getPaddingRight());
                    jSONObject2.put("opacity", (Object) textStickerView.getOpacity());
                    jSONObject2.put("text", (Object) textStickerView.getText());
                    jSONObject2.put("size", (Object) textStickerView.getFont().getSize());
                    jSONObject2.put("color", (Object) textStickerView.getFont().getColor());
                    jSONObject2.put("font_category", (Object) textStickerView.getFont().getCategory());
                    jSONObject2.put("font_name", (Object) textStickerView.getFont().getTypeface());
                    jSONObject2.put("letter_spacing", (Object) textStickerView.getLetterSpacing());
                    jSONObject2.put("line_spacing", (Object) textStickerView.getLineSpacing());
                    jSONObject2.put("underline", (Object) textStickerView.isUnderLine());
                    jSONObject2.put("strikethrough", (Object) textStickerView.isStrikethrough());
                    jSONObject2.put("align", (Object) textStickerView.getAlign());
                    jSONObject2.put("gradient", (Object) AppUtil.strArrayToStr(textStickerView.getFont().getGradient(), " "));
                    jSONObject2.put(str8, (Object) textStickerView.getFont().getGradientType());
                    jSONObject2.put("linear_direction", (Object) textStickerView.getFont().getLinearDirection());
                    jSONObject2.put("pattern_path", (Object) textStickerView.getFont().getPatternPath());
                    jSONObject2.put("pattern_mode", (Object) textStickerView.getFont().getPatternMode());
                    jSONObject2.put("pattern_repeats", (Object) textStickerView.getFont().getPatternRepeats());
                    arrayList.add(jSONObject2);
                }
                jSONObject.put("texts", (Object) arrayList);
                ArrayList arrayList2 = new ArrayList();
                Iterator it2 = EditorActivity.this.allImageSticker.iterator();
                while (true) {
                    str6 = "path";
                    if (!it2.hasNext()) {
                        break;
                    }
                    ImageStickerView imageStickerView = (ImageStickerView) it2.next();
                    imageStickerView.calculate();
                    JSONObject jSONObject3 = new JSONObject();
                    jSONObject3.put(str5, (Object) imageStickerView.getStickerId());
                    jSONObject3.put(str6, (Object) imageStickerView.getStickerPath());
                    jSONObject3.put(str3, (Object) imageStickerView.getLayoutX());
                    jSONObject3.put(str2, (Object) imageStickerView.getLayoutY());
                    jSONObject3.put(str4, (Object) imageStickerView.getScale());
                    jSONObject3.put(str, (Object) imageStickerView.getRotate());
                    arrayList2.add(jSONObject3);
                }
                jSONObject.put("stickers", (Object) arrayList2);
                ArrayList arrayList3 = new ArrayList();
                Iterator it3 = EditorActivity.this.addedPhotos.iterator();
                while (it3.hasNext()) {
                    PhotoView photoView = (PhotoView) it3.next();
                    JSONObject jSONObject4 = new JSONObject();
                    jSONObject4.put(str5, photoView.getTag());
                    jSONObject4.put(str4, (Object) photoView.getScale());
                    jSONObject4.put(str6, (Object) photoView.getPhotoPath());
                    arrayList3.add(jSONObject4);
                }
                jSONObject.put("photos", (Object) arrayList3);
                try {
                    String sb3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() +
                            "/Android/data/" +
                            EditorActivity.this.getPackageName() +
                            "/drafts/Json/";
                    if (!new File(sb3).exists()) {
                        new File(sb3).mkdirs();
                    }
                    if (EditorActivity.this.isDraft) {
                        str7 = EditorActivity.this.draft.save_path.replace(sb3, "");
                    } else {
                        String sb4 = EditorActivity.this.templateName +
                                "-" +
                                AppUtil.getCurrentTime() +
                                ".json";
                        str7 = sb4;
                    }
                    String sb5 = sb3 +
                            str7;
                    jSONObject.put("save_path", (Object) sb5);
                    File file = new File(sb3, str7);

                    try {
                        FileWriter fileWriter = new FileWriter(file);
                        try {
                            fileWriter.write(jSONObject.toJSONString());
                        } finally {
                            fileWriter.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    EditorActivity.this.draftJson = AppUtil.inputStreamToString(new FileInputStream(file));
                    Gson gson = new Gson();
                    String sb6 = sb3 +
                            str7;
                    draft = (Draft) gson.fromJson(AppUtil.inputStreamToString(new FileInputStream(new File(sb6))), Draft.class);
                    EditorActivity.this.removeUnusedFiles();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (EditorActivity.this.isSaved) {
                    EditorActivity.this.imageProcessing(this.btmDraw);
                }
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            return jSONObject;
        }

        protected void onPostExecute(JSONObject jSONObject) {
            super.onPostExecute(jSONObject);
            if (EditorActivity.this.isSaved) {
                EditorActivity.this.flWrapper.setDrawingCacheEnabled(false);
                String sb = EditorActivity.this.savePath +
                        "/" +
                        EditorActivity.this.outputName;
                File file = new File(sb);
                if (file.exists()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("title", EditorActivity.this.outputName);
                    String sb2 = EditorActivity.this.getString(R.string.app_name) +
                            " Application Android";
                    contentValues.put("description", sb2);
                    contentValues.put("datetaken", System.currentTimeMillis());
                    contentValues.put("bucket_id", file.toString().toLowerCase(Locale.US).hashCode());
                    contentValues.put("bucket_display_name", file.getName().toLowerCase(Locale.US));
                    contentValues.put("_data", file.getAbsolutePath());
                    EditorActivity.this.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
                    whichActivitytoStart = 1;
                    A = file;
                    B = EditorActivity.this.draftJson;
                    replaceScreen();
                } else {
                    Toasty.error(EditorActivity.this, getResources().getString(R.string.MSG_SOMETHING_WRONG), 0, true).show();
                }
                EditorActivity.this.loading(false, true);
                return;
            }
            EditorActivity.this.loading(false, this.isPhoto);
            EditorActivity.this.finish();
            EditorActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    private class SavePhoto extends AsyncTask<String, String, File> {
        private Bitmap btmDraw;
        private final int height;
        private final String photoName;
        private final String savePath;
        private final boolean showLoading;
        private final int width;

        public SavePhoto(String str, String str2, int i, int i2, boolean z) {
            this.savePath = str;
            this.photoName = str2;
            this.width = i;
            this.height = i2;
            this.showLoading = z;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            setCurrentTextStickerEdit(false, currentTextSticker);
            setCurrentImgStickerEdit(false, currentImgSticker);
            Log.d("@@@@@@@@@@@", "onPreExecute: "+currentImgSticker);
            EditorActivity.this.flWrapper.setDrawingCacheEnabled(true);
            EditorActivity.this.flWrapper.buildDrawingCache(true);
            this.btmDraw = BitmapUtil.createScaledBitmap(EditorActivity.this.flWrapper.getDrawingCache(), this.width, this.height, true);
        }

        protected File doInBackground(String... strArr) {
            File file = new File(this.savePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file, this.photoName);
            if (file2.exists()) {
                file2.delete();
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file2);
                this.btmDraw.compress(CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.showLoading) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("title", this.photoName);
                String sb = EditorActivity.this.getString(R.string.app_name) +
                        " Application Android";
                contentValues.put("description", sb);
                contentValues.put("datetaken", System.currentTimeMillis());
                contentValues.put("bucket_id", file2.toString().toLowerCase(Locale.US).hashCode());
                contentValues.put("bucket_display_name", file2.getName().toLowerCase(Locale.US));
                contentValues.put("_data", file2.getAbsolutePath());
                EditorActivity.this.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, contentValues);
            }
            return file2;
        }

        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (this.showLoading) {
                EditorActivity.this.loading(false, true);
                if (file != null) {
                    EditorActivity.this.flWrapper.setDrawingCacheEnabled(false);
                    Intent intent = new Intent(EditorActivity.this, PreviewActivity.class);
                    intent.putExtra("savedImageFile", file);
                    intent.putExtra("draft", EditorActivity.this.draftJson);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    EditorActivity.this.startActivity(intent);
                    EditorActivity.this.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                }
            }
        }
    }

    public EditorActivity() {
        String str = "Linear";
        this.directionsMenu = new String[]{str, "Radial", "Sweep"};
        String str2 = "Clamp";
        this.tilesMenu = new String[]{str2, "Mirror", "Repeat"};
        this.selectedBgGradient = null;
        this.photoTag = 0;
        this.patternRepeats = 2;
        this.gradientTile = str2;
        this.gradientType = str;
        String str3 = "Horizontal";
        this.linearDirection = str3;
        this.gradientTypeBg = str;
        this.linearDirectionBg = str3;
        this.firstLaunch = true;
        this.patternTile1 = TileMode.MIRROR;
        this.params = new LayoutParams(-1, -1);
    }

    private void init() {
        AnimationsUtil.initAnimationsValue(this);
        this.fm = getSupportFragmentManager();
        this.totalMemory = AppUtil.getTotalMemory(this);
        this.prefs = getSharedPreferences("Data Holder", 0);
        this.screenWidth = ScreenUtil.getScreenWidth(this);
        this.screenHeight = ScreenUtil.getScreenHeight(this);
        this.fontProvider = new FontProvider(this, getResources());
        this.isSaved = getIntent().getBooleanExtra("IsDraft", false);
        this.isDraft = getIntent().getBooleanExtra("draft", false);

        String str = "";
        String sb = Environment.DIRECTORY_PICTURES +
                "/" +
                getString(R.string.app_name).replace(" ", str);
        this.savePath = Environment.getExternalStoragePublicDirectory(sb).getAbsolutePath();

        String sb2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() +
                "/Android/data/" +
                getPackageName() +
                "/drafts";
        this.draftsPath = sb2;

        this.flWrapper.post(new Runnable() {
            public void run() {
                canvasHeight = flWrapper.getMeasuredHeight();
                double canvasHeight1 = (double) canvasHeight;
                Double.isNaN(canvasHeight1);
                canvasWidth = (int) (canvasHeight1 * 0.5625d);
                if (EditorActivity.this.canvasWidth >= EditorActivity.this.screenWidth) {
                    canvasWidth = (screenWidth * 90) / 100;
                    double canvasWidth1 = (double) canvasWidth;
                    Double.isNaN(canvasWidth1);
                    canvasHeight = (int) (canvasWidth1 * 1.7777777777777777d);
                }
                ViewGroup.LayoutParams layoutParams = EditorActivity.this.flWrapper.getLayoutParams();
                layoutParams.width = EditorActivity.this.canvasWidth;
                layoutParams.height = EditorActivity.this.canvasHeight;
                EditorActivity.this.flWrapper.setLayoutParams(layoutParams);
                centreX = (int) (flWrapper.getX() + ((float) (EditorActivity.this.flWrapper.getWidth() / 2)));
                centreY = (int) (flWrapper.getY() + ((float) (EditorActivity.this.flWrapper.getHeight() / 2)));
                if (EditorActivity.this.isDraft) {
                    EditorActivity.this.setupDraft();
                } else {
                    EditorActivity.this.setupTemplate();
                    String sb = "draft-" +
                            AppUtil.getCurrentTime();
                    draftFolder = sb;
                }
                String str = "/";
                String sb2 = EditorActivity.this.draftsPath +
                        str +
                        EditorActivity.this.draftFolder +
                        str;
                File file = new File(sb2);
                if (!file.exists()) {
                    file.mkdirs();
                }
                EditorActivity.this.initTextEntitiesListeners();
                EditorActivity.this.setBackgroundListeners();
            }
        });
        this.mLastClickTime = System.currentTimeMillis();
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        AdAdmob adAdmob = new AdAdmob(this);
        adAdmob.FullscreenAd(this);
        adAdmob.BannerAd((RelativeLayout) findViewById(R.id.banner), this);

        sharedpreferences = getSharedPreferences(mypreference, MODE_PRIVATE);
        isActivityLeft = false;
        activity = EditorActivity.this;

        connectionDetector = new ConnectionDetector(getApplicationContext());
        isInternetPresent = connectionDetector.isConnectingToInternet();


        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        addTemplate();
        init();


    }

    public void addTemplate() {
        this.templateCategory = getIntent().getStringExtra("category");
        this.templateName = getIntent().getStringExtra("template");

        Log.e("templateCategory", "==>" + templateCategory);
        Log.e("templateName", "==>" + ContractsUtil.initTemplates(templateCategory).get(templateName));

        this.flLayout.removeAllViewsInLayout();
        this.templateViewGroup = (ViewGroup) LayoutInflater.from(this).inflate(ContractsUtil.initTemplates(templateCategory).get(templateName), this.flLayout, false);
        this.flLayout.addView(this.templateViewGroup);
        this.flLayout.setBackgroundColor(0);
        try {
            AssetManager assets = getAssets();
            StringBuilder sb = new StringBuilder();
            sb.append("Templates/");
            sb.append(this.templateName);
            sb.append(".json");
            Log.e("templateJson", "==>" + sb);
            this.templateJson = AppUtil.inputStreamToString(assets.open(sb.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < this.templateViewGroup.getChildCount(); i++) {
            ViewGroup viewGroup = (ViewGroup) this.templateViewGroup.getChildAt(i);
            this.allLayouts.add(viewGroup);
            if (viewGroup.getChildAt(0) instanceof ShapeOfView) {
                ShapeOfView shapeOfView = (ShapeOfView) viewGroup.getChildAt(0);
                if ((shapeOfView.getChildAt(0) instanceof FrameLayout) && (((FrameLayout) shapeOfView.getChildAt(0)).getChildAt(0) instanceof PhotoView)) {
                    this.frameLayouts.add(viewGroup);
                }
            }
        }

        this.template = (Template) new Gson().fromJson(this.templateJson, Template.class);
    }

    public void setupTemplate() {

        int j;
        String str = " ";
        int textCenterY;
        int i = 0;
        char c;
        boolean z;

        setMediaOptions(templateViewGroup, false);
        templeteTexts = template.texts;

        while (i < templeteTexts.size()) {

            Font font = new Font();
            font.setColor(Color.parseColor(((Template.Text) templeteTexts.get(i)).color));
            font.setSize(((Template.Text) templeteTexts.get(i)).size);
            font.setCategory(((Template.Text) templeteTexts.get(i)).font_category);
            font.setTypeface(((Template.Text) templeteTexts.get(i)).font_name);
            font.setGradient(AppUtil.strTOStrArray(((Template.Text) templeteTexts.get(i)).gradient, str));
            font.setGradientType(((Template.Text) templeteTexts.get(i)).gradient_type);
            font.setLinearDirection(((Template.Text) templeteTexts.get(i)).linear_direction);
            font.setPatternPath(((Template.Text) templeteTexts.get(i)).pattern_path);
            font.setPatternMode(((Template.Text) templeteTexts.get(i)).pattern_mode);
            font.setPatternRepeats(((Template.Text) templeteTexts.get(i)).pattern_repeats);

            alignment = Alignment.ALIGN_CENTER;
            String str2 = ((Template.Text) templeteTexts.get(i)).align;

            if (str2.contains("right")) {

                c = 2;
                if (c != 0) {
                    alignment = Alignment.ALIGN_NORMAL;
                } else if (c == 1) {
                    alignment = Alignment.ALIGN_CENTER;
                } else if (c == 2) {
                    alignment = Alignment.ALIGN_OPPOSITE;
                }

                j = ((Template.Text) templeteTexts.get(i)).layout_id;
                textSticker = new TextStickerView(EditorActivity.this, canvasWidth, canvasHeight, fontProvider);
                if (((Template.Text) templeteTexts.get(i)).position.bottom != null) {
                    TextStickerView textStickerView = (TextStickerView) allTextSticker.get(Integer.parseInt(((Template.Text) templeteTexts.get(i)).position.bottom));
                    textCenterY = (int) (((textStickerView.getTextCenterY() + ((float) ((textStickerView.getTextHeight() * 70) / 100))) + ((float) DensityUtil.dp2px(EditorActivity.this, (float) ((Template.Text) templeteTexts.get(i)).margin_top))) - ((float) DensityUtil.dp2px(EditorActivity.this, (float) ((Template.Text) templeteTexts.get(i)).margin_bottom)));
                } else {
                    textCenterY = (int) ((((float) canvasHeight) * ((Template.Text) templeteTexts.get(i)).layout_y) / 100.0f);
                }

                createTextStickView(((Template.Text) templeteTexts.get(i)).id,
                        ((Template.Text) templeteTexts.get(i)).text,
                        font,
                        (int) ((((float) canvasWidth) * ((Template.Text) templeteTexts.get(i)).layout_x) / 100.0f),
                        textCenterY,
                        ((Template.Text) templeteTexts.get(i)).rotate,
                        ((Template.Text) templeteTexts.get(i)).scale,
                        ((Template.Text) templeteTexts.get(i)).padding_left,
                        ((Template.Text) templeteTexts.get(i)).padding_right,
                        alignment,
                        ((Template.Text) templeteTexts.get(i)).opacity,
                        ((Template.Text) templeteTexts.get(i)).underLine,
                        ((Template.Text) templeteTexts.get(i)).strikethrough,
                        (float) ((Template.Text) templeteTexts.get(i)).letter_spacing,
                        (float) ((Template.Text) templeteTexts.get(i)).line_spacing);

            } else if (str2.contains("center")) {
                c = 1;
                if (c != 0) {
                }
                j = ((Template.Text) templeteTexts.get(i)).layout_id;
                textSticker = new TextStickerView(EditorActivity.this, canvasWidth, canvasHeight, fontProvider);
                if (((Template.Text) templeteTexts.get(i)).position.bottom != null) {
                    TextStickerView textStickerView = (TextStickerView) allTextSticker.get(Integer.parseInt(((Template.Text) templeteTexts.get(i)).position.bottom));
                    textCenterY = (int) (((textStickerView.getTextCenterY() + ((float) ((textStickerView.getTextHeight() * 70) / 100))) + ((float) DensityUtil.dp2px(EditorActivity.this, (float) ((Template.Text) templeteTexts.get(i)).margin_top))) - ((float) DensityUtil.dp2px(EditorActivity.this, (float) ((Template.Text) templeteTexts.get(i)).margin_bottom)));
                } else {

                    textCenterY = (int) ((((float) canvasHeight) * ((Template.Text) templeteTexts.get(i)).layout_y) / 100.0f);

                }

                createTextStickView(((Template.Text) templeteTexts.get(i)).id,
                        ((Template.Text) templeteTexts.get(i)).text,
                        font,
                        (int) ((((float) canvasWidth) * ((Template.Text) templeteTexts.get(i)).layout_x) / 100.0f),
                        textCenterY,
                        ((Template.Text) templeteTexts.get(i)).rotate,
                        ((Template.Text) templeteTexts.get(i)).scale,
                        ((Template.Text) templeteTexts.get(i)).padding_left,
                        ((Template.Text) templeteTexts.get(i)).padding_right,
                        alignment,
                        ((Template.Text) templeteTexts.get(i)).opacity,
                        ((Template.Text) templeteTexts.get(i)).underLine,
                        ((Template.Text) templeteTexts.get(i)).strikethrough,
                        (float) ((Template.Text) templeteTexts.get(i)).letter_spacing,
                        (float) ((Template.Text) templeteTexts.get(i)).line_spacing);
            } else if (str2.contains("left")) {

                c = 0;

                if (c != 0) {
                }
                j = ((Template.Text) templeteTexts.get(i)).layout_id;
                textSticker = new TextStickerView(EditorActivity.this, canvasWidth, canvasHeight, fontProvider);
                if (((Template.Text) templeteTexts.get(i)).position.bottom != null) {
                    TextStickerView textStickerView = (TextStickerView) allTextSticker.get(Integer.parseInt(((Template.Text) templeteTexts.get(i)).position.bottom));
                    textCenterY = (int) (((textStickerView.getTextCenterY() + ((float) ((textStickerView.getTextHeight() * 70) / 100))) + ((float) DensityUtil.dp2px(EditorActivity.this, (float) ((Template.Text) templeteTexts.get(i)).margin_top))) - ((float) DensityUtil.dp2px(EditorActivity.this, (float) ((Template.Text) templeteTexts.get(i)).margin_bottom)));
                } else {
                    textCenterY = (int) ((((float) canvasHeight) * ((Template.Text) templeteTexts.get(i)).layout_y) / 100.0f);
                }

                createTextStickView(((Template.Text) templeteTexts.get(i)).id,
                        ((Template.Text) templeteTexts.get(i)).text,
                        font,
                        (int) ((((float) canvasWidth) * ((Template.Text) templeteTexts.get(i)).layout_x) / 100.0f),
                        textCenterY,
                        ((Template.Text) templeteTexts.get(i)).rotate,
                        ((Template.Text) templeteTexts.get(i)).scale,
                        ((Template.Text) templeteTexts.get(i)).padding_left,
                        ((Template.Text) templeteTexts.get(i)).padding_right,
                        alignment,
                        ((Template.Text) templeteTexts.get(i)).opacity,
                        ((Template.Text) templeteTexts.get(i)).underLine,
                        ((Template.Text) templeteTexts.get(i)).strikethrough,
                        (float) ((Template.Text) templeteTexts.get(i)).letter_spacing,
                        (float) ((Template.Text) templeteTexts.get(i)).line_spacing);
            }
            i += 1;
        }

        selectedBgColor = template.background_color;
        selectedBgGradient = template.background_gradient;

        if (selectedBgColor != null) {
            ivBackground.setVisibility(View.GONE);
            vBgColor.setBackgroundColor(Color.parseColor(selectedBgColor));
        } else if (selectedBgGradient != null) {
            ivBackground.setVisibility(View.GONE);
            gradientTypeBg = template.gradient_type;
            linearDirectionBg = template.gradient_linear_direction;
            changeBackground(null, selectedBgGradient, null);
        } else {
            z = false;
            findViewById(R.id.menu_background).setVisibility(View.GONE);
            setRoundedRect();
            firstLaunch = z;
        }
        z = false;
        setRoundedRect();
        firstLaunch = z;
    }

    public void setupDraft() {

        boolean z;

        try {
            draft = (Draft) new Gson().fromJson(AppUtil.inputStreamToString(new FileInputStream(new File(getIntent().getStringExtra("savePath")))), Draft.class);
            Iterator it = draft.photos.iterator();
            while (it.hasNext()) {

                draftPhotoIds.add(((Photo) it.next()).id);

            }
            setMediaOptions(templateViewGroup, false);
            draftFolder = draft.draft_name;
            draftTexts = draft.texts;
            if (draftTexts != null) {
                int i = 0;
                while (i < draftTexts.size()) {
                    try {
                        Font font = new Font();
                        font.setColor(((Text) draftTexts.get(i)).color);
                        font.setSize(((Text) draftTexts.get(i)).size);
                        font.setCategory(((Text) draftTexts.get(i)).font_category);
                        font.setTypeface(((Text) draftTexts.get(i)).font_name);
                        font.setGradient(AppUtil.strTOStrArray(((Text) draftTexts.get(i)).gradient, " "));
                        font.setGradientType(((Text) draftTexts.get(i)).gradient_type);
                        font.setLinearDirection(((Text) draftTexts.get(i)).linear_direction);
                        font.setPatternPath(((Text) draftTexts.get(i)).pattern_path);
                        font.setPatternMode(((Text) draftTexts.get(i)).pattern_mode);
                        font.setPatternRepeats(((Text) draftTexts.get(i)).pattern_repeats);
                        Alignment alignment = Alignment.ALIGN_CENTER;
                        String str = ((Text) draftTexts.get(i)).align;
                        char c = 65535;
                        int hashCode = str.hashCode();
                        if (hashCode != -1371700497) {
                            if (hashCode != -1047432319) {
                                if (hashCode == 1015327489) {
                                    if (str.equals("ALIGN_OPPOSITE")) {
                                        c = 2;
                                    }
                                }
                            } else if (str.equals("ALIGN_NORMAL")) {
                                c = 0;
                            }
                        } else if (str.equals("ALIGN_CENTER")) {
                            c = 1;
                        }
                        if (c == 0) {
                            alignment = Alignment.ALIGN_NORMAL;
                        } else if (c == 1) {
                            alignment = Alignment.ALIGN_CENTER;
                        } else if (c == 2) {
                            alignment = Alignment.ALIGN_OPPOSITE;
                        }
                        Alignment alignment2 = alignment;
                        if (((Text) draftTexts.get(i)).padding_left <= 100) {
                        }
                        ((Text) draftTexts.get(i)).padding_left = (((Text) draftTexts.get(i)).padding_left * ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION) / canvasWidth;
                        ((Text) draftTexts.get(i)).padding_right = (((Text) draftTexts.get(i)).padding_right * ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION) / canvasWidth;
                        float f = (float) ((Text) draftTexts.get(i)).letter_spacing;
                        createTextStickView(((Text) draftTexts.get(i)).id, ((Text) draftTexts.get(i)).text, font, ((Text) draftTexts.get(i)).layout_x, ((Text) draftTexts.get(i)).layout_y, ((Text) draftTexts.get(i)).rotate, ((Text) draftTexts.get(i)).scale, ((Text) draftTexts.get(i)).padding_left, ((Text) draftTexts.get(i)).padding_right, alignment2, ((Text) draftTexts.get(i)).opacity, ((Text) draftTexts.get(i)).underLine, ((Text) draftTexts.get(i)).strikethrough, f, (float) ((Text) draftTexts.get(i)).line_spacing);
                        i++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        finish();
                    }
                }

                try {
                    selectedBgColor = draft.background_color;
                    selectedBgGradient = draft.background_gradient;
                    selectedBgPattern = draft.background_photo;
                    if (selectedBgColor != null) {
                        ivBackground.setVisibility(View.GONE);
                        vBgColor.setBackgroundColor(Color.parseColor(selectedBgColor));
                    } else if (selectedBgGradient != null) {
                        ivBackground.setVisibility(View.GONE);
                        gradientTypeBg = draft.gradient_type;
                        linearDirectionBg = draft.gradient_linear_direction;
                        changeBackground(null, selectedBgGradient, null);
                    } else if (selectedBgPattern != null) {
                        z = false;
                        ivBackground.setVisibility(View.VISIBLE);
                        BitmapUtil.applyBlur(EditorActivity.this, selectedBgPattern, ((IndicatorSeekBar) bgSeekBars.get(1)).getProgress(), ivBackground);
                        ivBackground.setScaleX(draft.photo_scale);
                        ivBackground.setScaleY(draft.photo_scale);
                        ((IndicatorSeekBar) bgSeekBars.get(0)).setProgress((draft.photo_scale - 1.0f) * 100.0f);
                        ((IndicatorSeekBar) bgSeekBars.get(1)).setProgress((float) draft.photo_blur);
                        setRoundedRect();
                        firstLaunch = z;

                    }

                } catch (Exception e2) {
                    e2.printStackTrace();
                    finish();
                }
            }
            z = false;
            setRoundedRect();
            firstLaunch = z;
        } catch (Exception e3) {
            e3.printStackTrace();
            finish();
        }
    }


    private void setRoundedRect() {
        for (int i = 0; i < this.allLayouts.size(); i++) {
            if (((Layout) this.template.layouts.get(i)).rounded_rect) {
                ViewGroup viewGroup = (ViewGroup) this.allLayouts.get(i);
                if (viewGroup.getChildAt(0) instanceof ShapeOfView) {
                    ((ShapeOfView) viewGroup.getChildAt(0)).setDrawable(AppUtil.getRoundedRect(this, getResources().getColor(R.color.colorWhite), ((Layout) this.template.layouts.get(i)).topLeftRadius, ((Layout) this.template.layouts.get(i)).topRightRadius, ((Layout) this.template.layouts.get(i)).bottomLeftRadius, ((Layout) this.template.layouts.get(i)).bottomRightRadius));
                }
            }
        }
    }

    private void setMediaOptions(@NonNull ViewGroup viewGroup, boolean z) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof ShapeOfView) {
                ShapeOfView shapeOfView = (ShapeOfView) childAt;
                shapeOfView.setDrawingCacheEnabled(false);
                shapeOfView.buildDrawingCache(false);
                if (shapeOfView.getChildCount() > 1 && !shapeOfView.getChildAt(1).isShown()) {
                    this.mediaMasks.add(shapeOfView.getChildAt(1));
                }
            }

            if (childAt instanceof PhotoView) {
                final PhotoView photoView = (PhotoView) childAt;
                photoView.setTag(this.photoTag);
                this.photoTag++;
                if (!z && this.isDraft && this.draftPhotoIds.contains(photoView.getTag().toString())) {
                    String str = ((Photo) this.draft.photos.get(this.draftPhotoIds.indexOf(photoView.getTag().toString()))).path;
                    Bitmap decodeFile = BitmapFactory.decodeFile(str, null);
                    this.addedPhotos.add(photoView);
                    photoView.setFullScreen(true, false);
                    photoView.enableImageTransforms(true);
                    photoView.setCenterCropScaleType(true);
                    photoView.bindPhoto(decodeFile);
                    photoView.setPhotoPath(str);
                    photoView.setVisibility(View.VISIBLE);
                }

                ViewGroup viewGroup2 = (ViewGroup) photoView.getParent();
                viewGroup2.setOnDragListener(new DragListener());
                final ViewGroup viewGroup3 = (ViewGroup) viewGroup2.getParent().getParent();
                final ViewGroup viewGroup4 = (ViewGroup) ((ViewGroup) viewGroup3.getChildAt(2)).getChildAt(0);
                this.fabControllers.add(viewGroup4);
                for (int i2 = 0; i2 < this.allLayouts.size(); i2++) {
                    if (((View) this.allLayouts.get(i2)).equals(viewGroup3)) {
                        photoView.setPhotoRotation(((Layout) this.template.layouts.get(i2)).rotation);
                    }
                }

                photoView.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {

                        if (!EditorActivity.this.txBtnClicked) {
                            EditorActivity.this.isClickable();
                            EditorActivity.this.hideControllersFab(viewGroup4);
                            setCurrentTextStickerEdit(false, currentTextSticker);
                            setCurrentImgStickerEdit(false, currentImgSticker);

                            EditorActivity.this.photoClicked = photoView;
                            if (viewGroup4.isShown()) {
                                viewGroup4.setVisibility(View.GONE);
                                return;
                            }
                            viewGroup4.setVisibility(View.VISIBLE);
                            viewGroup4.getChildAt(0).setOnClickListener(new OnClickListener() {
                                public void onClick(View view) {
                                    EditorActivity.this.isClickable();
                                    EditorActivity.this.addedPhotos.remove(EditorActivity.this.photoClicked);
                                    EditorActivity.this.photoClicked.clearDrawable();
                                    viewGroup4.setVisibility(View.INVISIBLE);
                                    viewGroup3.getChildAt(1).setVisibility(View.VISIBLE);
                                    EditorActivity.this.photoClicked.setVisibility(View.INVISIBLE);
                                    EditorActivity.this.photoClicked = null;
                                }
                            });
                            viewGroup4.getChildAt(1).setOnClickListener(new OnClickListener() {
                                public void onClick(View view) {

                                    if (SDK_INT >= 33) {
                                        if (AppUtil.permissionGranted(EditorActivity.this, "android.permission.READ_MEDIA_IMAGES")){
                                            EditorActivity.this.findViewById(R.id.fl_fragment).setVisibility(View.VISIBLE);
                                            EditorActivity.this.addFragment(FiltersFrag.getInstance(EditorActivity.this.photoClicked.getPhoto()), R.id.fl_fragment, 0, 0);
                                        }
                                    }   else if (SDK_INT >= 30) {
                                        if (AppUtil.permissionGranted(EditorActivity.this, "android.permission.READ_EXTERNAL_STORAGE") && AppUtil.permissionGranted(EditorActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                                            EditorActivity.this.findViewById(R.id.fl_fragment).setVisibility(View.VISIBLE);
                                            EditorActivity.this.addFragment(FiltersFrag.getInstance(EditorActivity.this.photoClicked.getPhoto()), R.id.fl_fragment, 0, 0);
                                        }
                                    }else{
                                        if (AppUtil.permissionGranted(EditorActivity.this, "android.permission.READ_EXTERNAL_STORAGE") && AppUtil.permissionGranted(EditorActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                                            EditorActivity.this.findViewById(R.id.fl_fragment).setVisibility(View.VISIBLE);
                                            EditorActivity.this.addFragment(FiltersFrag.getInstance(EditorActivity.this.photoClicked.getPhoto()), R.id.fl_fragment, 0, 0);
                                        }
                                    }

                                }
                            });
                            viewGroup4.getChildAt(1).setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }

            boolean z2 = childAt instanceof PhotoView;

            if (z2) {
                PhotoView photoView = (PhotoView) childAt;
                final ViewGroup viewGroup8 = (ViewGroup) photoView.getParent();
                final ViewGroup viewGroup9 = (ViewGroup) ((ViewGroup) viewGroup8.getParent()).getParent();
                if (viewGroup9.getChildAt(1) instanceof RelativeLayout) {
                    viewGroup9.getChildAt(1).setOnClickListener(new OnOneOffClickListener() {
                        public void onOneClick(View view) {
                            if (!EditorActivity.this.txBtnClicked) {
                                EditorActivity.this.isClickable();
                                EditorActivity.this.hideControllersFab(null);
                                EditorActivity.this.overlayClicked = (RelativeLayout) viewGroup9.getChildAt(1);
                                setCurrentTextStickerEdit(false, currentTextSticker);
                                setCurrentImgStickerEdit(false, currentImgSticker);

                                EditorActivity.this.photoClicked = (PhotoView) viewGroup8.getChildAt(0);
                                selectedViewRatio = AppUtil.convertDecimalToFraction((float) overlayClicked.getMeasuredWidth(), (float) EditorActivity.this.overlayClicked.getMeasuredHeight());
                                Intent intent = new Intent(EditorActivity.this, Gallery.class);
                                intent.putExtra("title", "Select media");
                                intent.putExtra("mode", 0);
                                intent.putExtra("maxSelection", 1);
                                EditorActivity.this.startActivityForResult(intent, 0);
                                reset();
                            }
                        }
                    });
                }
                if (viewGroup8.getChildAt(0).isShown() || photoView.isShown()) {
                    viewGroup9.getChildAt(1).setVisibility(View.INVISIBLE);
                }
            } else if (childAt instanceof ViewGroup) {
                setMediaOptions((ViewGroup) childAt, z);
            }
        }
    }


    public void updateMediaOrder(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof PhotoView) {
                PhotoView photoView = (PhotoView) childAt;
                if (photoView.isShown()) {
                    this.addedPhotos.add(photoView);
                }
            } else if (childAt instanceof ViewGroup) {
                updateMediaOrder((ViewGroup) childAt);
            }
        }
    }


    public void setSelectedPhoto(Bitmap bitmap) {
        if (bitmap != null) {
            String sb2 = "photo-" +
                    AppUtil.getCurrentTime() +
                    ".png";
            String str = "/";
            String sb4 = this.draftsPath +
                    str +
                    this.draftFolder +
                    str;
            BitmapUtil.savePhoto(bitmap, sb4, sb2);
            PhotoView photoView = this.photoClicked;
            if (photoView != null) {
                photoView.setVisibility(View.VISIBLE);
            }
            this.overlayClicked.setVisibility(View.INVISIBLE);
            PhotoView photoView2 = this.photoClicked;
            if (photoView2 != null) {
                this.addedPhotos.add(photoView2);
                this.photoClicked.setFullScreen(true, false);
                this.photoClicked.enableImageTransforms(true);
                this.photoClicked.setCenterCropScaleType(true);
                this.photoClicked.bindPhoto(bitmap);
                PhotoView photoView3 = this.photoClicked;
                String sb5 = sb4 +
                        sb2;
                photoView3.setPhotoPath(sb5);
                Log.e("photoView", "==>" + photoView3.getPhotoPath());
            }
        }
        onBackPressed();
    }

    private void createTextStickView(int i, String str, Font font, int i2, int i3, float f, float f2, int i4, int i5, Alignment alignment, int i6, boolean z, boolean z2, float f3, float f4) {

        this.textSticker = new TextStickerView(this, this.canvasWidth, this.canvasHeight, this.fontProvider);

        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.addRule(10);

        this.textSticker.setLayoutParams(layoutParams);
        this.textSticker.setText(str);
        this.textSticker.setLayoutX(i2);
        this.textSticker.setLayoutY(i3);
        this.textSticker.setRotateAngle(f);
        this.textSticker.setScale(f2);
        this.textSticker.setPaddingLeft(i4);
        this.textSticker.setPaddingRight(i5);
        this.textSticker.setFont(font);
        this.textSticker.setAlign(alignment);
        this.textSticker.setOpacity(i6);
        this.textSticker.setUnderLine(z);
        this.textSticker.setStrikethrough(z2);
        this.textSticker.setLetterSpacing(f3);
        this.textSticker.setLineSpacing(f4);
        this.textSticker.setTag(i);

        this.textSticker.setOperationListener(new TextStickerView.OperationListener() {
            public void onUnselect(TextStickerView textStickerView) {
            }

            public void onDelete(TextStickerView textStickerView) {
                if (EditorActivity.this.currentTextSticker != null && textStickerView.getTag().equals(EditorActivity.this.currentTextSticker.getTag()) && textStickerView.isShowHelpBox()) {
                    if (EditorActivity.this.wgTextEditor.isShown()) {
                        EditorActivity.this.setCurrentTextStickerEdit(false, textStickerView);
                    }
                    EditorActivity.this.flTextSticker.removeView(textStickerView);
                    EditorActivity.this.allTextSticker.remove(textStickerView);
                }
                EditorActivity.this.setTxBtnClicked();
            }

            public void onEdit(TextStickerView textStickerView) {
                EditorActivity.this.bgClose();
                EditorActivity.this.setCurrentTextStickerEdit(true, textStickerView);
                EditorActivity.this.initTextEntitiesValues(textStickerView);
                EditorActivity.this.setTxBtnClicked();
            }

            public void onTouch(TextStickerView textStickerView) {
                EditorActivity.this.touchTextSticker = textStickerView;
                EditorActivity.this.setTxBtnClicked();
            }

            public void onSelect(TextStickerView textStickerView) {

                setCurrentImgStickerEdit(false, imgSticker);

                Iterator it = EditorActivity.this.fabControllers.iterator();
                while (it.hasNext()) {
                    ((ViewGroup) it.next()).setVisibility(View.INVISIBLE);
                }
                if (EditorActivity.this.currentTextSticker != null) {
                    EditorActivity.this.currentTextSticker.setInEdit(false);
                }
                EditorActivity.this.currentTextSticker = textStickerView;
                EditorActivity.this.currentTextSticker.setInEdit(true);
                EditorActivity.this.currentTextSticker.setShowHelpBox(true);
                EditorActivity.this.setTxBtnClicked();
            }
        });

        this.flTextSticker.addView(this.textSticker);
        this.allTextSticker.add(this.textSticker);
        setCurrentTextStickerEdit(true, this.textSticker);
    }

    private void initTextEntitiesListeners() {

        this.etTextEditor.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                if (EditorActivity.this.currentTextSticker != null) {
                    EditorActivity.this.currentTextSticker.setText(editable.toString());
                }
            }
        });

        this.etTextEditor.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                EditorActivity.this.findViewById(R.id.iv_text_keyboard).setVisibility(View.GONE);
                EditorActivity.this.findViewById(R.id.iv_text_edit).setVisibility(View.VISIBLE);
                EditorActivity.this.ntbTextEditor.setVisibility(View.GONE);
                EditorActivity.this.findViewById(R.id.swg_text_editor).setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.iv_text_done).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                setCurrentTextStickerEdit(false, currentTextSticker);
            }
        });

        findViewById(R.id.iv_text_edit).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                initTextEntitiesValues(currentTextSticker);
                AppUtil.hideKeyboard(EditorActivity.this, etTextEditor);
                EditorActivity.this.findViewById(R.id.iv_text_keyboard).setVisibility(View.VISIBLE);
                EditorActivity.this.findViewById(R.id.iv_text_edit).setVisibility(View.GONE);
                EditorActivity.this.ntbTextEditor.setVisibility(View.VISIBLE);
                EditorActivity.this.findViewById(R.id.swg_text_editor).setVisibility(View.VISIBLE);
                EditorActivity.this.findViewById(R.id.swg_text_editor).post(new Runnable() {
                    public void run() {
                        if (EditorActivity.this.currentTextSticker.getLayoutY() <= EditorActivity.this.centreY - ((EditorActivity.this.canvasHeight * 10) / 100)) {
                            EditorActivity.this.params.setMargins(0, 0, 0, DensityUtil.dp2px(EditorActivity.this, 80.0f));
                        } else {
                            EditorActivity.this.params.setMargins(0, ((-EditorActivity.this.screenHeight) / 2) + EditorActivity.this.findViewById(R.id.swg_text_editor).getMeasuredHeight(), 0, 0);
                        }
                        EditorActivity.this.rlContainer.setLayoutParams(EditorActivity.this.params);
                    }
                });
            }
        });

        findViewById(R.id.iv_text_keyboard).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                AppUtil.showKeyboard(EditorActivity.this, etTextEditor);
                if (EditorActivity.this.currentTextSticker.getLayoutY() <= EditorActivity.this.centreY - ((EditorActivity.this.canvasHeight * 10) / 100)) {
                    EditorActivity.this.params.setMargins(0, 0, 0, 0);
                } else {
                    EditorActivity.this.params.setMargins(0, ((-EditorActivity.this.screenHeight) / 2) + DensityUtil.dp2px(EditorActivity.this, 80.0f), 0, 0);
                }
                EditorActivity.this.rlContainer.setLayoutParams(EditorActivity.this.params);
                EditorActivity.this.ntbTextEditor.setVisibility(View.GONE);
                EditorActivity.this.findViewById(R.id.swg_text_editor).setVisibility(View.VISIBLE);
                EditorActivity.this.findViewById(R.id.iv_text_keyboard).setVisibility(View.GONE);
                EditorActivity.this.findViewById(R.id.iv_text_edit).setVisibility(View.VISIBLE);
            }
        });

        iv_align_left.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                iv_align_left.setImageResource(R.drawable.align_left_s);
                iv_align_center.setImageResource(R.drawable.ic_text_align_center);
                iv_align_right.setImageResource(R.drawable.ic_text_align_right);

                EditorActivity.this.currentTextSticker.setAlign(Alignment.ALIGN_NORMAL);
            }
        });

        iv_align_center.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                iv_align_left.setImageResource(R.drawable.ic_text_align_left);
                iv_align_center.setImageResource(R.drawable.center_s);
                iv_align_right.setImageResource(R.drawable.ic_text_align_right);

                EditorActivity.this.currentTextSticker.setAlign(Alignment.ALIGN_CENTER);
            }
        });

        iv_align_right.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                iv_align_left.setImageResource(R.drawable.ic_text_align_left);
                iv_align_center.setImageResource(R.drawable.ic_text_align_center);
                iv_align_right.setImageResource(R.drawable.align_right_s);

                EditorActivity.this.currentTextSticker.setAlign(Alignment.ALIGN_OPPOSITE);
            }
        });

        iv_text_underline.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (EditorActivity.this.currentTextSticker.isUnderLine()) {
                    iv_text_underline.setImageResource(R.drawable.ic_underline);
                    EditorActivity.this.currentTextSticker.setUnderLine(false);
                    return;
                }
                iv_text_underline.setImageResource(R.drawable.underline_s);
                EditorActivity.this.currentTextSticker.setUnderLine(true);
            }
        });

        iv_text_strikethrough.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (EditorActivity.this.currentTextSticker.isStrikethrough()) {
                    iv_text_strikethrough.setImageResource(R.drawable.ic_text_strikethrough);
                    EditorActivity.this.currentTextSticker.setStrikethrough(false);
                    return;
                }
                iv_text_strikethrough.setImageResource(R.drawable.text_line_s);
                EditorActivity.this.currentTextSticker.setStrikethrough(true);
            }
        });

        findViewById(R.id.iv_quote).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                TextStickerView currentTextSticker1 = EditorActivity.this.currentTextSticker;
                String str = "\"";
                String sb = str +
                        EditorActivity.this.currentTextSticker.getText() +
                        str;
                currentTextSticker1.setText(sb);
            }
        });

        ((IndicatorSeekBar) this.teSeekBars.get(0)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                EditorActivity.this.currentTextSticker.getFont().setSize((seekParams.progressFloat * 3.0f) / 1000.0f);
                EditorActivity.this.currentTextSticker.invalidate();
            }
        });

        ((IndicatorSeekBar) this.teSeekBars.get(1)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                EditorActivity.this.currentTextSticker.setOpacity((seekParams.progress * 255) / 100);
                EditorActivity.this.currentTextSticker.invalidate();
            }
        });

        ((IndicatorSeekBar) this.teSeekBars.get(2)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                EditorActivity.this.currentTextSticker.setLetterSpacing((float) seekParams.progress);
                EditorActivity.this.currentTextSticker.invalidate();
            }
        });

        ((IndicatorSeekBar) this.teSeekBars.get(3)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                EditorActivity.this.currentTextSticker.setLineSpacing((float) (seekParams.progress * 2));
                EditorActivity.this.currentTextSticker.invalidate();
            }
        });

        ((IndicatorSeekBar) this.teSeekBars.get(4)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                EditorActivity.this.currentTextSticker.setPaddingLeft(seekParams.progress);
            }
        });

        ((IndicatorSeekBar) this.teSeekBars.get(5)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                EditorActivity.this.currentTextSticker.setPaddingRight(seekParams.progress);
            }
        });

        setTexEditorTabs();

        this.fontCategories = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.font_categories)));
        for (int i = 0; i < this.fontCategories.size(); i++) {
            TabLayout tabLayout = this.tlFontCategories;
            tabLayout.addTab(tabLayout.newTab().setText((CharSequence) this.fontCategories.get(i)));
        }
        String defaultFontCategory = this.fontProvider.getDefaultFontCategory();
        FontProvider fontProvider2 = this.fontProvider;
        List fontNames = fontProvider2.getFontNames(fontProvider2.getDefaultFontCategory());
        FontProvider fontProvider3 = this.fontProvider;
        this.rvFontAdapter = new RvFontAdapter(this, defaultFontCategory, fontNames, fontProvider3, fontProvider3.getDefaultFontName(), this.screenWidth);
        this.rvFontDetail.setAdapter(this.rvFontAdapter);
        this.tlFontCategories.addOnTabSelectedListener((TabLayout.OnTabSelectedListener) new TabLayout.OnTabSelectedListener() {
            public void onTabReselected(TabLayout.Tab tab) {
            }

            public void onTabUnselected(TabLayout.Tab tab) {
            }

            public void onTabSelected(TabLayout.Tab tab) {
                RvFontAdapter rvFontAdapter = new RvFontAdapter(EditorActivity.this, tab.getText().toString(), EditorActivity.this.fontProvider.getFontNames(tab.getText().toString()), EditorActivity.this.fontProvider, EditorActivity.this.fontProvider.getDefaultFontName(), EditorActivity.this.screenWidth);
                EditorActivity.this.rvFontAdapter = rvFontAdapter;
                EditorActivity.this.rvFontDetail.setAdapter(EditorActivity.this.rvFontAdapter);
            }
        });

        this.rvColorAdapter = new RvColorsAdapter(this, this.colorList, false, this.screenWidth);
        this.rvColors.setLayoutManager(new GridLayoutManager((Context) this, 9, RecyclerView.VERTICAL, false));
        this.rvColors.setAdapter(this.rvColorAdapter);
        findViewById(R.id.btn_text_custom_color).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                EditorActivity.this.findViewById(R.id.wg_custom_color).setVisibility(View.VISIBLE);
                EditorActivity.this.findViewById(R.id.wg_custom_color).startAnimation(AnimationsUtil.SlideUpIn);
            }
        });

        final String[] stringArray = getResources().getStringArray(R.array.gradient_palette);
        this.rvGradientAdapter = new RvGradientAdapter(this, stringArray, this.gradientType, this.linearDirection, false, this.screenWidth);
        this.rvGradients.setLayoutManager(new GridLayoutManager((Context) this, 9, RecyclerView.VERTICAL, false));
        this.rvGradients.setAdapter(this.rvGradientAdapter);
        this.msgType.setItems(getResources().getStringArray(R.array.directions_menu));
        this.msgType.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(MaterialSpinner materialSpinner, int i, long j, Object obj) {
                gradientType = directionsMenu[i];
                rvGradientAdapter = new RvGradientAdapter(EditorActivity.this, stringArray, gradientType, EditorActivity.this.linearDirection, false, EditorActivity.this.screenWidth);
                EditorActivity.this.rvGradients.setAdapter(EditorActivity.this.rvGradientAdapter);
            }
        });

        findViewById(R.id.iv_gradientH).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                EditorActivity.this.linearDirection = "Horizontal";
                rvGradientAdapter = new RvGradientAdapter(EditorActivity.this, stringArray, gradientType, EditorActivity.this.linearDirection, false, EditorActivity.this.screenWidth);
                EditorActivity.this.rvGradients.setAdapter(EditorActivity.this.rvGradientAdapter);
            }
        });

        findViewById(R.id.iv_gradientV).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                EditorActivity.this.linearDirection = "Vertical";
                rvGradientAdapter = new RvGradientAdapter(EditorActivity.this, stringArray, gradientType, EditorActivity.this.linearDirection, false, EditorActivity.this.screenWidth);
                EditorActivity.this.rvGradients.setAdapter(EditorActivity.this.rvGradientAdapter);
            }
        });
    }

    private void setTexEditorTabs() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Builder(getResources().getDrawable(R.drawable.icon_text_align), getResources().getColor(R.color.colorGrey)).build());
        arrayList.add(new Builder(getResources().getDrawable(R.drawable.icon_text_adjust), getResources().getColor(R.color.colorGrey)).build());
        arrayList.add(new Builder(getResources().getDrawable(R.drawable.icon_text_font), getResources().getColor(R.color.colorGrey)).build());
        arrayList.add(new Builder(getResources().getDrawable(R.drawable.icon_text_color), getResources().getColor(R.color.colorGrey)).build());
        arrayList.add(new Builder(getResources().getDrawable(R.drawable.icon_text_gradient), getResources().getColor(R.color.colorGrey)).build());

        this.ntbTextEditor.setModels(arrayList);
        this.ntbTextEditor.setModelIndex(0);
        this.ntbTextEditor.getLayoutParams().width = arrayList.size() * DensityUtil.dp2px(this, 35.0f);
        this.ntbTextEditor.setOnTabBarSelectedIndexListener(new OnTabBarSelectedIndexListener() {
            public void onEndTabSelected(Model model, int i) {
            }

            public void onStartTabSelected(Model model, int i) {
                if (i == 0) {
                    EditorActivity.this.findViewById(R.id.sswg_text_align).setVisibility(View.VISIBLE);
                    EditorActivity.this.findViewById(R.id.sswg_text_adjust).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_font).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_color).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_gradient).setVisibility(View.GONE);
                } else if (i == 1) {
                    EditorActivity.this.findViewById(R.id.sswg_text_align).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_adjust).setVisibility(View.VISIBLE);
                    EditorActivity.this.findViewById(R.id.sswg_text_font).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_color).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_gradient).setVisibility(View.GONE);
                } else if (i == 2) {
                    EditorActivity.this.findViewById(R.id.sswg_text_align).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_adjust).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_font).setVisibility(View.VISIBLE);
                    EditorActivity.this.findViewById(R.id.sswg_text_color).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_gradient).setVisibility(View.GONE);
                } else if (i == 3) {
                    EditorActivity.this.findViewById(R.id.sswg_text_align).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_adjust).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_font).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_color).setVisibility(View.VISIBLE);
                    EditorActivity.this.findViewById(R.id.sswg_text_gradient).setVisibility(View.GONE);
                } else if (i == 4) {
                    EditorActivity.this.findViewById(R.id.sswg_text_align).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_adjust).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_font).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_color).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_text_gradient).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initTextEntitiesValues(TextStickerView textStickerView) {
        int i = AnonymousClass67.SwitchMapandroidtextLayoutAlignment[textStickerView.getAlign().ordinal()];
        if (i == 1) {
            iv_align_left.setImageResource(R.drawable.align_left_s);
            iv_align_center.setImageResource(R.drawable.ic_text_align_center);
            iv_align_right.setImageResource(R.drawable.ic_text_align_right);
        } else if (i == 2) {
            iv_align_left.setImageResource(R.drawable.ic_text_align_left);
            iv_align_center.setImageResource(R.drawable.center_s);
            iv_align_right.setImageResource(R.drawable.ic_text_align_right);

        } else if (i == 3) {

            iv_align_left.setImageResource(R.drawable.ic_text_align_left);
            iv_align_center.setImageResource(R.drawable.ic_text_align_center);
            iv_align_right.setImageResource(R.drawable.align_right_s);

        }

        if (textStickerView.isUnderLine()) {
            iv_text_underline.setImageResource(R.drawable.underline_s);
        } else {
            iv_text_underline.setImageResource(R.drawable.ic_underline);
        }
        if (textStickerView.isStrikethrough()) {
            iv_text_strikethrough.setImageResource(R.drawable.text_line_s);
        } else {
            iv_text_strikethrough.setImageResource(R.drawable.ic_text_strikethrough);
        }
        ((IndicatorSeekBar) this.teSeekBars.get(0)).setProgress((textStickerView.getFont().getSize() * 1000.0f) / 3.0f);
        ((IndicatorSeekBar) this.teSeekBars.get(1)).setProgress((float) ((textStickerView.getOpacity() * 100) / 255));
        ((IndicatorSeekBar) this.teSeekBars.get(2)).setProgress(textStickerView.getLetterSpacing());
        ((IndicatorSeekBar) this.teSeekBars.get(3)).setProgress(textStickerView.getLineSpacing() / 2.0f);
        ((IndicatorSeekBar) this.teSeekBars.get(4)).setProgress((float) textStickerView.getPaddingLeft());
        ((IndicatorSeekBar) this.teSeekBars.get(5)).setProgress((float) textStickerView.getPaddingRight());
        this.tlFontCategories.getTabAt(this.fontCategories.indexOf(textStickerView.getFont().getCategory())).select();
        this.rvFontAdapter = new RvFontAdapter(this, textStickerView.getFont().getCategory(), this.fontProvider.getFontNames(textStickerView.getFont().getCategory()), this.fontProvider, textStickerView.getFont().getTypeface(), this.screenWidth);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        int fontPosition = this.fontProvider.getFontPosition(textStickerView.getFont().getCategory(), textStickerView.getFont().getTypeface());
        int i2 = this.screenWidth;
        int i3 = i2 / 2;
        double d = (double) i2;
        Double.isNaN(d);
        linearLayoutManager.scrollToPositionWithOffset(fontPosition, (i3 - (((int) (d / 3.5d)) / 2)) - DensityUtil.dp2px(this, 6.0f));
        this.rvFontDetail.setLayoutManager(linearLayoutManager);
        this.rvFontDetail.setAdapter(this.rvFontAdapter);
    }

    public void changeTextEntityFont(String str, String str2) {
        this.currentTextSticker.getFont().setCategory(str);
        this.currentTextSticker.getFont().setTypeface(str2);
        this.currentTextSticker.invalidate();
    }

    public void changeTextEntityColor(String str) {
        this.currentTextSticker.getFont().setColor(Color.parseColor(str));
        this.currentTextSticker.getFont().setGradient(null);
        this.currentTextSticker.getFont().setPatternPath(null);
        this.currentTextSticker.invalidate();
    }

    public void changeTextEntityGradient(String[] strArr) {
        this.currentTextSticker.getFont().setGradient(strArr);
        this.currentTextSticker.getFont().setGradientType(this.gradientType);
        this.currentTextSticker.getFont().setLinearDirection(this.linearDirection);
        this.currentTextSticker.getFont().setPatternPath(null);
        this.currentTextSticker.invalidate();
    }

    private void setTxBtnClicked() {
        this.txBtnClicked = true;
        new Handler().postDelayed(new Runnable() {
            public void run() {
                EditorActivity.this.txBtnClicked = false;
                setMediaOptions(templateViewGroup, false);
            }
        }, 1500);
    }

    public void setCurrentTextStickerEdit(boolean z, TextStickerView textStickerView) {

        if (!this.firstLaunch) {
            TextStickerView textStickerView2 = this.currentTextSticker;
            if (textStickerView2 != null) {
                textStickerView2.setInEdit(false);
            }
            this.currentTextSticker = textStickerView;
            if (z) {
                this.etTextEditor.setText(this.currentTextSticker.getText());
                EditText editText = this.etTextEditor;
                editText.setSelection(editText.getText().length());
                if (this.currentTextSticker.getLayoutY() <= this.centreY - ((this.canvasHeight * 10) / 100)) {
                    this.params.setMargins(0, 0, 0, 0);
                } else {
                    this.params.setMargins(0, ((-this.screenHeight) / 2) + DensityUtil.dp2px(this, 00.0f), 0, 0);
                }
                this.rlContainer.setLayoutParams(this.params);
                AppUtil.showKeyboard(this, this.etTextEditor);
                AppUtil.showWidget(this.vWidgets, findViewById(R.id.wg_text_edit));
                this.currentTextSticker.setInEdit(true);
            } else if (textStickerView != null) {
                textStickerView.setInEdit(false);

                if (findViewById(R.id.wg_main_menu).getVisibility() == View.GONE) {
                    this.params.setMargins(0, 0, 0, DensityUtil.dp2px(this, 00.0f));
                    this.rlContainer.setLayoutParams(this.params);
                    AppUtil.hideKeyboard(this, this.etTextEditor);
                    AppUtil.showWidget(this.vWidgets, findViewById(R.id.wg_main_menu));
                }
                this.currentTextSticker = null;
            }
            findViewById(R.id.iv_text_keyboard).setVisibility(View.GONE);
            findViewById(R.id.iv_text_edit).setVisibility(View.VISIBLE);
            this.ntbTextEditor.setVisibility(View.GONE);
            findViewById(R.id.swg_text_editor).setVisibility(View.VISIBLE);

        } else if (textStickerView != null) {
            textStickerView.setInEdit(false);
        }
    }

    private void setCurrentImgStickerEdit(boolean z, ImageStickerView imageStickerView) {

        if (!this.firstLaunch) {
            ImageStickerView imageStickerView2 = this.currentImgSticker;
            if (imageStickerView2 != null) {
                imageStickerView2.setInEdit(false);
            }
            this.currentImgSticker = imageStickerView;
            if (z) {
                imageStickerView.setInEdit(true);
            } else if (imageStickerView != null) {
                imageStickerView.setInEdit(false);
            }
        } else if (imageStickerView != null) {
            imageStickerView.setInEdit(false);
        }
    }

    private void setBackgroundListeners() {
        setBgEditorTabs();
        this.rvColorAdapter = new RvColorsAdapter(this, this.colorList, true, this.screenWidth);
        this.rvBgColors.setLayoutManager(new GridLayoutManager((Context) this, 9, RecyclerView.VERTICAL, false));
        this.rvBgColors.setAdapter(this.rvColorAdapter);

        findViewById(R.id.btn_bg_custom_color).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                EditorActivity.this.findViewById(R.id.wg_custom_color).setVisibility(View.VISIBLE);
                EditorActivity.this.findViewById(R.id.wg_custom_color).startAnimation(AnimationsUtil.SlideUpIn);
            }
        });

        findViewById(R.id.btn_custom_color_close).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                EditorActivity.this.findViewById(R.id.wg_custom_color).startAnimation(AnimationsUtil.SlideUpOut);
                EditorActivity.this.findViewById(R.id.wg_custom_color).setVisibility(View.GONE);
            }
        });

        final String[] stringArray = getResources().getStringArray(R.array.gradient_palette);
        this.rvBgGradientAdapter = new RvGradientAdapter(this, stringArray, this.gradientTypeBg, this.linearDirectionBg, true, this.screenWidth);
        this.rvBgGradients.setLayoutManager(new GridLayoutManager((Context) this, 9, RecyclerView.VERTICAL, false));
        this.rvBgGradients.setAdapter(this.rvBgGradientAdapter);
        this.msgBgType.setItems(getResources().getStringArray(R.array.directions_menu));
        this.msgBgType.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(MaterialSpinner materialSpinner, int i, long j, Object obj) {
                gradientTypeBg = directionsMenu[i];
                rvBgGradientAdapter = new RvGradientAdapter(EditorActivity.this, stringArray, gradientTypeBg, EditorActivity.this.linearDirectionBg, true, EditorActivity.this.screenWidth);
                EditorActivity.this.rvBgGradients.setAdapter(EditorActivity.this.rvBgGradientAdapter);
            }
        });

        findViewById(R.id.iv_bg_gradientH).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                EditorActivity.this.linearDirectionBg = "Horizontal";
                rvBgGradientAdapter = new RvGradientAdapter(EditorActivity.this, stringArray, gradientTypeBg, EditorActivity.this.linearDirectionBg, true, EditorActivity.this.screenWidth);
                EditorActivity.this.rvBgGradients.setAdapter(EditorActivity.this.rvBgGradientAdapter);
            }
        });

        findViewById(R.id.iv_bg_gradientV).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                EditorActivity.this.linearDirectionBg = "Vertical";
                rvBgGradientAdapter = new RvGradientAdapter(EditorActivity.this, stringArray, gradientTypeBg, EditorActivity.this.linearDirectionBg, true, EditorActivity.this.screenWidth);
                EditorActivity.this.rvBgGradients.setAdapter(EditorActivity.this.rvBgGradientAdapter);
            }
        });

        ((IndicatorSeekBar) this.bgSeekBars.get(0)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                EditorActivity.this.ivBackground.setScaleX((((float) seekParams.progress) / 1000.0f));
                EditorActivity.this.ivBackground.setScaleY((((float) seekParams.progress) / 1000.0f));
            }
        });

        ((IndicatorSeekBar) this.bgSeekBars.get(1)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                if (EditorActivity.this.selectedBgPattern != null) {
                    BitmapUtil.applyBlur(EditorActivity.this, EditorActivity.this.selectedBgPattern, seekParams.progress, EditorActivity.this.ivBackground);
                    return;
                }
                Toast.makeText(EditorActivity.this, EditorActivity.this.getString(R.string.MSG_SELECT_PATTERN), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setBgEditorTabs() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new Builder(getResources().getDrawable(R.drawable.icon_text_color), getResources().getColor(R.color.colorGrey)).build());
        arrayList.add(new Builder(getResources().getDrawable(R.drawable.icon_text_gradient), getResources().getColor(R.color.colorGrey)).build());

        this.ntbBgEditor.setModels(arrayList);
        this.ntbBgEditor.setModelIndex(0);
        this.ntbBgEditor.getLayoutParams().width = arrayList.size() * DensityUtil.dp2px(this, 60.0f);
        this.ntbBgEditor.setOnTabBarSelectedIndexListener(new OnTabBarSelectedIndexListener() {
            public void onEndTabSelected(Model model, int i) {
            }

            public void onStartTabSelected(Model model, int i) {
                if (i == 0) {
                    EditorActivity.this.findViewById(R.id.sswg_bg_color).setVisibility(View.VISIBLE);
                    EditorActivity.this.findViewById(R.id.sswg_bg_gradient).setVisibility(View.GONE);

                } else if (i == 1) {
                    EditorActivity.this.findViewById(R.id.sswg_bg_color).setVisibility(View.GONE);
                    EditorActivity.this.findViewById(R.id.sswg_bg_gradient).setVisibility(View.VISIBLE);
                    EditorActivity.this.findViewById(R.id.sswg_bg_pattern).setVisibility(View.GONE);
                }
            }
        });
    }

    public void changeBackground(String str, String[] strArr, String str2) {
        if (str != null) {
            this.ivBackground.setVisibility(View.GONE);
            this.vBgColor.setBackgroundColor(Color.parseColor(str));
            this.selectedBgColor = str;
            this.selectedBgGradient = null;
            this.selectedBgPattern = null;
        } else if (strArr != null) {
            this.ivBackground.setVisibility(View.GONE);
            this.vBgColor.setBackgroundDrawable(null);
            this.vBgColor.setBackgroundDrawable(AppUtil.generateViewGradient(strArr, this.gradientTypeBg, this.linearDirectionBg, this.canvasWidth, this.canvasHeight));
            this.selectedBgColor = null;
            this.selectedBgGradient = strArr;
            this.selectedBgPattern = null;
        } else if (str2 != null) {
            this.ivBackground.setVisibility(View.VISIBLE);
            BitmapUtil.applyBlur(this, str2, ((IndicatorSeekBar) this.bgSeekBars.get(1)).getProgress(), this.ivBackground);
            this.selectedBgColor = null;
            this.selectedBgGradient = null;
            this.selectedBgPattern = str2;
        }
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

    public void loading(boolean z, boolean z2) {
        if (!z) {
            findViewById(R.id.wg_loading).setVisibility(View.GONE);
        } else if (z2) {
            findViewById(R.id.wg_loading).setVisibility(View.VISIBLE);
        }
    }

    private void setCustomColorListeners() {
        final int[] iArr = {255};
        final int[] iArr2 = {255};
        final int[] iArr3 = {255};
        ((IndicatorSeekBar) findViewById(R.id.sb_red)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                iArr[0] = seekParams.progress;
                String str = "#%02x%02x%02x";
                if (EditorActivity.this.wgTextEditor.isShown()) {
                    EditorActivity.this.changeTextEntityColor(String.format(str, Integer.valueOf(iArr[0]), Integer.valueOf(iArr2[0]), Integer.valueOf(iArr3[0])));
                    return;
                }
                EditorActivity.this.changeBackground(String.format(str, Integer.valueOf(iArr[0]), Integer.valueOf(iArr2[0]), Integer.valueOf(iArr3[0])), null, null);
            }
        });
        ((IndicatorSeekBar) findViewById(R.id.sb_green)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                iArr2[0] = seekParams.progress;
                String str = "#%02x%02x%02x";
                if (EditorActivity.this.wgTextEditor.isShown()) {
                    EditorActivity.this.changeTextEntityColor(String.format(str, Integer.valueOf(iArr[0]), Integer.valueOf(iArr2[0]), Integer.valueOf(iArr3[0])));
                    return;
                }
                EditorActivity.this.changeBackground(String.format(str, Integer.valueOf(iArr[0]), Integer.valueOf(iArr2[0]), Integer.valueOf(iArr3[0])), null, null);
            }
        });
        ((IndicatorSeekBar) findViewById(R.id.sb_blue)).setOnSeekChangeListener(new OnSeekChangeListener() {
            public void onStartTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onStopTrackingTouch(IndicatorSeekBar indicatorSeekBar) {
            }

            public void onSeeking(SeekParams seekParams) {
                iArr3[0] = seekParams.progress;
                String str = "#%02x%02x%02x";
                if (EditorActivity.this.wgTextEditor.isShown()) {
                    EditorActivity.this.changeTextEntityColor(String.format(str, Integer.valueOf(iArr[0]), Integer.valueOf(iArr2[0]), Integer.valueOf(iArr3[0])));
                    return;
                }
                EditorActivity.this.changeBackground(String.format(str, Integer.valueOf(iArr[0]), Integer.valueOf(iArr2[0]), Integer.valueOf(iArr3[0])), null, null);
            }
        });
    }

    public void isClickable() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - this.mLastClickTime >= 3000) {
            this.mLastClickTime = currentTimeMillis;
        }
    }

    private void hideControllersFab(View view) {
        Iterator it = this.fabControllers.iterator();
        while (it.hasNext()) {
            ViewGroup viewGroup = (ViewGroup) it.next();
            if (viewGroup != view) {
                viewGroup.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void removeUnusedFiles() {
        String str = "/";
        String sb = this.draftsPath +
                str +
                this.draftFolder +
                str;
        ArrayList filesPath = AppUtil.getFilesPath(sb);
        ArrayList arrayList = new ArrayList();
        Iterator it = this.draft.photos.iterator();
        while (it.hasNext()) {
            arrayList.add(((Photo) it.next()).path);
        }
        Iterator it3 = filesPath.iterator();
        while (it3.hasNext()) {
            String str2 = (String) it3.next();
            if (!str2.contains("thumb") && !arrayList.contains(str2) && new File(str2).exists()) {
                new File(str2).delete();
            }
        }
    }

    public void onPermissionGranted() {
        if (this.selectedBtn == R.id.menu_save) {

            final Dialog dialog = new Dialog(this, R.style.BottomDialog);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.wg_resolution_alert_dialog);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;

            dialog.getWindow().setAttributes(lp);

            CardView btn_standard = (CardView) dialog.findViewById(R.id.btn_standard);
            CardView btn_medium = (CardView) dialog.findViewById(R.id.btn_medium);
            CardView btn_high = (CardView) dialog.findViewById(R.id.btn_high);

            btn_standard.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (EditorActivity.this.totalMemory <= 1500) {
                        EditorActivity.this.saveWidth = 450;
                        EditorActivity.this.saveHeight = 800;
                    } else if (EditorActivity.this.totalMemory <= 2500) {
                        EditorActivity.this.saveWidth = 720;
                        EditorActivity.this.saveHeight = 1280;
                    } else if (EditorActivity.this.totalMemory <= 4500) {
                        EditorActivity.this.saveWidth = 810;
                        EditorActivity.this.saveHeight = 1440;
                    } else {
                        EditorActivity.this.saveWidth = 1080;
                        EditorActivity.this.saveHeight = 1920;
                    }
                    dialog.dismiss();
                    EditorActivity.this.isSaved = true;
                    new SaveDraft(0, true, "save_photo").execute();
                    new SaveDraft(0, true, "save_photo").execute();
                }
            });

            btn_medium.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {

                    if (EditorActivity.this.totalMemory <= 1500) {
                        EditorActivity.this.saveWidth = 720;
                        EditorActivity.this.saveHeight = 1280;
                    } else if (EditorActivity.this.totalMemory <= 2500) {
                        EditorActivity.this.saveWidth = 810;
                        EditorActivity.this.saveHeight = 1440;
                    } else if (EditorActivity.this.totalMemory < 4500) {
                        EditorActivity.this.saveWidth = 1080;
                        EditorActivity.this.saveHeight = 1920;
                    } else {
                        EditorActivity.this.saveWidth = 1620;
                        EditorActivity.this.saveHeight = 2880;
                    }
                    dialog.dismiss();
                    EditorActivity.this.isSaved = true;
                    new SaveDraft(0, true, "save_photo").execute();
                }
            });

            btn_high.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (EditorActivity.this.totalMemory <= 1500) {
                        EditorActivity.this.saveWidth = 810;
                        EditorActivity.this.saveHeight = 1440;
                    } else if (EditorActivity.this.totalMemory <= 2500) {
                        EditorActivity.this.saveWidth = 1080;
                        EditorActivity.this.saveHeight = 1920;
                    } else if (EditorActivity.this.totalMemory < 4500) {
                        EditorActivity.this.saveWidth = 1620;
                        EditorActivity.this.saveHeight = 2880;
                    } else {
                        EditorActivity.this.saveWidth = 2160;
                        EditorActivity.this.saveHeight = 3840;
                    }
                    dialog.dismiss();
                    EditorActivity.this.isSaved = true;
                    new SaveDraft(0, true, "save_photo").execute();
                }
            });

            dialog.show();
        }
    }

    private void imageProcessing(Bitmap bitmap) {
        String sb = "StoryMaker" +
                AppUtil.getCurrentTime() +
                ".png";
        this.outputName = sb;
        File file = new File(this.savePath);
        if (!file.exists()) {
            file.mkdirs();
        }

        File file2 = new File(file, this.outputName);
        if (file2.exists()) {
            file2.delete();
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            bitmap.compress(CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        String str = "/";
        super.onActivityResult(i, i2, intent);
        char c = 65535;
        if (i2 == -1) {
            String str2 = "filePath";
            if (i == 0) {

                try {
                    String stringExtra = intent.getStringExtra(str2);
                    String fileType = AppUtil.getFileType(stringExtra);
                    int hashCode = fileType.hashCode();
                    if (hashCode != 71588) {
                        if (hashCode != 70760763) {
                            if (hashCode == 82650203) {
                                if (fileType.equals("Video")) {
                                    c = 2;
                                }
                            }
                        } else if (fileType.equals("Image")) {
                            c = 0;
                        }
                    } else if (fileType.equals(Registry.BUCKET_GIF)) {
                        c = 1;
                    }
                    if (c == 0) {
                        AppUtil.cropPhoto(this, new File(stringExtra), ((Integer) this.selectedViewRatio.get(0)).intValue(), ((Integer) this.selectedViewRatio.get(1)).intValue(), this.totalMemory);
                    } else if (c == 1) {
                    }
                } catch (Exception unused) {
                    Toasty.error((Context) this, (CharSequence) getString(R.string.MSG_SOMETHING_WRONG), 0, true).show();
                }
            } else if (i == 69) {
                try {
                    Bitmap bitmap = Media.getBitmap(getContentResolver(), UCrop.getOutput(intent));
                    findViewById(R.id.fl_fragment).setVisibility(View.VISIBLE);
                    addFragment(FiltersFrag.getInstance(bitmap), R.id.fl_fragment, 0, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (i == 11) {
                String stringExtra2 = intent.getStringExtra("rewardType");
                if (stringExtra2.equals(getString(R.string.TITLE_FONTS_LOCKED))) {
                    this.rvFontAdapter.refreshList();
                    this.rvFontDetail.setAdapter(this.rvFontAdapter);
                } else if (stringExtra2.equals(getString(R.string.TITLE_GRADIENTS_LOCKED))) {
                    this.rvGradientAdapter.refreshList(findViewById(R.id.wg_background_menu).isShown());
                    this.rvGradients.setAdapter(this.rvGradientAdapter);
                    this.rvBgGradients.setAdapter(this.rvGradientAdapter);
                }
                Toast.makeText(this, "Thanks for watching", Toast.LENGTH_SHORT).show();
            } else if (i == 12) {

                try {
                    addStickerView();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }



            }
        } else if (i2 == 96) {
            Toasty.error((Context) this, (CharSequence) UCrop.getError(intent).getMessage(), 0, true).show();
        }
    }

    private void addStickerView() throws IOException {
        Log.e("@@@@@@@@@@@", "onDeleteClick: _______4" );

            this.d = Drawable.createFromStream(getAssets().open("crown/" + Glob_Sticker.SelectedTattooName.replace("assets://crown/", "")), null);

        ImageStickerView imageStickerView = new ImageStickerView(EditorActivity.this, "", (float) (EditorActivity.this.canvasWidth / 2), (float) (EditorActivity.this.canvasHeight / 2), 0.3f, 0.0f, EditorActivity.this.allImageSticker.size());
        Bitmap B = convertDrawableToBitmap(this.d);
        imgSticker = imageStickerView;
        imgSticker.setBitmap(B);
        EditorActivity.this.allImageSticker.remove(EditorActivity.this.currentImgSticker);
        EditorActivity.this.flImageSticker.removeView(EditorActivity.this.currentImgSticker);
        Log.e("@@@@@@@@@@@", "onDeleteClick: _______3"+currentImgSticker);
        this.imgSticker.setOperationListener(new ImageStickerView.OperationListener() {
            public void onDeleteClick() {
                EditorActivity.this.allImageSticker.remove(EditorActivity.this.currentImgSticker);
                EditorActivity.this.flImageSticker.removeView(EditorActivity.this.currentImgSticker);
                Log.e("@@@@@@@@@@@", "onDeleteClick: _______3"+currentImgSticker );
            }

            public void onEdit(ImageStickerView imageStickerView) {
                EditorActivity.this.bgClose();
                setCurrentTextStickerEdit(false, textSticker);

                Iterator it = EditorActivity.this.fabControllers.iterator();
                while (it.hasNext()) {
                    ((ViewGroup) it.next()).setVisibility(View.INVISIBLE);
                }
                if (EditorActivity.this.currentImgSticker != null) {
                    EditorActivity.this.currentImgSticker.setInEdit(false);
                }
                EditorActivity.this.currentImgSticker = imageStickerView;
                EditorActivity.this.currentImgSticker.setInEdit(true);
            }

            public void onTop(ImageStickerView imageStickerView) {
                int indexOf = EditorActivity.this.allImageSticker.indexOf(imageStickerView);
                if (indexOf != EditorActivity.this.allImageSticker.size() - 1) {
                    EditorActivity.this.allImageSticker.add(EditorActivity.this.allImageSticker.size(), (ImageStickerView) EditorActivity.this.allImageSticker.remove(indexOf));
                }
            }
        });

        this.flImageSticker.addView(imgSticker);
        Log.d("@@@@@@@", "addStickerView: " + imgSticker);
        Log.d("@@@@@@@", "addStickerView: " + imgSticker);
        this.allImageSticker.add(this.imgSticker);
        Log.d("@@@@@@@", "addStickerView:_____1 " + allImageSticker);
        Log.d("@@@@@@@", "addStickerView:_____1.1 " + flImageSticker);

        setCurrentImgStickerEdit(true, this.imgSticker);
    }

    private Bitmap convertDrawableToBitmap(Drawable d) {
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(12, 12, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);
        return bitmap;
    }

    public void onBackPressed() {
        if (!findViewById(R.id.wg_loading).isShown()) {
            if (findViewById(R.id.fl_fragment).isShown()) {
                findViewById(R.id.fl_fragment).setVisibility(View.GONE);
                if (this.fm.findFragmentById(R.id.fl_fragment) != null) {
                    FragmentTransaction beginTransaction = this.fm.beginTransaction();
                    Fragment findFragmentById = this.fm.findFragmentById(R.id.fl_fragment);
                    findFragmentById.getClass();
                    beginTransaction.remove(findFragmentById).commit();
                    return;
                }
                return;
            }
            goBack();
        }
    }

    @OnClick({R.id.menu_close})
    public void goBack() {

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

        btn_neutral.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btn_negative.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (EditorActivity.this.isDraft) {
                    EditorActivity.this.removeUnusedFiles();
                } else {
                    String sb = EditorActivity.this.draftsPath +
                            "/" +
                            EditorActivity.this.draftFolder;
                    AppUtil.deleteFolder(new File(sb));
                }

                dialog.dismiss();
                Intent intent = new Intent(EditorActivity.this, MainActivity.class);
                startActivity(intent);
                EditorActivity.this.finish();
                EditorActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });


        btn_positive.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {

               if(SDK_INT >= 33){
                   if (AppUtil.permissionGranted(EditorActivity.this, "android.permission.READ_MEDIA_IMAGES") && AppUtil.permissionGranted(EditorActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                       EditorActivity.this.isSaved = false;
                       Intent intent = new Intent(EditorActivity.this, MainActivity.class);
                       startActivity(intent);
                       EditorActivity.this.finish();
                       dialog.dismiss();
                       new SaveDraft(0, true, "save_draft").execute();
                   }
               } if(SDK_INT >= 32) {
                    if (AppUtil.permissionGranted(EditorActivity.this, "android.permission.READ_EXTERNAL_STORAGE") && AppUtil.permissionGranted(EditorActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                        EditorActivity.this.isSaved = false;
                        Intent intent = new Intent(EditorActivity.this, MainActivity.class);
                        startActivity(intent);
                        EditorActivity.this.finish();
                        dialog.dismiss();
                        new SaveDraft(0, true, "save_draft").execute();
                    }
                }else{
                    if (AppUtil.permissionGranted(EditorActivity.this, "android.permission.READ_EXTERNAL_STORAGE") && AppUtil.permissionGranted(EditorActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                        EditorActivity.this.isSaved = false;
                        Intent intent = new Intent(EditorActivity.this, MainActivity.class);
                        startActivity(intent);
                        EditorActivity.this.finish();
                        dialog.dismiss();
                        new SaveDraft(0, true, "save_draft").execute();
                    }
                }

            }
        });
        dialog.show();
    }

    @OnClick({R.id.menu_save})
    public void save() {
        isClickable();
        if (addedPhotos.size() < frameLayouts.size()) {
            Toasty.warning(EditorActivity.this, (CharSequence) getString(R.string.MSG_FILL_FRAMES), 0, true).show();
            return;
        }
        selectedBtn = R.id.menu_save;
        AppUtil.editorPermissionGranted(EditorActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE");
    }

    @OnClick({R.id.menu_text})
    public void addText() {
        isClickable();

        Font font = new Font();
        font.setColor(-16777216);
        font.setSize(0.075f);
        font.setCategory(this.fontProvider.getDefaultFontCategory());
        font.setTypeface(this.fontProvider.getDefaultFontName());
        createTextStickView(this.allTextSticker.size() - 1, "My Story", font, this.canvasWidth / 2, this.centreY, 0.0f, 1.0f, 0, 0, Alignment.ALIGN_CENTER, 255, false, false, 0.0f, 10.0f);

    }

    @OnClick({R.id.menu_background})
    public void addBackground() {
        isClickable();
        findViewById(R.id.wg_background_menu).setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.bg_close})
    public void bgClose() {
        isClickable();
        findViewById(R.id.wg_background_menu).setVisibility(View.GONE);
    }

    @OnClick({R.id.menu_sticker})
    public void addSticker() {
        isClickable();
        if (SDK_INT >= 33) {
            if (AppUtil.permissionGranted(this, "android.permission.READ_MEDIA_IMAGES")) {
                startActivityForResult(new Intent(getApplicationContext(), StickerActivity.class), 12);
            }
        }else if (SDK_INT >= 30) {
            if (AppUtil.permissionGranted(this, "android.permission.READ_EXTERNAL_STORAGE") && AppUtil.permissionGranted(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                startActivityForResult(new Intent(getApplicationContext(), StickerActivity.class), 12);
            }
        }else{
            if (AppUtil.permissionGranted(this, "android.permission.READ_EXTERNAL_STORAGE") && AppUtil.permissionGranted(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
                startActivityForResult(new Intent(getApplicationContext(), StickerActivity.class), 12);
            }
        }

    }


    private void replaceScreen() {
        if (whichActivitytoStart == 1) {
            Intent intent = new Intent(EditorActivity.this, PreviewActivity.class);
            intent.putExtra("savedImageFile", A);
            intent.putExtra("draft", B);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            EditorActivity.this.startActivity(intent);
            EditorActivity.this.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
        }
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
        handler.removeCallbacks(runnable);
        this.isActivityLeft = true;
    }
}

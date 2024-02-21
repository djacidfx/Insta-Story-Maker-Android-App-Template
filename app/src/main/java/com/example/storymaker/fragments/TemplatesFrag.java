package com.example.storymaker.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.storymaker.R;
import com.example.storymaker.Activity.LaunchScreenActivity;
import com.example.storymaker.Activity.MainActivity;
import com.example.storymaker.adapters.SrvTemplateSectionAdapter;
import com.example.storymaker.utils.ContractsUtil;

import java.util.ArrayList;

public class TemplatesFrag extends Fragment {

    private final ArrayList<String> categories = new ArrayList<>();

    private MainActivity mainActivity;
    private View rootView;
    RecyclerView rvTemplates;
    private SrvTemplateSectionAdapter srvTemplateSectionAdapter;
    public static final int ITEM_PER_AD=2;

    public static TemplatesFrag getInstance(MainActivity mainActivity2) {
        TemplatesFrag templatesFrag = new TemplatesFrag();
        templatesFrag.mainActivity = mainActivity2;
        return templatesFrag;
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.rootView = layoutInflater.inflate(R.layout.fragment_templates, viewGroup, false);

        rvTemplates = (RecyclerView)rootView.findViewById(R.id.rv_templates);

        setTemplateCategories();
        return this.rootView;
    }


    private void setTemplateCategories() {
        if (this.mainActivity == null) {
            Intent intent = new Intent(getActivity(), LaunchScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }
        this.categories.addAll(ContractsUtil.templateCategories.keySet());
        this.srvTemplateSectionAdapter = new SrvTemplateSectionAdapter(this.mainActivity, this.categories);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        this.rvTemplates.setHasFixedSize(true);
        this.rvTemplates.setLayoutManager(linearLayoutManager);
        this.rvTemplates.setAdapter(this.srvTemplateSectionAdapter);
    }






}

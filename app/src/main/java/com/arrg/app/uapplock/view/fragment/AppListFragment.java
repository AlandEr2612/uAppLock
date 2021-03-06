package com.arrg.app.uapplock.view.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arrg.app.uapplock.R;
import com.arrg.app.uapplock.UAppLock;
import com.arrg.app.uapplock.interfaces.AppListFragmentView;
import com.arrg.app.uapplock.model.entity.App;
import com.arrg.app.uapplock.model.service.UAppLockService;
import com.arrg.app.uapplock.presenter.IAppListFragmentPresenter;
import com.arrg.app.uapplock.util.SharedPreferencesUtil;
import com.arrg.app.uapplock.util.kisstools.utils.PackageUtil;
import com.arrg.app.uapplock.util.kisstools.utils.ResourceUtil;
import com.arrg.app.uapplock.util.kisstools.utils.SystemUtil;
import com.arrg.app.uapplock.util.kisstools.utils.ToastUtil;
import com.arrg.app.uapplock.view.activity.ApplicationListActivity;
import com.arrg.app.uapplock.view.adapter.AppAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.kyleduo.switchbutton.SwitchButton;
import com.sbrukhanda.fragmentviewpager.FragmentVisibilityListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.arrg.app.uapplock.UAppLock.LOCKED_APPS_PREFERENCES;

public class AppListFragment extends Fragment implements AppListFragmentView, FragmentVisibilityListener, BaseQuickAdapter.OnRecyclerViewItemClickListener, BaseQuickAdapter.OnRecyclerViewItemLongClickListener {

    private App app;
    private AppAdapter appAdapter;
    private ArrayList<App> appArrayList;
    private IAppListFragmentPresenter iAppListFragmentPresenter;
    private SharedPreferences lockedAppsPreferences;
    private SharedPreferencesUtil preferencesUtil;
    private Snackbar snackbar;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    public AppListFragment() {

    }

    public static AppListFragment newInstance(ArrayList<App> apps) {
        AppListFragment appListFragment = new AppListFragment();

        Bundle args = new Bundle();
        args.putSerializable("apps", apps);

        appListFragment.setArguments(args);

        return appListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iAppListFragmentPresenter = new IAppListFragmentPresenter(this);
        iAppListFragmentPresenter.onCreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        iAppListFragmentPresenter.setAdapter(getArguments());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        iAppListFragmentPresenter.unregisterReceiver();
    }

    @Override
    public void configFragment() {
        preferencesUtil = new SharedPreferencesUtil(getActivity());
        lockedAppsPreferences = getActivity().getSharedPreferences(LOCKED_APPS_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void setAdapter(ArrayList<App> apps) {
        appAdapter = null;

        appArrayList = apps;

        appAdapter = new AppAdapter(R.layout.app_item, apps, lockedAppsPreferences, preferencesUtil);

        recyclerView.setAdapter(appAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        appAdapter.setOnRecyclerViewItemClickListener(this);
        appAdapter.setOnRecyclerViewItemLongClickListener(this);
    }

    @Override
    public FragmentActivity getFragmentActivity() {
        return getActivity();
    }

    @Override
    public Context getFragmentContext() {
        return getActivity();
    }

    @Override
    public Integer getIndex() {
        return ApplicationListActivity.ALL_APPS;
    }

    @Override
    public ArrayList<App> getApps() {
        return appArrayList;
    }

    @Override
    public void add(App app, Integer position) {
        appAdapter.add(position, app);
    }

    @Override
    public void removeAppIn(Integer position) {
        appAdapter.remove(position);
    }

    @Override
    public void showErrorMessage(String message) {
        ToastUtil.show(message);
    }

    @Override
    public void onItemClick(View view, int i) {
        app = appAdapter.getItem(i);

        SwitchButton switchCompat = (SwitchButton) view.findViewById(R.id.switchCompat);
        switchCompat.toggle();

        app.setChecked(switchCompat.isChecked());

        preferencesUtil.putValue(lockedAppsPreferences, app.getAppPackage(), switchCompat.isChecked());

        iAppListFragmentPresenter.updateListWith(app, switchCompat.isChecked(), getFragmentActivity());

        if (!switchCompat.isChecked()) {
            UAppLockService.SERVICE.unlockApp(app.getAppPackage());
        }
    }

    @Override
    public boolean onItemLongClick(View view, final int i) {
        app = appAdapter.getItem(i);

        new MaterialDialog.Builder(getActivity())
                .title(app.getAppName())
                .icon(app.getAppIcon())
                .limitIconToDefaultSize()
                .content(getString(R.string.long_click_message))
                .positiveText(getString(R.string.open))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(app.getAppPackage());
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    }
                })
                .negativeText(getString(R.string.uninstall))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (SystemUtil.isRooted()) {
                            snackbar = Snackbar.make(getView(), R.string.uninstall_app_message, Snackbar.LENGTH_LONG);
                            snackbar.setActionTextColor(ResourceUtil.getColor(R.color.colorPrimary));
                            snackbar.setAction(ResourceUtil.getString(R.string.cancel), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.setCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    switch (event) {
                                        case DISMISS_EVENT_TIMEOUT:
                                            if (!PackageUtil.silentUninstall(app.getAppPackage(), false)) {
                                                ToastUtil.show(R.string.uninstall_error_message);
                                            } else {
                                                removeAppIn(i);
                                            }
                                            break;
                                    }
                                }
                            });
                            snackbar.show();
                        } else {
                            PackageUtil.uninstall(app.getAppPackage());
                        }
                    }
                })
                .neutralText(getString(R.string.app_details))
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + app.getAppPackage()));
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
                    }
                })
                .typeface(UAppLock.typeface(), UAppLock.typeface())
                .build()
                .show();

        return false;
    }

    @Override
    public void onFragmentVisible() {

    }

    @Override
    public void onFragmentInvisible() {
    }
}

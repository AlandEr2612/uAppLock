package com.arrg.app.uapplock.interfaces;

import com.arrg.app.uapplock.model.service.UAppLockService;

public interface UAppLockServicePresenter {

    void onCreate(UAppLockService uAppLockService);

    void registerScreenReceiver();

    void registerIconOnAppDrawerReceiver();

    void registerNotificationReceiver();

    void restartServiceIfNeeded();

    void unregisterReceivers();
}
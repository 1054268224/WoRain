package com.cydroid.softmanager.strategyupdate.manager;

import com.cydroid.softmanager.strategyupdate.strategy.AutoBootAppWhiteUpdateStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.AutoBootBlackUpdateStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.AutoBootBroadcastActionWhiteUpdateStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.AutoBootProviderWhiteUpdateStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.AutoBootServiceActionWhiteUpdateStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.AutoBootServiceAppWhiteUpdateStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.BlackKillUpdateStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.IStrategy;
import com.cydroid.softmanager.strategyupdate.strategy.OneCleanUpdateStrategy;

import android.content.Context;
/**
 * Created by mengjk on 17-5-16.
 */
public class WhiteListStrategyUpdateManager extends BaseStrategyUpdateManager {
    private static final String TAG = WhiteListStrategyUpdateManager.class.getSimpleName();
    public WhiteListStrategyUpdateManager(Context context) {
        super(context.getApplicationContext());
        addUpdateStrategy();
    }

    private void addUpdateStrategy() {
        // auto boot
        addAutoBootUpdateStrategy(AutoBootAppWhiteUpdateStrategy.TYPE, AutoBootAppWhiteUpdateStrategy.class);
        addAutoBootUpdateStrategy(AutoBootBroadcastActionWhiteUpdateStrategy.TYPE,
                AutoBootBroadcastActionWhiteUpdateStrategy.class);
        addAutoBootUpdateStrategy(AutoBootProviderWhiteUpdateStrategy.TYPE,
                AutoBootProviderWhiteUpdateStrategy.class);
        addAutoBootUpdateStrategy(AutoBootServiceActionWhiteUpdateStrategy.TYPE,
                AutoBootServiceActionWhiteUpdateStrategy.class);
        addAutoBootUpdateStrategy(AutoBootServiceAppWhiteUpdateStrategy.TYPE,
                AutoBootServiceAppWhiteUpdateStrategy.class);
        // genera
        addGeneraUpdateStrategy(OneCleanUpdateStrategy.TYPE, OneCleanUpdateStrategy.class);
        addGeneraUpdateStrategy(BlackKillUpdateStrategy.TYPE, BlackKillUpdateStrategy.class);
        addGeneraUpdateStrategy(AutoBootBlackUpdateStrategy.TYPE, AutoBootBlackUpdateStrategy.class);
    }

    private void addAutoBootUpdateStrategy(String type, Class<? extends IStrategy> cls) {
        strategyClassMap.put(type, cls);
    }

    private void addGeneraUpdateStrategy(String type, Class<? extends IStrategy> cls) {
        strategyClassMap.put(type, cls);
    }


}

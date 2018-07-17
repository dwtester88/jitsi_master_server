package org.jitsi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.jitsi.android.gui.LauncherActivity;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, LauncherActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);

    }
}

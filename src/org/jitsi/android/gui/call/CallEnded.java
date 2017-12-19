/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jitsi.android.gui.call;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.*;

import android.widget.ImageView;
import net.java.sip.communicator.util.Logger;
import org.jitsi.*;
import org.jitsi.android.*;
import org.jitsi.android.gui.util.*;
import org.jitsi.service.osgi.*;

/**
 * Fragment displayed in <tt>VideoCallActivity</tt> when the call has ended.
 *
 * @author Pawel Domas
 */
public class CallEnded
    extends OSGiFragment
{
    ImageView hangup;
    /**
     * The logger
     */
    private static final Logger logger =
            Logger.getLogger(CallEnded.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Activity activity = getActivity();

        View v = inflater.inflate(R.layout.call_ended, container, false);

        ViewUtil.setTextViewValue(v, R.id.callTime,
                                  VideoCallActivity.callState.callDuration);
        String errorReason = VideoCallActivity.callState.errorReason;
        if(!errorReason.isEmpty())
        {
            ViewUtil.setTextViewValue(v, R.id.callErrorReason, errorReason);
        }
        else
        {
            ViewUtil.ensureVisible(v, R.id.callErrorReason, false);
        }

       hangup= (ImageView) v.findViewById(R.id.callHangupButton);

            hangup.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    logger.info("mychange videocallactivity status is at callended ");

                    Context ctx = getActivity();
                    getActivity().finish();
                    ctx.startActivity(JitsiApplication.getHomeIntent());
                }
            });
        hangup.postDelayed(endcall,2000);


        return v;
    }

    private Runnable endcall = new Runnable() {
        @Override
        public void run() {

            Activity activity = getActivity();

            hangup.performClick();



        }
    };
}

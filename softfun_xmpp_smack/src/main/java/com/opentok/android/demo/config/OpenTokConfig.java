package com.opentok.android.demo.config;

import com.softfun_xmpp.R;
import com.softfun_xmpp.application.GlobalContext;

public class OpenTokConfig {
    public static final String SESSION_ID = GlobalContext.getInstance().getResources().getString(R.string.session_id);
    public static final String TOKEN =  GlobalContext.getInstance().getResources().getString(R.string.token);
    public static final String API_KEY = GlobalContext.getInstance().getResources().getString(R.string.api_key);
}

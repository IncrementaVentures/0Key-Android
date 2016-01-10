package com.incrementaventures.okey.Networking;


import com.parse.ParseException;

/**
 * Created by Andres on 09-01-2016.
 */
public class ParseErrorHandler {
    private static OnParseErrorListener mListener;

    public interface OnParseErrorListener {
        void sessionExpired();
        void timeout();
        void defaultError();
    }

    private ParseErrorHandler() {  }

    public static void initialize(OnParseErrorListener listener) {
        mListener = listener;
    }

    public static void handleError(ParseException error) {
        switch (error.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN:
                mListener.sessionExpired();
                break;
            case ParseException.TIMEOUT:
                mListener.timeout();
                break;
            default:
                mListener.defaultError();
        }
    }
}

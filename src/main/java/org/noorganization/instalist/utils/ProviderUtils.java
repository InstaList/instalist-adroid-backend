package org.noorganization.instalist.utils;

import android.net.Uri;

/**
 * Common provider data.
 * Created by Desnoo on 14.02.2016.
 */
public class ProviderUtils {

    public final static String AUTHORITY = "org.noorganization.instalist.provider";

    public final static String BASE_VENDOR = "vnd.org.noorganization.instalist.";
    /**
     * The base content uri. Build a uri with the table paths.
     **/
    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

}

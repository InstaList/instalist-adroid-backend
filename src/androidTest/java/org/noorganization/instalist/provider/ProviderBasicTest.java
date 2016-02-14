package org.noorganization.instalist.provider;

/**
 * Created by damihe on 21.10.15.
 */

import android.net.Uri;
import android.test.AndroidTestCase;

public class ProviderBasicTest extends AndroidTestCase {

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    public void testPathSegments() throws Exception {
        Uri testUri = Uri.parse("content://org.noorganization.instalist.provider/test1/test2");
        assertEquals("content", testUri.getScheme());
        assertEquals("org.noorganization.instalist.provider", testUri.getAuthority());
        assertEquals("test1", testUri.getPathSegments().get(0));
    }
}
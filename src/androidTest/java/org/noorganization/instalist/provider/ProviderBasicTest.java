/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
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
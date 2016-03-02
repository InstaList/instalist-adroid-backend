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

package org.noorganization.instalist.view.utils;

/**
 * Utils for provider usage.
 * Created by Tino on 24.10.2015.
 */
public class ProviderUtils {


    /**
     * Extends the the selection by a id query parameter at the first position.
     * @param _idString the name of the id.
     * @param _selection the selection provided by content provider _selection.
     * @return the new selection string.
     */
    public static String prependIdToQuery(String _idString, String _selection){
        return _idString + "=?" + ((_selection != null) ? " AND " + _selection : "");
    }

    /**
     * Extends the selectionArgs so that there can be a id be added to the selection args.
     * @param _selectionArgs the _selectionArgs provided by the content provider.
     * @param _uuid the uuid of the table.
     * @return the selectionArgs extended by the id parameter.
     */
    public static String[] prependSelectionArgs(String[] _selectionArgs, String _uuid){
        String[] selectionArgs = new String[1 + (_selectionArgs != null ? _selectionArgs.length : 0)];
        selectionArgs[0] = _uuid;
        if (_selectionArgs != null) {
            System.arraycopy(_selectionArgs, 0, selectionArgs, 1, _selectionArgs.length);
        }
        return selectionArgs;
    }


}

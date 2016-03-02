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

package org.noorganization.instalist.enums;

/**
 * The type of the model as enum.
 * Created by Desnoo on 16.02.2016.
 */
public enum eModelType {
    CATEGORY,
    LIST,
    LIST_ENTRY,
    INGREDIENT,
    PRODUCT,
    RECIPE,
    TAG,
    TAGGED_PRODUCT,
    UNIT,
    ALL;

    public static eModelType getTypeId(int _model) {
        switch (_model) {
            case 0:
                return CATEGORY;
            case 1:
                return LIST;
            case 2:
                return LIST_ENTRY;
            case 3:
                return INGREDIENT;
            case 4:
                return PRODUCT;
            case 5:
                return RECIPE;
            case 6:
                return TAG;
            case 7:
                return TAGGED_PRODUCT;
            case 8:
                return UNIT;
            default:
                return null;
        }
    }
}

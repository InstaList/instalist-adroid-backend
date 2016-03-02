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

package org.noorganization.instalist.model;

import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;

import java.util.Date;

/**
 * Do not move to instalistsynch because this is used to enable logging with triggers.
 * The log includes the actions performed on the elements of the clients.
 * Created by Desnoo on 16.02.2016.
 */
public class LogInfo {

    /**
     * The id of the log.
     */
    private int mId;

    /**
     * The uuid of the item.
     */
    private String mItemUuuid;

    /**
     * The action performed.
     */
    private eActionType mAction;

    /**
     * The model that was affected.
     */
    private eModelType mModelType;

    /**
     * The date when this action was performed.
     */
    private Date mActionDate;


    public LogInfo(int _id, String itemUuuid, eActionType action, eModelType modelType,
            Date actionDate) {
        mId = _id;
        mItemUuuid = itemUuuid;
        mAction = action;
        mModelType = modelType;
        mActionDate = actionDate;
    }

    public static final String TABLE_NAME = "log_info";

    /**
     * Column names that does not contain the table prefix.
     */
    public final static class COLUMN {
        public static final String ID = "_id";
        public static final String ITEM_UUID = "item_uuid";
        public static final String ACTION = "action_id";
        public static final String MODEL = "model_id";
        public static final String ACTION_DATE = "action_date";

        public static final String ALL_COLUMNS[] = {ID, ITEM_UUID, ACTION, MODEL, ACTION_DATE};
        public static final String ALL_COLUMNS_WITH_DATE[] = {ID, ITEM_UUID, ACTION, MODEL,
                "datetime(" + ACTION_DATE + ") as " + ACTION_DATE};
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class PREFIXED_COLUMN {
        public static final String ID = TABLE_NAME.concat(COLUMN.ID);
        public static final String ITEM_UUID = TABLE_NAME.concat(COLUMN.ITEM_UUID);
        public static final String ACTION = TABLE_NAME.concat(COLUMN.ACTION);
        public static final String MODEL = TABLE_NAME.concat(COLUMN.MODEL);
        public static final String ACTION_DATE = TABLE_NAME.concat(COLUMN.ACTION_DATE);

        public static final String ALL_COLUMNS[] = {ID, ITEM_UUID, ACTION, MODEL, ACTION_DATE};
    }

    public static String DB_CREATE_V1 = "CREATE TABLE " + TABLE_NAME + " ( "
            + COLUMN.ID + " INTEGER PRIMARY KEY NOT NULL, "
            + COLUMN.ITEM_UUID + " TEXT NOT NULL, "
            + COLUMN.ACTION + " INTEGER NOT NULL, "
            + COLUMN.ACTION_DATE + " TEXT NOT NULL, "
            + COLUMN.MODEL + " INTEGER NOT NULL "
            + ")";


    /**
     * Create a trigger that is executed when an insertion to a specified table was made.
     *
     * @param _observedTable   the table that should be observed.
     * @param _observedTableId the name of the column id of the observed table.
     * @param _modelType       the type of the model.
     * @return the trigger creation string.
     */
    public static String createTriggerInsertion(String _observedTable, String _observedTableId, eModelType _modelType) {
        return "CREATE TRIGGER track"+ _observedTable + "InsertTrigger "
                + " AFTER INSERT ON " + _observedTable + " FOR EACH ROW "
                + " BEGIN "
                + " INSERT INTO " + LogInfo.TABLE_NAME + " ( "
                + COLUMN.ITEM_UUID + ","
                + COLUMN.ACTION + ","
                + COLUMN.MODEL + ","
                + COLUMN.ACTION_DATE
                + " )"
                + " VALUES ( "
                + " NEW." + _observedTableId + ","
                + " " + eActionType.INSERT.ordinal() + ","
                + " " + _modelType.ordinal() + ","
                + " strftime('%Y-%m-%dT%H:%M:%S', 'now') || 'Z'"
                + " );"
                + " END;";
    }

    public static String createTriggerUpdate(String _observedTable, String _observedTableId, eModelType _modelType) {
        return "CREATE TRIGGER track" + _observedTable + "UpdateTrigger "
                + " AFTER UPDATE ON " + _observedTable + " FOR EACH ROW "
                + " BEGIN "
                + " INSERT INTO " + LogInfo.TABLE_NAME + " ( "
                + COLUMN.ITEM_UUID + ","
                + COLUMN.ACTION + ","
                + COLUMN.MODEL + ","
                + COLUMN.ACTION_DATE
                + " )"
                + " VALUES ( "
                + " NEW." + _observedTableId + ","
                + " " + eActionType.UPDATE.ordinal() + ","
                + " " + _modelType.ordinal() + ","
                + " strftime('%Y-%m-%dT%H:%M:%S', 'now') || 'Z'"
                + " );"
                + " END;";
    }

    public static String createTriggerDeletion(String _observedTable, String _observedTableId, eModelType _modelType) {
        return "CREATE TRIGGER track" + _observedTable + "DeletionTrigger "
                + " AFTER DELETE ON " + _observedTable + " FOR EACH ROW "
                + " BEGIN "
                + " INSERT INTO " + LogInfo.TABLE_NAME + " ( "
                + COLUMN.ITEM_UUID + ","
                + COLUMN.ACTION + ","
                + COLUMN.MODEL + ","
                + COLUMN.ACTION_DATE
                + " )"
                + " VALUES ( "
                + " OLD." + _observedTableId + ","
                + " " + eActionType.DELETE.ordinal() + ","
                + " " + _modelType.ordinal() + ","
                + " strftime('%Y-%m-%dT%H:%M:%S', 'now') || 'Z'"
                + " );"
                + " END;";
    }


    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getItemUuuid() {
        return mItemUuuid;
    }

    public void setItemUuuid(String itemUuuid) {
        mItemUuuid = itemUuuid;
    }

    public eActionType getAction() {
        return mAction;
    }

    public void setAction(eActionType action) {
        mAction = action;
    }

    public eModelType getModelType() {
        return mModelType;
    }

    public void setModelType(eModelType modelType) {
        mModelType = modelType;
    }

    public Date getActionDate() {
        return mActionDate;
    }

    public void setActionDate(Date actionDate) {
        mActionDate = actionDate;
    }
}

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
        public static final String ID          = "_id";
        public static final String ITEM_UUID   = "item_uuid";
        public static final String ACTION      = "action_id";
        public static final String MODEL       = "model_id";
        public static final String ACTION_DATE = "action_date";

        public static final String ALL_COLUMNS[] = {ID, ITEM_UUID, ACTION, MODEL, ACTION_DATE};
        public static final String ALL_COLUMNS_WITH_DATE[] = {ID, ITEM_UUID, ACTION, MODEL, "datetime(" + ACTION_DATE + ") as " + ACTION_DATE};
    }

    /**
     * Column names that are prefixed with the table name. So like this TableName.ColumnName
     */
    public final static class PREFIXED_COLUMN {
        public static final String ID          = TABLE_NAME.concat(COLUMN.ID);
        public static final String ITEM_UUID   = TABLE_NAME.concat(COLUMN.ITEM_UUID);
        public static final String ACTION      = TABLE_NAME.concat(COLUMN.ACTION);
        public static final String MODEL       = TABLE_NAME.concat(COLUMN.MODEL);
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

    public static String DB_TRIGGER_LIST_INSERTION = "CREATE TRIGGER trackListInsertTrigger "
            + " AFTER INSERT ON " + ShoppingList.TABLE_NAME + " FOR EACH ROW "
            + " BEGIN "
            + " INSERT INTO " + LogInfo.TABLE_NAME + " ( "
            + COLUMN.ITEM_UUID + ","
            + COLUMN.ACTION + ","
            + COLUMN.MODEL + ","
            + COLUMN.ACTION_DATE
            + " )"
            + " VALUES ( "
            + " NEW." + ShoppingList.COLUMN.ID + ","
            + " " + eActionType.INSERT.ordinal() + ","
            + " " + eModelType.LIST.ordinal() + ","
            + " strftime('%Y-%m-%dT%H:%M:%S', 'now')"
            + " );"
            + " END;";

    public static String DB_TRIGGER_LIST_DELETION = "CREATE TRIGGER trackListDeletionTrigger "
            + " AFTER DELETE ON " + ShoppingList.TABLE_NAME + " FOR EACH ROW "
            + " BEGIN "
            + " INSERT INTO " + LogInfo.TABLE_NAME + " ( "
            + COLUMN.ITEM_UUID + ","
            + COLUMN.ACTION + ","
            + COLUMN.MODEL + ","
            + COLUMN.ACTION_DATE
            + " )"
            + " VALUES ( "
            + " OLD." + ShoppingList.COLUMN.ID + ","
            + " " + eActionType.DELETE.ordinal() + ","
            + " " + eModelType.LIST.ordinal() + ","
            + " strftime('%Y-%m-%dT%H:%M:%S', 'now')"
            + " );"
            + " END;";

    public static String DB_TRIGGER_LIST_UPDATE = "CREATE TRIGGER trackListUpdateTrigger "
            + " AFTER UPDATE ON " + ShoppingList.TABLE_NAME + " FOR EACH ROW "
            + " BEGIN "
            + " INSERT INTO " + LogInfo.TABLE_NAME + " ( "
            + COLUMN.ITEM_UUID + ","
            + COLUMN.ACTION + ","
            + COLUMN.MODEL + ","
            + COLUMN.ACTION_DATE
            + " )"
            + " VALUES ( "
            + " NEW." + ShoppingList.COLUMN.ID + ","
            + " " + eActionType.UPDATE.ordinal() + ","
            + " " + eModelType.LIST.ordinal() + ","
            + " strftime('%Y-%m-%dT%H:%M:%S', 'now')"
            + " );"
            + " END;";


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

package org.noorganization.instalist.presenter.implementation;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.noorganization.instalist.presenter.ITagController;
import org.noorganization.instalist.presenter.event.Change;
import org.noorganization.instalist.presenter.event.TagChangedMessage;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.provider.internal.TagProvider;

import de.greenrobot.event.EventBus;

public class TagController implements ITagController {

    private static TagController mInstance;

    private EventBus mBus;
    private ContentResolver mResolver;

    private TagController(Context _context) {
        mBus = EventBus.getDefault();
        mResolver = _context.getContentResolver();
    }

    static ITagController getInstance(Context _context) {
        if (mInstance == null) {
            mInstance = new TagController(_context);
        }
        return mInstance;
    }

    @Override
    public Tag createTag(String _title) {
        if (_title == null) {
            return null;
        }
        Cursor tagCursor = mResolver.query(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI), Tag.COLUMN.ALL_COLUMNS, Tag.COLUMN.NAME + "= ?", new String[]{_title}, null, null);
        if (tagCursor == null || tagCursor.getCount() > 0) {
            if (tagCursor != null) {
                tagCursor.close();
            }
            return null;
        }

        tagCursor.close();

        Tag tag = new Tag(_title);
        Uri tagUri = mResolver.insert(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI), tag.toContentValues());
        // insertion went wrong
        if (tagUri == null) {
            return null;
        }
        tag.mUUID = tagUri.getLastPathSegment();
        mBus.post(new TagChangedMessage(Change.CREATED, tag));
        return tag;
    }

    @Override
    public Tag renameTag(Tag _toRename, String _newTitle) {
        if (_toRename == null) {
            return null;
        }
        if (_toRename.mName.equals(_newTitle)) {
            return _toRename;
        }

        Tag toChange = findById(_toRename.mUUID);
        if (toChange == null || _newTitle == null) {
            return _toRename;
        }

        // check if name was changed
        Cursor cursor = mResolver.query(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI),
                Tag.COLUMN.ALL_COLUMNS, Tag.COLUMN.NAME + "=? AND " + Tag.COLUMN.ID + " <> ?",
                new String[]{_newTitle, _toRename.mUUID},
                null);
        if (cursor == null || cursor.getCount() > 0) {
            if (cursor != null) {
                cursor.close();
            }
            return _toRename;
        }
        cursor.close();


        toChange.mName = _newTitle;
        int updatedRows = mResolver.update(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", toChange.mUUID)), toChange.toContentValues(), null, null);

        if (updatedRows == 0) {
            return _toRename;
        }

        mBus.post(new TagChangedMessage(Change.CHANGED, toChange));
        return toChange;
    }

    @Override
    public void removeTag(Tag _toRemove) {
        if (_toRemove == null) {
            return;
        }
        /*
        List<TaggedProduct> taggedProducts = Select.from(TaggedProduct.class).
                where(Condition.prop(TaggedProduct.ATTR_TAG).eq(_toRemove.getId())).list();
        IProductController productController = ControllerFactory.getProductController();
        for (TaggedProduct toNotify : taggedProducts) {
            productController.removeTagFromProduct(toNotify.mProduct, toNotify.mTag);
        }
        */
        int removedRows = mResolver.delete(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", _toRemove.mUUID)), null, null);
        if (removedRows == 0) {
            return;
        }

        mBus.post(new TagChangedMessage(Change.DELETED, _toRemove));
    }

    @Override
    public Tag findById(String _uuid) {
        Cursor cursor = mResolver.query(Uri.parse(TagProvider.SINGLE_TAG_CONTENT_URI.replace("*", _uuid)), Tag.COLUMN.ALL_COLUMNS, null, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        cursor.moveToFirst();
        Tag tag = new Tag();
        tag.mUUID = cursor.getString(cursor.getColumnIndex(Tag.COLUMN.ID));
        tag.mName = cursor.getString(cursor.getColumnIndex(Tag.COLUMN.NAME));
        cursor.close();
        return tag;
    }

    @Override
    public Tag findByName(String _name) {
        Cursor cursor = mResolver.query(Uri.parse(TagProvider.MULTIPLE_TAG_CONTENT_URI),
                Tag.COLUMN.ALL_COLUMNS,
                Tag.COLUMN.NAME + "=?",
                new String[]{_name}, null);
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        cursor.moveToFirst();
        Tag tag = new Tag();
        tag.mUUID = cursor.getString(cursor.getColumnIndex(Tag.COLUMN.ID));
        tag.mName = cursor.getString(cursor.getColumnIndex(Tag.COLUMN.NAME));
        cursor.close();
        return tag;
    }
}

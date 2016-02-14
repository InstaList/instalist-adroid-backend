package org.noorganization.instalist.presenter.event;

import org.noorganization.instalist.model.Tag;

/**
 * Event for notifying about changes of tags. Fired by the presenter. Note: (Un-)Tagged products
 * will be notified with a {@link ProductChangedMessage}.
 * Created by daMihe on 21.07.2015.
 */
public class TagChangedMessage {
    public Change mChange;
    public Tag    mTag;

    public TagChangedMessage(Change _change, Tag _tag) {
        mChange = _change;
        mTag    = _tag;
    }
}

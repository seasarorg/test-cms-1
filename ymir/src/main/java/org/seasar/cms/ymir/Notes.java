package org.seasar.cms.ymir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class Notes {

    public static final String GLOBAL_NOTE = "org.seasar.cms.ymir.GLOBAL_NOTE";

    private List list_ = new ArrayList();

    private LinkedHashMap map_ = new LinkedHashMap();

    private boolean accessed_ = false;

    public Notes() {
    }

    public Notes(Notes notes) {
        add(notes);
    }

    public Notes add(Notes notes) {
        if (notes == null) {
            return this;
        }

        Iterator categories = notes.categories();
        while (categories.hasNext()) {
            String category = (String) categories.next();

            Iterator itr = notes.get(category);
            while (itr.hasNext()) {
                Note note = (Note) itr.next();
                add(category, note);
            }
        }
        return this;
    }

    public Notes add(Note note) {
        return add(GLOBAL_NOTE, note);
    }

    public Notes add(String category, Note note) {
        list_.add(note);
        List noteList = (List) map_.get(category);
        if (noteList == null) {
            noteList = new ArrayList();
            map_.put(category, noteList);
        }
        noteList.add(note);
        return this;
    }

    public Notes clear() {
        list_.clear();
        map_.clear();
        accessed_ = false;
        return this;
    }

    public Iterator get() {
        accessed_ = true;
        return Collections.unmodifiableCollection(list_).iterator();
    }

    public Note[] getNotes() {
        accessed_ = true;
        return (Note[]) list_.toArray(new Note[0]);
    }

    public boolean isAccessed() {
        return accessed_;
    }

    public Iterator get(String category) {
        accessed_ = true;
        List noteList = (List) map_.get(category);
        if (noteList == null) {
            noteList = new ArrayList();
        }
        return Collections.unmodifiableCollection(noteList).iterator();
    }

    public Note[] getNotes(String category) {
        accessed_ = true;
        List noteList = (List) map_.get(category);
        if (noteList == null) {
            return new Note[0];
        } else {
            return (Note[]) noteList.toArray(new Note[0]);
        }
    }

    public boolean isEmpty() {
        return list_.isEmpty();
    }

    public Iterator categories() {
        return Collections.unmodifiableSet(map_.keySet()).iterator();
    }

    public int size() {
        return list_.size();
    }

    public int size(String category) {
        List noteList = (List) map_.get(category);
        if (noteList == null) {
            return 0;
        } else {
            return noteList.size();
        }
    }

    public boolean containsValue(String value) {
        int n = list_.size();
        for (int i = 0; i < n; i++) {
            Note note = (Note) list_.get(i);
            if (note.getValue().equals(value)) {
                return true;
            }
        }

        return false;
    }
}

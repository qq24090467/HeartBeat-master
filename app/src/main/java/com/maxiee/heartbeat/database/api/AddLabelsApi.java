package com.maxiee.heartbeat.database.api;

import android.content.ContentValues;
import android.content.Context;

import com.maxiee.heartbeat.database.tables.LabelsTable;

import java.util.ArrayList;

/**
 * Created by maxiee on 15-6-16.
 *
 * TODO: need an AddLabelApi
 */
public class AddLabelsApi extends BaseDBApi{

    private ArrayList<String> mLabels;

    public AddLabelsApi(Context context,
                        ArrayList<String> labels) {
        super(context);
        mLabels = labels;
    }

    public AddLabelsApi(Context context,
                        String label) {
        super(context);
        mLabels = new ArrayList<>();
        mLabels.add(label);
    }

    public int insertLabel(String label) {
        ContentValues values = new ContentValues();
        values.put(LabelsTable.LABEL, label);
        return (int) add(LabelsTable.NAME, values);
    }

    public ArrayList<Integer> exec() {
        ArrayList<Integer> ret = new ArrayList<>();
        for (String label: mLabels) {
            int id = new HasLabelApi(mContext, label).exec();
            if (id == HasLabelApi.NOT_FOUND) { // insert into DB
                int insertedId = insertLabel(label);
                ret.add(insertedId);
            } else {
                ret.add(id);
            }
        }
        return ret;
    }
}

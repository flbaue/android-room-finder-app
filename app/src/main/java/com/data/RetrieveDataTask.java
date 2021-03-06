/*
 * Copyright 2014 eSailors IT Solutions GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.Constants;
import com.events.DataUpdatedEvent;
import com.squareup.otto.Bus;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.util.LinkedList;

import javax.inject.Inject;

import timber.log.Timber;

public class RetrieveDataTask extends AsyncTask {

    private final Bus eventsBus;

    @Inject
    public RetrieveDataTask(Bus eventsBus) {
        this.eventsBus = eventsBus;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object result) {
        eventsBus.post(new DataUpdatedEvent((Boolean) result));
    }

    protected Boolean doInBackground(Object... params) {

        try {
            Timber.d("check for updates...");
            DataProvider dataProvider = new DataProvider();
            boolean isAlreadyStored = false;
            isAlreadyStored = dataProvider.isDataStored((Activity) params[0]);

            if (!isAlreadyStored) {
                Timber.d("no data available, so retrieve data...");
                HttpResponse response = new DefaultHttpClient().execute(new HttpGet(Constants.NAME_ROOM_URL));
                BufferedInputStream in = new BufferedInputStream(response.getEntity().getContent());

                LinkedList<Byte> bytes = new LinkedList<>();
                int b;
                while ((b = in.read()) != -1) {
                    bytes.add((byte) b);
                }

                byte[] decryptedData = DecryptionService.decryptData(bytes, (String) params[1]);
                String data = new String(decryptedData);
                for (String l : data.split("\n")) {
                    dataProvider.addNewEntry(l, (Activity) params[0]);
                }
            }
            SharedPreferences spData = ((Activity) params[0]).getApplicationContext().getSharedPreferences("Data", 0);
            int remoteVersion = spData.getInt("DataVersionRemote", 0);
            spData.edit().putInt("DataVersion", remoteVersion).apply();

            Timber.d("... update success");
            return true;
        } catch (Exception e) {
            Timber.e(e, "... update failure " + e.getMessage());
        }
        return false;

    }


}


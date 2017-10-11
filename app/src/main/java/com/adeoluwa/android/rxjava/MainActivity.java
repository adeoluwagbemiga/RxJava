package com.adeoluwa.android.rxjava;


import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    private Subscription subscription;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.tv_textView_holder);

        subscription = getGistObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Gist>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(Gist gist) {
                        StringBuilder sb = new StringBuilder();
                        for(Map.Entry<String, GistRepo> entry : gist.repos.entrySet()){
                            sb.append(entry.getKey());
                            sb.append("-");
                            sb.append(entry.getValue().content.length());
                            sb.append("\n");
                        }
                        mTextView.setText(sb.toString());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }

    public Observable<Gist> getGistObservable(){
        return Observable.defer(new Func0<Observable<Gist>>(){

            @Override
            public Observable<Gist> call() {
                try{
                    return Observable.just(getGist());
                }catch(IOException ex){
                    return null;
                }
            }
        });
    }
    @Nullable
    private Gist getGist() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://gist.github.com/adeoluwagbemiga/59488f02db24ebd83450289e0b0f9ff7")
                .build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()){
            Gist gist = new Gson().fromJson(response.body().charStream(), Gist.class);
            return gist;
        }
        return null;
    }
}

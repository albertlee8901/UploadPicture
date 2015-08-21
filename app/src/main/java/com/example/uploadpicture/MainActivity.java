package com.example.uploadpicture;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    ImageView ivImageToUpload, ivDownloadedImage;
    EditText etUploadImageName, etDownloadImageName;

    private static final int IMAGE_PICKED = 1;
    public static final String SERVER_ADDRESS = "http://770514970.site40.net/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivImageToUpload = (ImageView) findViewById(R.id.ivImageToUpload);
        ivDownloadedImage = (ImageView) findViewById(R.id.ivDownloadedImage);
        etUploadImageName = (EditText) findViewById(R.id.etUploadImageName);
        etDownloadImageName = (EditText) findViewById(R.id.etDownloadImageName);

    }

    public void chooseImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICKED);
    }

    public void uploadImage(View view) {
        Bitmap bitmap = ((BitmapDrawable) ivImageToUpload.getDrawable()).getBitmap();
        new UploadImageAsyncTask(bitmap, etUploadImageName.getText().toString()).execute();
    }

    public void downloadImage(View view) {
        new DownloadImageAsyncTask(etDownloadImageName.getText().toString()).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_PICKED && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            ivImageToUpload.setImageURI(imageUri);
        }
    }

    public class UploadImageAsyncTask extends AsyncTask<Void, Void, Void> {

        Bitmap imageToUpload;
        String name;

        public UploadImageAsyncTask(Bitmap imageToUpload, String name) {
            this.imageToUpload = imageToUpload;
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageToUpload.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("name", name));
            dataToSend.add(new BasicNameValuePair("image", encodedImage));

            HttpParams httpParams = getHttpRequestParams();

            HttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(SERVER_ADDRESS + "SaveImage.php");

            try{
                httpPost.setEntity(new UrlEncodedFormEntity(dataToSend));
                httpClient.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_LONG).show();
        }
    }

    private HttpParams getHttpRequestParams() {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 1000 * 30);
        HttpConnectionParams.setSoTimeout(httpParams, 1000 * 30);
        return httpParams;
    }

    public class DownloadImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {
        String name;

        public DownloadImageAsyncTask(String name) {
            this.name = name;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            String url = SERVER_ADDRESS + "pictures/" + name + ".JPG";

            try{
                URLConnection urlConnection = new URL(url).openConnection();
                urlConnection.setConnectTimeout(1000 * 30);
                urlConnection.setReadTimeout(1000 * 30);

                return BitmapFactory.decodeStream((InputStream) urlConnection.getContent(), null, null);
            }catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap != null) {
                ivDownloadedImage.setImageBitmap(bitmap);
            }
        }
    }







}

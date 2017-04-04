package sky.alien.korenovskguide;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.util.Util;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by NeoGhost on 04.04.2017.
 */

public class ChatActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ClickListenerChatFirebase{

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQEST = 3;

    static final String TAG = ChatActivity.class.getSimpleName();
    static final String CHAT_REFERENCE = "chatmodel";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatebaseRefence mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //Model
    private UserModel userModel;

    //UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageView btSendMessage, btEmoji;
    private EmojiconEditText edMessage;
    private View contentRoot;
    private EmojIconActions emojIcon;

    //File
    private File filePathImageCamera;

    //Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[]PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        if (!Util.verificaConexao(this)){
            Util.initToast(this,"Aga");
            finish();
        }else{
            bindViews();
            verificaUsuarioLogado();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);
        if(requestCode == IMAGE_GALLERY_REQUEST){
            if(resultCode == RESULT_OK){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    sendFileFirebase(storageRef, selectedImageUri);
                }else{
                    //Uri null
                }
            }

        }else if (requestCode == IMAGE_CAMERA_REQUEST){
            if(resultCode == RESULT_OK){
                if(filePathImageCamera != null && filePathImageCamera.exists()){
                    StorageReference imageCameraRef = storageRef.child(filePathImageCamera.getName()+ "_camera");
                    sendFileFirebase(imageCameraRef, filePathImageCamera);
                }else{
                    //null
                }
            }
        }
    }

    @Override
    public boolean onCrateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.sendPhoto:
                verifyStoragePermissions();
                break;
            case R.id.sendPhotoGallery:
                photoGalleryIntent();
                break;
            case R.id.sendLocation:
                locationPlacesIntent();
                break;
            case R.id.sing_out:
                signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnecionResult connecionResult){
        Log.d(TAG,"onConnectionFailed: " + connecionResult);
        Util.initToast(this, "Google Play Services error. ");
    }
    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.buttonMessage:
                sendMessageFirebase();
                break;
        }
    }
    @Override
    public void clickImageChat(View view, int position,String nameUser,String urlPhotoUser,String urlPhotoClick) {
        Intent intent = new Intent(this, FullScreenImageActivity.class);
        intent.putExtra("nameUser", nameUser);
        intent.putExtra("urlPhotoUser", urlPhotoUser);
        intent.putExtra("urlPhotoClick", urlPhotoClick);
        startActivity(intent);

    }
    @Override
    public void clickImageMapChat(View view, int position,String latitude,String longitude) {
        String uri = String.format("geo:%s, %s?z=17&q=%s, %s",latitude, longitude, latitude,longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }
    private void sendFileFirebase(StorageReference storageReference,final Uri file){
        if(storageReference != null){
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date().toString());
            StorageReference imageGalleryRef = storageReference.child(name+"_gallery");
            UploadTask uploadTask = imageGalleryRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,"onFailure sendFileFirebase "+e.getMessage());

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i(TAG,"onSuccess sendFileFirebase");
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    FileModel fileModel = new FileModel("img",downloadUrl.toString(),name,"");
                    ChatModel chatModel = new ChatModel(userModel,"", Calendar.getInstance().getTime().getTime()+"",fileModel);
                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                }
            });

        }else{
    }
    private void sendFileFirebase(StorageReference storageReference, final File file){
      if(storageReference != null){
          Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                  BuildConfig.APPLICATION_ID + ".provider", file);
          UploadTask uploadTask = storageReference.putFile(photoURI);
          uploadTask.addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                  Log.e(TAG, "onFailure sendFileFirebase "+e.getMessage());
              }
          }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  Log.e(TAG, "onSuccess sendFileFirebase");
                  Uri downloadUrl = taskSnapshot.getDownloadUrl();
                  FileModel fileModel = new FileModel("img",downloadUrl.toString(),file.getName(),file.length()+"");
                  ChatModel chatModel = new ChatModel(userModel,"",Calendar.getInstance().getTime().getTime()+"",fileModel);
                  mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
              }
          });
      }else{
          //NULL KEKA :)
      }
    }
    private void locationPlacesIntent{
        try{
            PlacePicker.IntentBuilder builder = new PlacePicker.InetentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQEST);
        }catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        }
    }
    private void photoCameraIntent(){
        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto+"camera.jpg");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                filePathImageCamera);
        it.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
        startActivityForResult(it, IMAGE_CAMERA_REQUEST);
    }
    private void photoGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);

    private void sendMessageFirebase() {
        ChatModel model = new ChatModel(userModel, edMessage.getText().toString(), Calendar.getInstance().getTime().getTime() + "", null);
        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
        edMessage.setText(null);

    }

    private void lerMessagensFirebase() {
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference.child(CHAT_REFERENCE), userModel.getName(), this);
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    rvListMessage.scrollToPosition(positionStart);
                }
            }
        });
        rvListMessage.setLayoutManager(mLinearLayoutManager);
        rvListMessage.setAdapter(firebaseAdapter);
    }

    private void verificaUsuarioLogado() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            userModel = new UserModel(mFirebaseUser.getDisplayName(), mFirebaseUser.getPhotoUrl().toString(), mFirebaseUser.getUid());
            lerMessagensFirebase();

        }
    }

    private void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void verifyStoragePermissions() {
        int permission = ActivityCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    ChatActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            photoCameraIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    photoCameraIntent();
                }
                break;
        }
    }
}

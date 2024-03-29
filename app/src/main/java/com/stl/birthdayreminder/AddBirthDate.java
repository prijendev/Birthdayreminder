package com.stl.birthdayreminder;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class AddBirthDate extends AppCompatActivity
{
    DatabaseReference databaseReference;


    FloatingActionButton btn;
    ProgressDialog progressDialog;
    public String u_bDate, u_name,u_contact,u_email;
    EditText bDate,name,email,contact;
    DatePickerDialog picker;
    Uri imgUri;
    byte[] Imagebyte;
    ImageView profile;
    Button save;
    String profile_img="";

    Toolbar toolbar;
    StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_birth_date);

        databaseReference=FirebaseDatabase.getInstance().getReference("User");
        databaseReference.keepSynced(true);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);



        profile=(ImageView)findViewById(R.id.profile);
        name=(EditText)findViewById(R.id.name);
        contact=(EditText)findViewById(R.id.contact);
        email=(EditText)findViewById(R.id.email);
        bDate=(EditText) findViewById(R.id.bdate);
        bDate.setInputType(InputType.TYPE_NULL);
        save=(Button)findViewById(R.id.save);


        bDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(AddBirthDate.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                            {
                                bDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });


        btn=(FloatingActionButton) findViewById(R.id.fab);

        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(ContextCompat.checkSelfPermission(AddBirthDate.this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    SelectImage();
                }
                else
                {
                    ActivityCompat.requestPermissions(AddBirthDate.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(imgUri != null)
                UploadData();


            }
        });



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            SelectImage();

        }
        else
            Toast.makeText(this,"Please provide permission",Toast.LENGTH_LONG).show();

    }

    public void SelectImage()
    {
        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,86);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 86 && data!= null )
        {
            imgUri = data.getData();

            try
            {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                // In case you want to compress your image, here it's at 40%
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();


                profile_img=Base64.encodeToString(byteArray, Base64.DEFAULT);



                String imageDataBytes = profile_img.substring(profile_img.indexOf(",")+1);

                InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));

                Bitmap bitmap1 = BitmapFactory.decodeStream(stream);


                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);

                //ByteArrayOutputStream baos = new ByteArrayOutputStream();

                //bitmap.compress(Bitmap.CompressFormat.JPEG,10,baos);//to compress image
                //Imagebyte=baos.toByteArray();

                profile.setImageBitmap(bitmap1);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            //name.setText(data.getData().getPath()+"");
        }
        else
        {
            Toast.makeText(this,"failed to load",Toast.LENGTH_LONG).show();
        }
    }


    public void UploadData()
    {


        u_bDate = bDate.getText().toString();
        u_name = name.getText().toString();
        u_contact = contact.getText().toString();
        u_email = email.getText().toString();


        //User user = new User(u_name,u_bDate,u_contact,u_email);
        DatabaseHelper db = new DatabaseHelper(this);
        if(db.insertData(u_name,u_email,u_bDate,profile_img,u_contact))
        {
            //Toast.makeText(this,"Successfullly made"+u_bDate+u_contact,Toast.LENGTH_LONG).show();
            Intent intent  = new Intent(AddBirthDate.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
            Toast.makeText(this,"not success"+u_bDate+u_contact,Toast.LENGTH_LONG).show();
    }
}

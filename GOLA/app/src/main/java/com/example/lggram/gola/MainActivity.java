package com.example.lggram.gola;

import android.database.Cursor;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;


import android.net.Uri;

import android.os.Bundle;

import android.os.Environment;

import android.provider.MediaStore;

import android.provider.MediaStore;

import android.util.Log;
import android.view.View;

import android.app.Activity;

import android.app.AlertDialog;

import android.app.Dialog;

import android.content.Intent;

import android.graphics.Bitmap;

import android.graphics.BitmapFactory;

import android.view.Menu;

import android.widget.Button;
import android.widget.ImageView;

import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class MainActivity extends Activity implements View.OnClickListener{
//�궗吏꾩쑝濡� �쟾�넚�떆 �릺�룎�젮 諛쏆쓣 踰덊샇
    static int REQUEST_PICTURE=1;
//�븿踰붿쑝濡� �쟾�넚�떆 �룎�젮諛쏆쓣 踰덊샇
    static int REQUEST_PHOTO_ALBUM=2;
//泥ル쾲吏� �씠誘몄� �븘�씠肄� �깦�뵆 �씠�떎.
    static String SAMPLEIMG="ic_launcher.png";
    ImageView iv;
    Dialog dialog;
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;
    Button uploadBtn;
    File fileToUpload ;
    private Uri imageUri;
    String imageName = "newTest.jpg";
    String filename=null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
       //�뿬湲곗뿉 �씪�떒 湲곕낯�쟻�씤 �씠誘몄��뙆�씪 �븯�굹瑜� 媛��졇�삩�떎.
        iv=(ImageView) findViewById(R.id.imgView);
        uploadBtn = (Button) findViewById(R.id.uploadBtn);

        //媛��졇�삱 �궗吏꾩쓽 �씠由꾩쓣 �젙�븳�떎.
        findViewById(R.id.getCustom).setOnClickListener(this);
       uploadBtn.setOnClickListener(this);

        // Amazon Cognito �씤利� 怨듦툒�옄瑜� 珥덇린�솕�빀�땲�떎
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:f0c50d3f-bbf0*****************", // �옄寃� 利앸챸 �� ID
                Regions.AP_NORTHEAST_2 // 由ъ쟾
        );

        s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");

        transferUtility = new TransferUtility(s3, getApplicationContext());


    }

    @Override
    public void onClick(View v){
        //泥ル쾲吏몃줈 �궗吏꾧��졇�삤湲곕�� �겢由��븯硫� �삉�떎瑜� �젅�씠�븘�썐寃껋쓣 �떎�씠�뼱濡쒓렇濡� 異쒕젰�빐�꽌
        //�꽑�깮�븯寃뚮걫 �븯�옄 !!!!
        if(v.getId()==R.id.getCustom){
        //�떎�씠�뼱濡쒓렇瑜� 癒쇱�留뚮뱾�뼱�궦�떎.
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            //�씠怨녹뿉 留뚮뱶�뒗 �떎�씠�뼱濡쒓렇�쓽 layout �쓣 �젙�븳�떎.
            View customLayout=View.inflate(this,R.layout.dialog,null);
            //�쁽�옱 鍮뚮뜑�뿉 �슦由ш�留뚮뱺 �떎�씠�뼱濡쒓렇 �젅�씠�븘�썐酉곕�� 異붽��븯�룄濡앺븯�옄!!
            builder.setView(customLayout);
            //�떎�씠�뼱濡쒓렇�쓽 踰꾪듉�뿉  移대찓�씪�� �궗吏꾩븿踰붿쓽 �겢由� 湲곕뒫�쓣 �꽔�뼱二쇱옄.
            customLayout.findViewById(R.id.camera).setOnClickListener(this);
            customLayout.findViewById(R.id.photoAlbum).setOnClickListener(this);
            //吏�湲덇퉴吏� 留뚮뱺 builder瑜� �깮�꽦�븯怨�, �쓣�슦�옄.!!!

            dialog=builder.create();
            dialog.show();
        }else if(v.getId()==R.id.camera){
            //移대찓�씪踰꾪듉�씤寃쎌슦,�씪�떒 �떎�씠�뼱濡쒓렇瑜� �걚怨� �궗吏꾩쓣 李띾뒗 �븿�닔瑜� 遺덈윭�삤�옄
            dialog.dismiss();
            takePicture();
        }else if(v.getId()==R.id.photoAlbum){
            //�씠寃쎌슦�뿭�떆 �떎�씠�뼱濡쒓렇瑜� �걚怨� �븿踰붿쓣 遺덈윭�삤�뒗 �븿�닔瑜� 遺덈윭�삤�옄!!
            dialog.dismiss();
            photoAlbum();

        }else if(v.getId()==R.id.uploadBtn){
            TransferObserver observer = transferUtility.upload(
                    "golabucket",
                    filename,
                    fileToUpload
            );
        }

    }



    void takePicture(){
//�궗吏꾩쓣 李띾뒗 �씤�뀗�듃瑜� 媛��졇�삩�떎. 洹몄씤�뀗�듃�뒗 MediaStore�뿉�엳�뒗
//ACTION_IMAGE_CAPTURE瑜� �솢�슜�빐�꽌 媛��졇�삩�떎.
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//洹명썑 �뙆�씪�쓣 吏��젙�빐�빞�븯�뒗�뜲 File�쓽 �븵遺�遺� 留ㅺ컻蹂��닔�뿉�뒗 �뙆�씪�쓽 �젅��寃쎈줈瑜� 遺숈뿬�빞

//�븳�떎. 洹몃윭�굹 吏곸젒 寃쎈줈瑜� �뜥�꽔�쑝硫� sdcard�젒洹쇱씠 �븞�릺誘�濡� ,

//Environment.getExternalStorageDirectory()濡� �젒洹쇳빐�꽌 寃쎈줈瑜� 媛��졇�삤怨� �몢踰덉㎏

//留ㅺ컻 蹂��닔�뿉 �뙆�씪�씠由꾩쓣 �꽔�뼱�꽌 �솢�슜 �븯�룄濡앺븯�옄!!

        File file=new File(Environment.getExternalStorageDirectory(),SAMPLEIMG);

//洹몃떎�쓬�뿉 �궗吏꾩쓣 李띿쓣�� 洹명뙆�씪�쓣 �쁽�옱 �슦由ш� 媛뽮퀬�엳�뒗 SAMPLEIMG濡� ���옣�빐�빞

//�븳�떎. 洹몃옒�꽌 寃쎈줈瑜� putExtra瑜� �씠�슜�빐�꽌 �꽔�룄濡� �븳�떎. �뙆�씪�삎�깭濡� 留먯씠�떎.

//洹몃━怨� �떎�젣濡� �씠�뙆�씪�씠 媛�由ы궎�뒗 寃쎈줈�뒗 /mnt/sdcard/ic_launcher)

        intent.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(file));

//洹몃읆�씠�젣�궗吏꾩컢湲� �씤�뀗�듃瑜� 遺덈윭�삩�떎.

        startActivityForResult(intent,REQUEST_PICTURE);

    }

    void photoAlbum(){
        //���옣�맂 �궗吏꾩쓣 遺덈윭�삤�뒗 �븿�닔�씠�떎. 利됱븿踰붿뿉�엳�뒗寃껋씤�뜲 �씤�뀗�듃�뒗 ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        //媛ㅻ윭由щ━�쓽 湲곕낯�꽕�젙 �빐二쇰룄濡앺븯�옄!�븘�옒�뒗�씠誘몄���洹멸꼍濡쒕�� �몴以����엯�쑝濡� �꽕�젙�븳�떎.
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        //�궗吏꾩씠 ���옣�맂�쐞移�(sdcard)�뿉 �뜲�씠�꽣媛� �엲�떎怨� 吏��젙
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,REQUEST_PHOTO_ALBUM);
    }

    Bitmap loadPicture(){
        //�궗吏꾩컢�� 寃껋쓣 濡쒕뱶 �빐�삤�뒗�뜲 �궗�씠利덈�� 議곗젅�븯�룄濡앺븯�옄!!�씪�떒 �뙆�씪�쓣 媛��졇�삤怨�
        File file=new File(Environment.getExternalStorageDirectory(),SAMPLEIMG);
        //�쁽�옱�궗吏꾩컢�� 寃껋쓣 議곗젅�븯援ъ씠�빐�꽌 議곗젅�븯�뒗 �겢�옒�뒪瑜� 留뚮뱾�옄.
        BitmapFactory.Options options=new BitmapFactory.Options();

        //�씠�젣 �궗�씠利덈�� �꽕�젙�븳�떎.
        options.inSampleSize=4;
        //洹명썑�뿉 �궗吏� 議곗젙�븳寃껋쓣 �떎�떆 �룎�젮蹂대궦�떎.
        return BitmapFactory.decodeFile(file.getAbsolutePath(),options);
    }

    private String getRealPathFromURI(Uri contentURI) {
        String thePath = "no-path-found";
        String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(contentURI, filePathColumn, null, null, null);
        if(cursor.moveToFirst()){
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            thePath = cursor.getString(columnIndex);
        }
        Toast.makeText(this, "寃쎈줈:" + thePath, Toast.LENGTH_LONG).show();
        cursor.close();
        return  thePath;
    }

    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){

            if(requestCode==REQUEST_PICTURE){

                //�궗吏꾩쓣 李띿�寃쎌슦 洹몄궗吏꾩쓣 濡쒕뱶�빐�삩�떎.

                iv.setImageBitmap(loadPicture());
            }

            if(requestCode==REQUEST_PHOTO_ALBUM){
                //�븿踰붿뿉�꽌 �샇異쒗븳寃쎌슦 data�뒗 �씠�쟾�씤�뀗�듃(�궗吏꾧갇�윭由�)�뿉�꽌 �꽑�깮�븳 �쁺�뿭�쓣 媛��졇�삤寃뚮맂�떎.
                imageUri=data.getData();
                iv.setImageURI(data.getData());
                 //aws uri img to upload
                imageName = (String) iv.getTag();
                Toast.makeText(this, "�씠誘몄��궡�엫"+imageName, Toast.LENGTH_LONG).show();
                imageUri = data.getData();
                fileToUpload = new File(Environment.getExternalStorageDirectory() + "/dcim/camera/" +getRealPathFromURI(imageUri));
                if (fileToUpload == null) {
                    Toast.makeText(this, "�뙆�씪�쓣 遺덈윭�삤吏� 紐삵뻽�뒿�땲�떎.", Toast.LENGTH_LONG).show();
                     //to make sure that file is not emapty or null
                    return;
                }else {
                    filename = fileToUpload.getName();
                    Toast.makeText(this, filename + " �뙆�씪�깮�꽦", Toast.LENGTH_LONG).show();
                }
            }


        }



    }
        }

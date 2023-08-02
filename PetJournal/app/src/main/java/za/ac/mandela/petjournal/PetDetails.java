package za.ac.mandela.petjournal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PetDetails extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageButton img;
    String currentPhotoPath="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_details);

        //get reference to the date edittext
        EditText edit=findViewById(R.id.editText);
        //set the date on the edittext to today's date
        String dateS = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        edit.setText(dateS);
        edit.setFocusable(false);
        //get reference to the imagebutton used to oppen the dialog for cchoosing date
        ImageButton dateBtn=findViewById(R.id.datePick);
        dateBtn.setOnClickListener(txtV->doCalendar(edit));

        //get reference to the view for the pets name
        EditText petName=findViewById(R.id.editText2);

        //get reference to the imaagebutton which shows the pet image
        ImageButton imageButton=findViewById(R.id.petImage);
        img=imageButton;

        //read stored data if possible
        readData(edit,petName,img);

        //the app requests permission to use the camera if it doesnt have permission already,if it does,it starts a camera intent
        imageButton.setOnClickListener(cl->{
            if (ContextCompat.checkSelfPermission(PetDetails.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PetDetails.this, new String[]{Manifest.permission.CAMERA},1);
            }
            else {
                dispatchTakePictureIntent();
            }
        });

        FloatingActionButton savedetails=findViewById(R.id.saveDetails);
        savedetails.setOnClickListener(saveBtn->saveDetails(petName,edit));
    }

    /**
     * save pet details to a file
     * @param x
     * @param y
     */
    public  void saveDetails(EditText x,EditText y){
        //create file if it doesnt exist already
        File file = new File(PetDetails.this.getFilesDir(), "userdata");
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            File theSaveFile = new File(file, "myPet");
            FileWriter writer = new FileWriter(theSaveFile);

            String data;
            if(currentPhotoPath=="")
                data=x.getText()+","+y.getText();
            else
                data=x.getText()+","+y.getText()+","+currentPhotoPath;

            writer.append(data);
            writer.flush();
            writer.close();
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.petSaved, Snackbar.LENGTH_LONG);
            snackbar.show();
        } catch (Exception e) {e.printStackTrace(); }

        finish();
    }
    public void doCalendar(EditText txt){
        //get the instance of calendar
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        //start a dialog for choosing the date
        DatePickerDialog chooseDate = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        //set the text of the edit text to the date picked
                        txt.setText(day + "-" + (month + 1) + "-" + year);
                    }
                }, year, month, day);
        chooseDate.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
            setPic();
        }
    }

    /**
     * read data from a file for the date of birth,the name of the pet the picture of the pet where relavant
     * @param x
     * @param y
     * @param z
     */
    private void readData(EditText x,EditText y,ImageButton z){
        try {
            //if the directory doesn't exist create it
            File file = new File(PetDetails.this.getFilesDir(), "userdata");
            if (!file.exists()) {
                file.mkdir();
            }

            File pet = new File(file, "myPet");
            FileReader reader = new FileReader(pet);
            BufferedReader reader1 = new BufferedReader(reader);
            String data=reader1.readLine();
            String[] dataArr=data.split(",");
            x.setText(dataArr[0]);
            y.setText(dataArr[1]);

            //if a picture of the pet was taken already,view it on the image button
            //if no picture was taken,the default avatar will be shown
            if(dataArr.length>=3) {
                currentPhotoPath = dataArr[2];
                setPic();
                reader1.close();
            }
            else {
                Resources res = getResources();
                String mDrawableName = "dog.png";
                int resID = res.getIdentifier(mDrawableName , "drawable", getPackageName());
                Drawable drawable= ResourcesCompat.getDrawable(getResources(),resID, null);
                img.setImageDrawable(drawable);
            }
        }
        catch (Exception ex){ex.printStackTrace();}
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    private void galleryAddPic() {
        String currentPhotoPath="";
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    private void setPic() {
        // Get the dimensions of the View
        int targetW = 919;
        int targetH = 919;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        img.setImageBitmap(bitmap);
    }
}

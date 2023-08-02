package za.ac.mandela.petjournal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JournalEntry extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageButton img;
    String currentPhotoPath="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_entry);

        Intent intent=getIntent();
        //get the entry which was sent with the intent
        Entry entry=(Entry)intent.getExtras().get("entry");

        //get reference to the editText for date
        EditText date=findViewById(R.id.entryDate);


        //get current date and set the text of the textview to the current date
        String dateS = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        date.setText(dateS);
        date.setFocusable(false);

        //get reference to the spinner which is used to choose entry type
        Spinner entryType=findViewById(R.id.entryType);

        //get reference to the picture displayed
        ImageButton journalPic=findViewById(R.id.journalPic);
        img=journalPic;
        entryTypes(entryType,journalPic,entry);

        //get reference to the floating button for saving
        FloatingActionButton saveEntry=findViewById(R.id.saveEntry);



        //get reference to the entrytext textview
        EditText entryText=findViewById(R.id.entryTxt);

        int index=(int) intent.getExtras().get("index");
        sendResult(intent,saveEntry,dateS,entryText,entryType,index);

        //if the image of the parsed entry is not null,set value of photopath
        if(entry.getImage()!=null)
        {
            currentPhotoPath=entry.getImage();
        }
        setdata(entry,entryText);

        //when the image is clicked,the app will request to use the camera permission
        //if permission was already granted the app will start a camera intent
        journalPic.setOnClickListener(cl->{
            if (ContextCompat.checkSelfPermission(JournalEntry.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(JournalEntry.this, new String[]{Manifest.permission.CAMERA},1);
            }
            else {
                dispatchTakePictureIntent();
            }
        });

        //reference to button for opening calendar
        ImageButton choosedate=findViewById(R.id.entryDateCalendar);
        choosedate.setOnClickListener(cd->doCalendar(date));
    }

    /**
     * If the entry doesnt have an image the method returns
     * if the image is a picture the setpic method is called
     * if both of the conditions aren't met,the picture is set according to the entrytype
     * @param entry entry sent through
     * @param entryText edittext for the entrytext
     */
    private void setdata(Entry entry,EditText entryText){

        if(entry.getImage()==null){
            return;
        }
        if(entry.getImage().length()>20){
            setPic();
        }
        else {
            Resources res = this.getResources();
            String mDrawableName = (entry.getEntryType().toLowerCase()).replaceAll("\\s", "");
            int resID = res.getIdentifier(mDrawableName, "drawable", this.getPackageName());
            Drawable drawable = ResourcesCompat.getDrawable(this.getResources(), resID, null);
            img.setImageDrawable(drawable);
        }

        entryText.setText(entry.getEntryText());
    }

    /**
     * send back information to the activity that started the intend after it has done its processes
     * @param intent
     * @param save
     * @param date
     * @param entryText
     * @param entryType
     * @param index
     */
    public  void sendResult(Intent intent,FloatingActionButton save,String date,EditText entryText,Spinner entryType,int index){
        if(intent!=null) {
            save.setOnClickListener(view -> {
                Entry newone = new Entry(currentPhotoPath, date, entryText.getText().toString(), entryType.getSelectedItem().toString());
                Intent result = new Intent();
                result.putExtra("index", index);
                result.putExtra("entry", newone);
                setResult(Activity.RESULT_OK, result);
                finish();

            });
        }
    }

    /**
     * sets the values of the spinner dropdown using resources
     * @param spinner
     * @param journalPic
     * @param entry
     */
    private void entryTypes(Spinner spinner,ImageView journalPic,Entry entry) {

        ArrayList<String> types = new ArrayList<>();
        types.add(getResources().getString(R.string.vetvisit));
        types.add(getResources().getString(R.string.medication));
        types.add(getResources().getString(R.string.appointment));
        types.add(getResources().getString(R.string.selfie));
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(typeAdapter);

        if(entry.getEntryType()!=null) {
            String compareValue = entry.getEntryType();
            spinner.setSelection(((ArrayAdapter<String>)spinner.getAdapter()).getPosition(compareValue));
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if no picture was taken yet,the image shown will correspond to the entry type
                if(currentPhotoPath.length()<=20) {
                    Resources res = getResources();
                    String val = parent.getItemAtPosition(position).toString();
                    String mDrawableName = (val.toLowerCase()).replaceAll("\\s", "");
                    int resID = res.getIdentifier(mDrawableName, "drawable", getPackageName());
                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), resID, null);
                    journalPic.setImageDrawable(drawable);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
            setPic();
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

    /**
     * opens a calendar choosing dialog and allows the user to pick dates
     * @param txt
     */
    public void doCalendar(EditText txt){
        //get instance of calendar
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog chooseDate = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                //set the text of the date editText to the chosen date
                txt.setText(day + "-" + (month + 1) + "-" + year);
            }
        }, year, month, day);
        chooseDate.show();
    }
}

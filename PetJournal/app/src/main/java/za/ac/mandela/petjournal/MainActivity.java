package za.ac.mandela.petjournal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String FILE_NAME = "jEntryData.txt";
    private List<Entry> entries;
    private PetJournalAdapter adapter;
    private final int REQ_ADD=2020;
    public  static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check whether user has save or used the app before
        checkForExistFIle();

        //get reference to question mark float button
        FloatingActionButton qmark=findViewById(R.id.qMark);
        qmark.setOnClickListener(qbutt->qMarkClicked());


        //get reference to add float button
        FloatingActionButton add=findViewById(R.id.addEntry);
        add.setOnClickListener(aButton->addClicked());


        entries=new ArrayList<>();
        context=this;

        // Get reference to Recycler View
        RecyclerView lstPeople = findViewById(R.id.entriesRecycler);

        // Create an adapter to map single data element -> single item view
        adapter = new PetJournalAdapter(entries,this);

        //readData();

        // How will the individual items be laid out in the collection view?
        RecyclerView.LayoutManager layoutManager;
        layoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);

        // Assign adapter to "list" viewer
        lstPeople.setLayoutManager(layoutManager);
        lstPeople.setAdapter(adapter);

        // Set extra parameters if needed.
        // This decorator adds extra spacing around all items in the recycle view.
        lstPeople.addItemDecoration(new EqualSpaceItemDecoration(18));
        // Attach item selected event handler
        adapter.setOnClickListener( view -> {

            PetJournalAdapter.EntryViewHolder viewHolder =
                    (PetJournalAdapter.EntryViewHolder) lstPeople.findContainingViewHolder(view);


            // Get the person from the view holder.
            Entry journalEntry = viewHolder.JournalEntry;
            Intent intent=new Intent(this,JournalEntry.class);
            intent.putExtra("index",adapter.getIndex(journalEntry));
            intent.putExtra("entry",journalEntry);
            startActivityForResult(intent,REQ_ADD);
        });

        //load existing user data to recycle view
        loadentryData();
    }

    /**
     * starts the pet details activity then the ? floating button is clicked
     */
    private void  qMarkClicked(){
        Intent intent=new Intent(this,PetDetails.class);
        startActivity(intent);
    }

    /**
     * starts th journal entry activity when the + floating button is clicked
     */
    private void addClicked(){
        Intent intent=new Intent(this,JournalEntry.class);
        intent.putExtra("index",-1);
        intent.putExtra("entry",new Entry(null,null,null,null));
        startActivityForResult(intent,REQ_ADD);
    }
    @Override
    protected void onActivityResult(int reqCode,int resCode,Intent data){
        Calendar theTime = Calendar.getInstance();


        //check if the result was successful and is its the correct request code
        if(reqCode==REQ_ADD&&resCode== Activity.RESULT_OK){
            //get the entry that was created on the sender activity and the index of the entry
            Entry c=(Entry) data.getExtras().getSerializable("entry");
            int index=(int)data.getExtras().get("index");
            if(c!=null&&adapter!=null&&index<0){
                //this code is for adding a brand new entry
                c.setDate(c.getDate()+" "+theTime.get(Calendar.HOUR_OF_DAY)+":"+theTime.get(Calendar.MINUTE));
                adapter.add(c);
                adapter.notifyDataSetChanged();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.saved, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            else if(adapter!=null&&index>=0){
                //thi code is for editing an entry
                Entry x=adapter.get(index);
                x.setEntryType(c.getEntryType());
                x.setImage(c.getImage());
                x.setEntryText(c.getEntryText());
                c.setDate(c.getDate()+" "+theTime.get(Calendar.HOUR_OF_DAY)+":"+theTime.get(Calendar.MINUTE));
                adapter.notifyDataSetChanged();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.editJournal, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
        saveEntryData();

    }

    /**
     * uses the fileoutputsptream to write the entries to a text file
     */
    private void saveEntryData()  {
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            boolean count = true;
            for (int x = 0; x < adapter.getItemCount(); x++) {
                Entry entry = adapter.get(x);
                if (count) {
                    outputStream.write((entry.getImage() + "," + entry.getDate() + "," + entry.getEntryText() + "," + entry.getEntryType()).getBytes());
                    count = false;
                } else {
                    outputStream.write(("\n" + entry.getImage() + "," + entry.getDate() + "," + entry.getEntryText() + "," + entry.getEntryType()).getBytes());
                }
            }
            outputStream.close();
        }
        catch (Exception io){
            io.printStackTrace();
        }
    }

    /**
     * uses the inputstreamreader to load previously saved data to the recyclerview using the adapter
     */
    private void loadentryData() {
        FileInputStream inputStream;
        try {
            inputStream = openFileInput(FILE_NAME);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String data;
            while ((data = br.readLine()) != null) {
                String[] dataArr=data.split(",");
                Entry newone=new Entry(dataArr[0],dataArr[1],dataArr[2],dataArr[3]);
                adapter.add(newone);
            }
        } catch (Exception io) {
            io.printStackTrace();
        }
    }

    /**
     * checks whether userdata file exists,if not the pet details activity is started since is the first time the user is using the app
     */
    private  void checkForExistFIle(){
        File file = new File(this.getFilesDir(), "userdata");
        if (!file.exists()) {
            Intent intent=new Intent(this,PetDetails.class);
            startActivity(intent);
        }
    }


}

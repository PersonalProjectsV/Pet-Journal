package za.ac.mandela.petjournal;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PetJournalAdapter extends RecyclerView.Adapter<PetJournalAdapter.EntryViewHolder> {
    // On Click Listener that will be propogated to each View Holder
    private View.OnClickListener onClickListener;


    /**
     * You need to define a View Holder, which will hold the data in its view for
     * a specific person.
     */
    public static class EntryViewHolder extends RecyclerView.ViewHolder {
        public TextView entryDate;
        public TextView entryText;
        public ImageView entryImage;
        public Entry JournalEntry;
        public ConstraintLayout container;
        Context context;

        public EntryViewHolder(@NonNull View view, Context context) {
            super(view);
            //get references to views in the card
            entryDate=view.findViewById(R.id.cardDate);
            entryText = view.findViewById(R.id.cardText);
            entryImage = view.findViewById(R.id.cardPic);
            container=view.findViewById(R.id.theContainer);
            this.context = context;

        }


        public void setContact(Entry journalEntry) {
            this.JournalEntry = journalEntry;

            entryDate.setText(journalEntry.getDate());
            entryText.setText(journalEntry.getEntryText());


            //set the background color of the card according to the entry type
            if(journalEntry.getEntryType().equals(R.string.appointment))
                container.setBackgroundColor(Color.parseColor("#ff4d4d"));
            else if (journalEntry.getEntryType().equals(R.string.medication))
                container.setBackgroundColor(Color.parseColor("#3cc732"));
            else if (journalEntry.getEntryType().equals(R.string.selfie))
                container.setBackgroundColor(Color.parseColor("#d1882e"));
            else if (journalEntry.getEntryType().equals(R.string.vetvisit))
                container.setBackgroundColor(Color.parseColor("#d130bc"));

            //if the entry has an image,the view is made larger
            if(journalEntry.getImage().length()>20){
                setPic(journalEntry.getImage(),entryImage);
            }
            else {
                ViewGroup.LayoutParams params =entryImage.getLayoutParams();
                params.height = 400;
                Resources res = context.getResources();
                String mDrawableName = (journalEntry.getEntryType().toLowerCase()).replaceAll("\\s", "");
                int resID = res.getIdentifier(mDrawableName, "drawable", context.getPackageName());
                Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), resID, null);
                entryImage.setImageDrawable(drawable);
            }
        }
        private void setPic(String currentPhotoPath, ImageView img) {
            // Get the dimensions of the View
            int targetW = 400;
            int targetH = 400;
            ViewGroup.LayoutParams params =img.getLayoutParams();
            img.setPadding(1,1,1,1);
            params.height = 600;
            // existing height is ok as is, no need to edit it
            img.setLayoutParams(params);
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


    // The collection of data that this adapter is currently displaying.
    private final List<Entry> journalEntries;
    private  Context context;
    public PetJournalAdapter(List<Entry> journalEntries, View.OnClickListener onClickListener) {
        this.journalEntries = journalEntries;
        this.onClickListener = onClickListener;
    }

    public PetJournalAdapter(List<Entry> journalEntries) {
        this.journalEntries =journalEntries ;
    }
    public PetJournalAdapter(List<Entry> journalEntries, Context context) {
        this.journalEntries = journalEntries;
        this.context=context;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This method is called by Android when it needs a brand new View to display
        // a single person. The ViewHolder will hold a reference to this newly created View.

        // Inflate (create) the UI scenegraph from the layout xml resource.
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.recyclerview_journalentrycard,
                        parent, false);

        // Put it into a View Holder object and return this.
        EntryViewHolder pvh = new EntryViewHolder(view,context);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        // Given the View Holder and an index to the View to be used to display it,
        // fill the data item's values into the view.

        // Get the data to be displayed
        Entry journalEntry = journalEntries.get(position);

        // Fill the data from person into the view.
        holder.setContact(journalEntry);

        holder.itemView.setOnClickListener(onClickListener);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    public void add(Entry journalEntry) {
        // Add the person and notify the view of changes.
        journalEntries.add(journalEntry);

        // In this case, specify WHICH person changed.
        notifyItemChanged(journalEntries.size() - 1);
        sortDate();

    }
    public int getIndex(Entry entry){
        for(int x=0;x<journalEntries.size();x++){
            if(entry==journalEntries.get(x))
                return x;
        }
        return -1;
    }
    public void remove(int position) {
        // Remove the person.
        journalEntries.remove(position);
        // Notify view of underlying data changed.
        notifyItemRemoved(position);
    }

    /**
     * sorts the journal entries according to the date using insertion sort
     */
    public void sortDate()
    {
        int x;
        int y;
        Entry value;
        for (x = 1; x < journalEntries.size(); x++) {
            value = journalEntries.get(x);
            y = x - 1;
            Entry temp=journalEntries.get(y);
            //get the date for each entry
            String forKey=value.getDate();
            String forTemp=temp.getDate();
            //earlier dates are prioritized
            while (y >= 0 && forTemp.compareTo(forKey)<0) {
                journalEntries.set(y+1,journalEntries.get(y));
                y = y - 1;
            }
            journalEntries.set(y + 1,value);
        }
        notifyDataSetChanged();
    }
    public Entry get(int position) {
        return journalEntries.get(position);
    }
}

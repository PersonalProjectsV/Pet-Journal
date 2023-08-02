package za.ac.mandela.petjournal;

import java.io.Serializable;

public class Entry implements Serializable {
    private String image;
    private String date;
    private String entryText;
    private String entryType;
    public Entry(String image,String date, String entryText,String entryType){
        this.image=image;
        this.date=date;
        this.entryText=entryText;
        this.entryType=entryType;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setEntryText(String entryText) {
        this.entryText = entryText;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getEntryType() {
        return entryType;
    }

    public String getDate() {
        return date;
    }

    public String getEntryText() {
        return entryText;
    }

    public String getImage() {
        return image;
    }
}

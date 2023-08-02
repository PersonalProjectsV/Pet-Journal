package za.ac.mandela.petjournal;

public class Pet {
    String image;
    String birthDate;
    String name;
    public Pet(String image,String birthDate,String name){
        this.name=name;
        this.image=image;
        this.birthDate=birthDate;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getName() {
        return name;
    }
}

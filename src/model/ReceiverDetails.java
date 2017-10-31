package model;

public class ReceiverDetails {
    String name;
    String image;
    String computerName = null;

    public ReceiverDetails(String name, String image, String computerName) {
        this.name = name;
        this.image = image;
        this.computerName = computerName;
    }

    //getters methods
    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getComputerName() { return computerName;}

    public String toString() {
        return name + " : " + image;
    }
}

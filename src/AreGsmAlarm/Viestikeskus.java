package AreGsmAlarm;

public class Viestikeskus {

    private String name;
    private String phoneNumber;

    public Viestikeskus(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void changeNumber(String newNumber) {
        this.phoneNumber = newNumber;
    }

    public String getName() {
        return this.name;
    }

    public String getNumber() {
        return this.phoneNumber;
    }
}

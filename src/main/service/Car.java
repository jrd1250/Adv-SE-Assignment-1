package service;

public class Car {

    private int id;

    private String brand;

    private String type;

    private String licensePlate;

    private Person owner;

    public Car(int id, String brand, String type, String licensePlate, Person owner) {
        setId(id);
        setBrand(brand);
        setType(type);
        setLicensePlate(licensePlate);
        setOwner(owner);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

}

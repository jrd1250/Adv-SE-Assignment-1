package service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jdk.internal.jline.internal.TestAccessible;
import org.graalvm.compiler.nodeinfo.StructuralInput;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Table(name = "car")
@NamedQueries({
        @NamedQuery(name = "Car.findAll", query = "SELECT c from Car c")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Car {

    @Id
    @SequenceGenerator(
            name = "car_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "car_sequence")
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "brand", length = 20, nullable = false)
    private String brand;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Size(min = 1, max = 9)
    @Column(name = "licensePlate", nullable = false)
    private String licensePlate;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private Person owner;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Car car = (Car) o;
        return Objects.equals(id, car.id) &&
                Objects.equals(brand, car.brand) &&
                Objects.equals(type, car.type) &&
                Objects.equals(licensePlate, car.licensePlate) &&
                owner == null ? car.owner == null : Objects.equals(owner.getId(), car.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, brand, type, licensePlate, owner == null ? null : owner.getId());
    }
}

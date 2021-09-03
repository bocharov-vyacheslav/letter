package data.models;

import java.util.Objects;

public class Phone {

    private byte id;

    private String osnPhone;

    private String dopPhone;

    private boolean isFax;

    public String getOsnPhone() {
        return osnPhone;
    }

    public void setOsnPhone(String osnPhone) {
        this.osnPhone = osnPhone;
    }

    public String getDopPhone() {
        return dopPhone;
    }

    public void setDopPhone(String dopPhone) {
        this.dopPhone = dopPhone;
    }

    public boolean isFax() {
        return isFax;
    }

    public void setFax(boolean fax) {
        isFax = fax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Phone phone = (Phone) o;
        return isFax == phone.isFax &&
                Objects.equals(osnPhone, phone.osnPhone) &&
                Objects.equals(dopPhone, phone.dopPhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(osnPhone, dopPhone, isFax);
    }

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }
}

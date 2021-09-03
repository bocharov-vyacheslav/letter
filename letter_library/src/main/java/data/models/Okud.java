package data.models;

import java.util.Objects;

public class Okud implements IName {

    private String okud;

    private String name;

    private String periodicity;

    private byte periodicityCode;

    @Override
    public String toString() {
        return okud;
    }

    public String getOkud() {
        return okud;
    }

    public void setOkud(String okud) {
        this.okud = okud;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public byte getPeriodicityCode() {
        return periodicityCode;
    }

    public void setPeriodicityCode(byte periodicityCode) {
        this.periodicityCode = periodicityCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Okud okud1 = (Okud) o;
        return periodicityCode == okud1.periodicityCode &&
                Objects.equals(okud, okud1.okud) &&
                Objects.equals(name, okud1.name) &&
                Objects.equals(periodicity, okud1.periodicity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(okud, name, periodicity, periodicityCode);
    }
}

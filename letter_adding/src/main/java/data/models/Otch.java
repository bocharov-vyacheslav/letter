package data.models;

import java.util.Objects;

public class Otch {

    private byte id;

    private String okpo;

    private String okud;

    private String periodYear;

    private String periodicity;

    private String period;

    public String getOkpo() {
        return okpo;
    }

    public void setOkpo(String okpo) {
        this.okpo = okpo;
    }

    public String getOkud() {
        return okud;
    }

    public void setOkud(String okud) {
        this.okud = okud;
    }

    public String getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(String periodYear) {
        this.periodYear = periodYear;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Otch otch = (Otch) o;
        return Objects.equals(okpo, otch.okpo) &&
                Objects.equals(okud, otch.okud) &&
                Objects.equals(periodYear, otch.periodYear) &&
                Objects.equals(periodicity, otch.periodicity) &&
                Objects.equals(period, otch.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(okpo, okud, periodYear, periodicity, period);
    }

    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }
}

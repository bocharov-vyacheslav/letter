package data.models;

import java.util.Objects;

public class PreviousOtch {

    public PreviousOtch () {

        okpo = "";
        okud = "";
        yearIndex = 1;
        periodicityIndex = 0;
        periodIndex = 0;

    }

    private String okpo;

    private String okud;

    private int yearIndex;

    private int periodicityIndex;

    private int periodIndex;

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

    public int getYearIndex() {
        return yearIndex;
    }

    public void setYearIndex(int yearIndex) {
        this.yearIndex = yearIndex;
    }

    public int getPeriodicityIndex() {
        return periodicityIndex;
    }

    public void setPeriodicityIndex(int periodicityIndex) {
        this.periodicityIndex = periodicityIndex;
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(int periodIndex) {
        this.periodIndex = periodIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreviousOtch that = (PreviousOtch) o;
        return yearIndex == that.yearIndex &&
                periodicityIndex == that.periodicityIndex &&
                periodIndex == that.periodIndex &&
                Objects.equals(okpo, that.okpo) &&
                Objects.equals(okud, that.okud);
    }

    @Override
    public int hashCode() {
        return Objects.hash(okpo, okud, yearIndex, periodicityIndex, periodIndex);
    }
}

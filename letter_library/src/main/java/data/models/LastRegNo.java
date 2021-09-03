package data.models;

import java.util.Objects;

public class LastRegNo {

    private long regNoId;

    public long getRegNoId() {
        return regNoId;
    }

    public void setRegNoId(long regNoId) {
        this.regNoId = regNoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LastRegNo lastRegNo = (LastRegNo) o;
        return regNoId == lastRegNo.regNoId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(regNoId);
    }
}

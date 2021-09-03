package data.models;

import java.util.Objects;

public class Okpo implements IName {

    private String okpo;

    private String name;

    public String getOkpo() {
        return okpo;
    }

    public void setOkpo(String okpo) {
        this.okpo = okpo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Okpo okpo1 = (Okpo) o;
        return Objects.equals(okpo, okpo1.okpo) &&
                Objects.equals(name, okpo1.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(okpo, name);
    }

    @Override
    public String toString() {
        return okpo;
    }
}

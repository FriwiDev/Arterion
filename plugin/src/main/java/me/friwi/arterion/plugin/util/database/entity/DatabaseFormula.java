package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "formulas")
public class DatabaseFormula implements DatabaseEntity {
    @Id
    private String identifier;
    private String formula;

    protected DatabaseFormula() {
    }

    public DatabaseFormula(String identifier, String formula) {
        this.identifier = identifier;
        this.formula = formula;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        return Objects.equals(this.identifier, ((DatabaseFormula) other).identifier);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    @Override
    public String toString() {
        return "DatabaseFormula{" +
                "identifier='" + identifier + '\'' +
                ", formula='" + formula + '\'' +
                '}';
    }
}

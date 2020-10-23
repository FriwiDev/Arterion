package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;
import me.friwi.arterion.plugin.util.database.enums.SanctionType;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "sanctions")
public class DatabaseSanction implements DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int sanctionId;
    private long issued;
    private long expires;
    @Enumerated(EnumType.STRING)
    private SanctionType sanctionType;
    private String reason;
    @ManyToOne
    @Basic(fetch = FetchType.LAZY)
    private DatabasePlayer affected;
    @ManyToOne
    @Basic(fetch = FetchType.LAZY)
    private DatabasePlayer issuer;

    protected DatabaseSanction() {
    }

    public DatabaseSanction(long issued, long expires, SanctionType sanctionType, String reason, DatabasePlayer affected, DatabasePlayer issuer) {
        this.issued = issued;
        this.expires = expires;
        this.sanctionType = sanctionType;
        this.reason = reason;
        this.affected = affected;
        this.issuer = issuer;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sanctionId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        return Objects.equals(this.sanctionId, ((DatabaseSanction) other).sanctionId);
    }

    public boolean isSanctionActive() {
        return expires > System.currentTimeMillis();
    }

    public int getSanctionId() {
        return sanctionId;
    }

    public void setSanctionId(int sanctionId) {
        this.sanctionId = sanctionId;
    }

    public long getIssued() {
        return issued;
    }

    public void setIssued(long issued) {
        this.issued = issued;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public SanctionType getSanctionType() {
        return sanctionType;
    }

    public void setSanctionType(SanctionType sanctionType) {
        this.sanctionType = sanctionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public DatabasePlayer getAffected() {
        return affected;
    }

    public void setAffected(DatabasePlayer affected) {
        this.affected = affected;
    }

    public DatabasePlayer getIssuer() {
        return issuer;
    }

    public void setIssuer(DatabasePlayer issuer) {
        this.issuer = issuer;
    }

    @Override
    public String toString() {
        return "DatabaseSanction{" +
                "sanctionId=" + sanctionId +
                ", issued=" + issued +
                ", expires=" + expires +
                ", sanctionType=" + sanctionType +
                ", reason='" + reason + '\'' +
                ", affected=" + affected +
                ", issuer=" + issuer +
                '}';
    }
}

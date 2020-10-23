package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;
import me.friwi.arterion.plugin.util.database.enums.ProductType;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "buy_entries")
public class DatabaseBuyEntry implements DatabaseEntity {
    @Id
    @GeneratedValue
    private UUID uuid;
    private UUID buyerId;
    @Enumerated(EnumType.STRING)
    private ProductType product;

    public DatabaseBuyEntry() {
    }

    public DatabaseBuyEntry(UUID buyerId, ProductType product) {
        this.buyerId = buyerId;
        this.product = product;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        return Objects.equals(this.uuid, ((DatabaseBuyEntry) other).uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public ProductType getProduct() {
        return product;
    }

    public void setProduct(ProductType product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "DatabaseBuyEntry{" +
                "uuid=" + uuid +
                ", buyerId=" + buyerId +
                ", product=" + product +
                '}';
    }
}

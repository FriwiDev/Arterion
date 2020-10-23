package me.friwi.arterion.website.premium;

import me.friwi.arterion.plugin.util.database.enums.ProductType;

import java.util.UUID;

public class MCUser {
    private String name;
    private UUID uuid;
    private ProductType productType;

    public MCUser(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }
}

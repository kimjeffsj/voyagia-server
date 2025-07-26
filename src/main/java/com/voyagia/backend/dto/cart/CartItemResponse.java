package com.voyagia.backend.dto.cart;

import com.voyagia.backend.entity.Cart;
import com.voyagia.backend.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cart Item response DTO
 */
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productSku;
    private String productMainImageUrl;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Integer availableStock;
    private Boolean inStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static class ProductSummary {
        private Long id;
        private String name;
        private String slug;
        private String sku;
        private String mainImageUrl;
        private BigDecimal price;
        private Integer stockQuantity;
        private Boolean isActive;

        public ProductSummary() {
        }

        public ProductSummary(Product product) {
            this.id = product.getId();
            this.name = product.getName();
            this.slug = product.getSlug();
            this.sku = product.getSku();
            this.mainImageUrl = product.getMainImageUrl();
            this.price = product.getPrice();
            this.stockQuantity = product.getStockQuantity();
            this.isActive = product.getIsActive();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getMainImageUrl() {
            return mainImageUrl;
        }

        public void setMainImageUrl(String mainImageUrl) {
            this.mainImageUrl = mainImageUrl;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getStockQuantity() {
            return stockQuantity;
        }

        public void setStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }
    }

    private ProductSummary product;

    public CartItemResponse() {
    }

    public CartItemResponse(Cart cart) {
        this.id = cart.getId();
        this.quantity = cart.getQuantity();
        this.unitPrice = cart.getUnitPrice();
        this.totalPrice = cart.getTotalPrice();
        this.createdAt = cart.getCreatedAt();
        this.updatedAt = cart.getUpdatedAt();

        if (cart.getProduct() != null) {
            Product prod = cart.getProduct();
            this.productId = prod.getId();
            this.productName = prod.getName();
            this.productSlug = prod.getSlug();
            this.productSku = prod.getSku();
            this.productMainImageUrl = prod.getMainImageUrl();
            this.availableStock = prod.getStockQuantity();
            this.inStock = prod.isInStock();
            this.product = new ProductSummary(prod);
        }
    }

    // Static factory method
    public static CartItemResponse from(Cart cart) {
        return new CartItemResponse(cart);
    }

    // Getters/Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSlug() {
        return productSlug;
    }

    public void setProductSlug(String productSlug) {
        this.productSlug = productSlug;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductMainImageUrl() {
        return productMainImageUrl;
    }

    public void setProductMainImageUrl(String productMainImageUrl) {
        this.productMainImageUrl = productMainImageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public ProductSummary getProduct() {
        return product;
    }

    public void setProduct(ProductSummary product) {
        this.product = product;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CartItemResponse{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                ", availableStock=" + availableStock +
                ", inStock=" + inStock +
                '}';
    }
}

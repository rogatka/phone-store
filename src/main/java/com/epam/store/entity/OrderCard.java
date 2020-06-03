package com.epam.store.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "order_cards")
public class OrderCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "phone_id")
    @NotNull
    private Phone phone;

    @Column(name = "item_count")
    @Positive
    @NotNull
    private Long itemCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public Long getItemCount() {
        return itemCount;
    }

    public void setItemCount(Long count) {
        this.itemCount = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderCard orderCard = (OrderCard) o;
        return Objects.equals(id, orderCard.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrderCard{" +
                "id=" + id +
                ", order=" + order +
                ", phone=" + phone +
                ", item count=" + itemCount +
                '}';
    }
}

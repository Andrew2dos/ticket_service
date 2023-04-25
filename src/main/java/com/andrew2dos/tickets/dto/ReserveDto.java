package com.andrew2dos.tickets.dto;

import com.andrew2dos.tickets.entity.Event;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "reserves", schema = "public", catalog = "tickets")
public class ReserveDto {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "reserve_id", nullable = false)
    private Long reserveId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "customer")
    private String customerName;

    @Column(name = "quantity")
    private Integer reservedQuantity;

    public ReserveDto() {
    }

    public ReserveDto(Event event, String customerName, Integer reservedQuantity) {
        this.event = event;
        this.customerName = customerName;
        this.reservedQuantity = reservedQuantity;
    }

    @Override
    public String toString() {
        return "reserve id - " + reserveId + "\n" +
                "event name - '" + event.getEventName() + '\'' + "\n" +
                "quantity - " + reservedQuantity + "\n" +
                "customer - '" + customerName + '\'';
    }
}

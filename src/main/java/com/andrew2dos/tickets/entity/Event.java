package com.andrew2dos.tickets.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "events", schema = "public", catalog = "tickets")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String eventName;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "price")
    private Integer price;

    @Column(name = "place")
    private String place;

    @Column(name = "quantity")
    private Integer quantity;

//    @Column(name = "reserve_quantity")
//    private Integer reserveQuantity;

}

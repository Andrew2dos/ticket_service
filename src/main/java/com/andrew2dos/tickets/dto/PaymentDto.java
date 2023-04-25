package com.andrew2dos.tickets.dto;

import lombok.Data;

@Data
public class PaymentDto {

    private Long reserveId;
    private boolean paid;

}

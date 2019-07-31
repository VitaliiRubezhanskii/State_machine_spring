package com.example.order;

import lombok.*;



@NoArgsConstructor
@Data
public class Order{

    private int id;
    private OrderState currentState;
    private String mark;

}

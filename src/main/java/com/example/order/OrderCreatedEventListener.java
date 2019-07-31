package com.example.order;

import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.stereotype.Component;

import com.example.ContextEntity;
import com.example.DefaultStateMachineAdapter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener extends AbstractRepositoryEventListener<Order> {

    final DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter;

    @Override
    @SneakyThrows
    protected void onBeforeCreate(Order order) {
        log.info("PERSISTED ORDER");
        orderStateMachineAdapter.persist(orderStateMachineAdapter.create(), order);
    }

}

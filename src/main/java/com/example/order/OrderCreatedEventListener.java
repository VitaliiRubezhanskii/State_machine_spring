package com.example.order;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.event.AbstractRepositoryEventListener;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener extends AbstractRepositoryEventListener<Order> {


    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    @Override
    @SneakyThrows
    protected void onBeforeCreate(Order order) {

        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.getExtendedState().getVariables().put("order", order);
        log.info("ORDER INFO: {}", stateMachine.getExtendedState().get("order", Order.class));

    }

}

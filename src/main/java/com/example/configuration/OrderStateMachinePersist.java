package com.example.configuration;

import com.example.ContextEntity;
import com.example.order.OrderEvent;
import com.example.order.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import java.io.Serializable;
@Slf4j
@Configuration
public class OrderStateMachinePersist implements StateMachinePersist<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> {

    @Override
    public void write(StateMachineContext<OrderState, OrderEvent> context, ContextEntity<OrderState, OrderEvent, Serializable> order) throws Exception {
        order.setStateMachineContext(context);
    }

    @Override
    public StateMachineContext<OrderState, OrderEvent> read(ContextEntity<OrderState, OrderEvent, Serializable> order) throws Exception {
        return order.getStateMachineContext();
    }
}

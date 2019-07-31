package com.example;

import java.io.Serializable;

import com.example.configuration.OrderStateMachinePersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityLinks;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import com.example.order.OrderEvent;
import com.example.order.OrderState;

@SpringBootApplication
@EntityScan
@Import(OrderStateMachinePersist.class)
class OrderApplication {

    @Autowired
    private OrderStateMachinePersist orderStateMachinePersist;
    public static void main(String[] args) {
        new SpringApplicationBuilder(OrderApplication.class).build().run(args);
    }

    @Bean
    public DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> orderStateMachineAdapter(
            StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
            StateMachinePersister<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> persister) {
        return new DefaultStateMachineAdapter<>(stateMachineFactory, persister);
    }

    @Bean
    public ContextObjectResourceProcessor<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> orderResourceProcessor(EntityLinks entityLinks,
            DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter) {
        return new ContextObjectResourceProcessor<>(entityLinks, orderStateMachineAdapter);
    }

    @Bean
    public StateMachinePersister<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> persister() {
        return new DefaultStateMachinePersister<>(orderStateMachinePersist);
    }

//    @Bean
//    public StateMachinePersist<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, Serializable>> persist() {
//        return new StateMachinePersist<>() {
//
//
//            public StateMachineContext<OrderState, OrderEvent> read(ContextEntity<OrderState, OrderEvent, Serializable> order) throws Exception {
//                return order.getStateMachineContext();
//            }
//
//
//            public void write(StateMachineContext<OrderState, OrderEvent> context, ContextEntity<OrderState, OrderEvent, Serializable> order) throws Exception {
//                order.setStateMachineContext(context);
//            }
//        };
//    }
}

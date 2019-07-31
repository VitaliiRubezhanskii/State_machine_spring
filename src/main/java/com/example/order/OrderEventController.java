package com.example.order;

import com.example.ContextEntity;
import com.example.DefaultStateMachineAdapter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@RestController
@RequiredArgsConstructor
public class OrderEventController {

    private final DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState,
             OrderEvent, ? extends Serializable>> defaultStateMachineAdapter;
    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;
    private final StateMachinePersister<OrderState, OrderEvent,
            ContextEntity<OrderState, OrderEvent, ? extends Serializable> > persister;
    private final OrderRepository orderRepository;



    @RequestMapping(path = "/orders/receive/{event}", method = RequestMethod.POST)
    @SneakyThrows
    @Transactional
    public HttpEntity<Void> receiveEvent(@RequestBody Order order, @PathVariable("event") OrderEvent event) {
//        StateMachine<OrderState, OrderEvent> stateMachine = defaultStateMachineAdapter.create();
        StateMachine<OrderState, OrderEvent> preStateMachine = stateMachineFactory.getStateMachine();
        StateMachine<OrderState, OrderEvent> stateMachine = persister.restore(preStateMachine, order);
//        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.start();
        if (stateMachine.sendEvent(event)) {
//            defaultStateMachineAdapter.persist(stateMachine, order);
//            orderRepository.save(order);

//            persister.persist(stateMachine, order);
            return ResponseEntity.accepted().build();
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }
}

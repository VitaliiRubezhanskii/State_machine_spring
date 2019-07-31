package com.example.order;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderEventController {


    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;
    private final OrderRepository orderRepository;



    @RequestMapping(path = "/orders/receive/{event}", method = RequestMethod.POST)
    @SneakyThrows
    @Transactional
    public HttpEntity<Void> receiveEvent(@RequestBody Order order, @PathVariable("event") OrderEvent event) {

        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine();
        stateMachine.start();

        Map<Object, Object> extendedState = stateMachine.getExtendedState().getVariables();
        stateMachine.getExtendedState().getVariables().put("order", order);


        if (stateMachine.sendEvent(event)) {

            Object expectManaged = extendedState.get("order");
            orderRepository.save(order);

            return ResponseEntity.accepted().build();
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }
}

package com.example.configuration;

import com.example.order.OrderEvent;
import com.example.order.OrderState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

@Slf4j
@Configuration
public class OrderStateMachineListener implements StateMachineListener<OrderState, OrderEvent> {

    @Override
    public void stateChanged(State<OrderState, OrderEvent> from, State<OrderState, OrderEvent> to) {
        log.info("State changed to {}", to.getId());
    }

    @Override
    public void stateEntered(State state) {

    }

    @Override
    public void stateExited(State state) {

    }

    @Override
    public void eventNotAccepted(Message<OrderEvent> message) {
        log.error("Event not accepted: {}", message.getPayload());
    }

    @Override
    public void transition(Transition transition) {

    }

    @Override
    public void transitionStarted(Transition transition) {

    }

    @Override
    public void transitionEnded(Transition transition) {

    }

    @Override
    public void stateMachineStarted(StateMachine stateMachine) {

    }

    @Override
    public void stateMachineStopped(StateMachine stateMachine) {

    }

    @Override
    public void stateMachineError(StateMachine stateMachine, Exception e) {

    }

    @Override
    public void extendedStateChanged(Object o, Object o1) {

    }

    @Override
    public void stateContext(StateContext stateContext) {

    }
}

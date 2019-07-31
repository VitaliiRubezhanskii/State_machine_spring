package com.example.order;

import com.example.configuration.OrderStateMachineListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.EnumSet;

@Configuration
@Slf4j
@EnableStateMachineFactory(contextEvents=false)
@Import(OrderStateMachineListener.class)
public class OrderStateMachineConfiguration extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

    @Autowired
    private OrderStateMachineListener orderStateMachineListener;

    @Override
    public void configure(StateMachineConfigurationConfigurer<OrderState, OrderEvent> config) throws Exception {
        config
        .withConfiguration()
        .listener(orderStateMachineListener);
    }

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states)
            throws Exception {
        states
            .withStates()
                .initial(OrderState.Open, context -> setUnpaid(context.getExtendedState()))
                .states(EnumSet.allOf(OrderState.class));
    }



/**
+----------------------------------------------------------------------------------------------------------------------------+
|                                                     pre-payment flow                                                       |
+----------------------------------------------------------------------------------------------------------------------------+
|                                (1)                            (2) [if paid]                 (3)                            |
|     +------------------+ ReceivePayment  +-- ---------------+  Deliver +------------------+  Refund  +------------------+  |
| *-->|       Open       |---------------->| ReadyForDelivery |--------->|    Completed     |--------->|     Canceled     |  |
|     |                  |                 |                  |          |                  |          |                  |  |
|     |                  |                 |  ReceivePayment  |          |                  |          |  ReceivePayment  |  |
|     |                  |                 |  +------------+  |          |                  |          |  +------------+  |  |
|     |                  |                 |  |    (11)    |  |          |                  |          |  |    (12)    |  |  |
|     |                  |                 |  |            v  |          |                  |          |  |            v  |  |
|     +------------------+                 +------------------+          +------------------+          +------------------+  |
|        | ^                                             | |         [if paid] (4) Refund                ^   ^       | ^     |
|        | |                                             | +---------------------------------------------+   |       | |     |
|        | |                                             |                                                   |       | |     |
|        | |                                             |           [if !paid]  (8) Cancel                  |       | |     |
|        | |           (5) Reopen                        +---------------------------------------------------+       | |     |
|        | +---------------------------------------------------------------------------------------------------------+ |     |
|        |                                              (6) Cancel                                                     |     |
|        +-------------------------------------------------------------------------------------------------------------+     |
|                                                                                                                            |
+----------------------------------------------------------------------------------------------------------------------------+


+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                                                     post-payment flow                                                                       |
+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
|                                (7)                            (9) [if !paid]                 (10)                            (3)                            |
|     +------------------+ UnlockDelivery  +-- ---------------+  Deliver +------------------+ ReceivePayment +---------------+  Refund  +------------------+  |
| *-->|       Open       |---------------->| ReadyForDelivery |--------->| AwaitingPayment  |--------------->|   Completed   |--------->|     Canceled     |  |
|     |                  |                 |                  |          |                  |                |               |          |                  |  |
|     |                  |                 |  ReceivePayment  |          |                  |                |               |          |  ReceivePayment  |  |
|     |                  |                 |  +------------+  |          |                  |                |               |          |  +------------+  |  |
|     |                  |                 |  |    (11)    |  |          |                  |                |               |          |  |    (12)    |  |  |
|     |                  |                 |  |            v  |          |                  |                |               |          |  |            v  |  |
|     +------------------+                 +------------------+          +------------------+                +---------------+          +------------------+  |
|        | ^                                             |  |          [if paid] (4) Refund                                               ^    ^      | ^     |
|        | |                                             |  +-----------------------------------------------------------------------------+    |      | |     |
|        | |                                             |                                                                                     |      | |     |
|        | |                                             |             [if !paid] (8) Cancel                                                   |      | |     |
|        | |           (5) Reopen                        +-------------------------------------------------------------------------------------+      | |     |
|        | +------------------------------------------------------------------------------------------------------------------------------------------+ |     |
|        |                                              (6) Cancel                                                                                      |     |
|        +----------------------------------------------------------------------------------------------------------------------------------------------+     |
|                                                                                                                                                             |
+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
*/
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions)
            throws Exception {
        transitions
            .withExternal()
            // (1)
                .source(OrderState.Open)
                .target(OrderState.ReadyForDelivery)
                .event(OrderEvent.ReceivePayment)
                .action(receivePayment())
            .and()
            // (2)
            .withExternal()
                .source(OrderState.ReadyForDelivery)
                .target(OrderState.Completed)
                .guard(isPaid())
                .event(OrderEvent.Deliver)
            .and()
            // (3)
            .withExternal()
                .source(OrderState.ReadyForDelivery)
                .target(OrderState.Canceled)
                .guard(isPaid())
                .event(OrderEvent.Refund)
                .action(refundPayment())
            .and()
            // (4)
            .withExternal()
                .source(OrderState.Completed)
                .target(OrderState.Canceled)
                .event(OrderEvent.Refund)
                .action(refundPayment())
            .and()
            // (5)
            .withExternal()
                .source(OrderState.Canceled)
                .target(OrderState.Open)
                .event(OrderEvent.Reopen)
            .and()
            // (6)
            .withExternal()
                .source(OrderState.Open)
                .target(OrderState.Canceled)
                .event(OrderEvent.Cancel)
            .and()
            // (7)
            .withExternal()
                .source(OrderState.Open)
                .target(OrderState.ReadyForDelivery)
                .event(OrderEvent.UnlockDelivery)
            .and()
            // (8)
            .withExternal()
                .source(OrderState.ReadyForDelivery)
                .target(OrderState.Canceled)
                .guard(not(isPaid()))
                .event(OrderEvent.Cancel)
            .and()
            // (9)
            .withExternal()
                .source(OrderState.ReadyForDelivery)
                .target(OrderState.AwaitingPayment)
                .guard(not(isPaid()))
                .event(OrderEvent.Deliver)
            .and()
            // (10)
            .withExternal()
                .source(OrderState.Open)
                .target(OrderState.Completed)
                .event(OrderEvent.ReceivePayment)
                .action(receivePayment())
            .and()
            // (11)
            .withInternal()
                .source(OrderState.ReadyForDelivery)
                .event(OrderEvent.ReceivePayment)
                .action(receivePayment())
            .and()
            // (12)
            .withInternal()
                .source(OrderState.Canceled)
                .event(OrderEvent.ReceivePayment)
                .action(receivePayment())
            .and();
    }

    public Action<OrderState, OrderEvent> receivePayment() {
        return context -> setPaid(context.getExtendedState());
    }

    public Action<OrderState, OrderEvent> refundPayment() {
        return context -> setUnpaid(context.getExtendedState());
    }

    private Guard<OrderState, OrderEvent> isPaid() {
        return context -> 
            (boolean) context.getExtendedState().get("paid", Boolean.class);
    }

    private Guard<OrderState, OrderEvent> not(Guard<OrderState, OrderEvent> guard) {
        return context -> !guard.evaluate(context);
    }

    void setUnpaid(ExtendedState extendedState) {
        log.info("Unsetting paid");
        extendedState.getVariables().put("paid", Boolean.FALSE);
    }

    void setPaid(ExtendedState extendedState) {
      Order o =  ((Order)extendedState.getVariables().get("order"));

      o.setCurrentState(OrderState.Managed); // Business logic

      extendedState.getVariables().put("order", o);
        log.info("Setting paid");
        extendedState.getVariables().put("paid", Boolean.TRUE);
    }

}

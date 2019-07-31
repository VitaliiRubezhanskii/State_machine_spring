package com.example.order;



import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.statemachine.StateMachineContext;

import com.example.ContextEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.experimental.FieldDefaults;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Access(AccessType.FIELD)
@Data
@Table(name = "orders", indexes = @Index(columnList = "currentState"))
public class Order extends AbstractPersistable<Long>
        implements ContextEntity<OrderState, OrderEvent, Long> { // NOSONAR

    private static final long serialVersionUID = 8848887579564649636L;

    @Getter
    @JsonIgnore
    StateMachineContext<OrderState, OrderEvent> stateMachineContext; // NOSONAR

    @Getter
    @Enumerated(EnumType.STRING)
    OrderState currentState;

    @Override
    public void setStateMachineContext(@NonNull StateMachineContext<OrderState, OrderEvent> stateMachineContext) {
        this.currentState = stateMachineContext.getState();
        this.stateMachineContext = stateMachineContext;
    }

    @JsonIgnore
    @Override
    public boolean isNew() {
        return super.isNew();
    }
}

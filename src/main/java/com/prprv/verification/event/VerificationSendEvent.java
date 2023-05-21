package com.prprv.verification.event;

import com.prprv.verification.token.Type;
import org.springframework.context.ApplicationEvent;

/**
 * @author Yoooum
 */
public class VerificationSendEvent extends ApplicationEvent {
    private final Type type;
    public VerificationSendEvent(Object source, Type type) {
        super(source);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}

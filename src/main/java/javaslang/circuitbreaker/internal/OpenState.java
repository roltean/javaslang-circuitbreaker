/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package javaslang.circuitbreaker.internal;

import javaslang.circuitbreaker.CircuitBreaker;
import javaslang.circuitbreaker.CircuitBreakerOpenException;

import java.time.Instant;

final class OpenState extends CircuitBreakerState {

    private final Instant retryAfterWaitDuration;

    OpenState(CircuitBreakerStateMachine stateMachine) {
        super(stateMachine);
        this.retryAfterWaitDuration = Instant.now().plus(stateMachine.getCircuitBreakerConfig().getWaitDurationInOpenState());
    }

    /**
     * Returns false, if the wait duration has not elapsed.
     * Returns true, if the wait duration has elapsed and transitions the state machine to HALF_CLOSED state.
     *
     * @return false, if the wait duration has not elapsed. true, if the wait duration has elapsed.
     */
    @Override
    boolean isCallPermitted() {
        // Thread-safe
        if (Instant.now().isAfter(retryAfterWaitDuration)) {
            stateMachine.transitionToHalfClosedState(CircuitBreaker.StateTransition.OPEN_TO_HALF_CLOSED);
            return true;
        }
        return false;
    }

    /**
     * Should never be called, because isCallPermitted returns false.
     */
    @Override
    void recordFailure() {
        // Should never be called, because isCallPermitted returns false
        throw new CircuitBreakerOpenException(String.format("CircuitBreaker '%s' is open", stateMachine.getName()));
    }

    /**
     * Should never be called, because isCallPermitted returns false.
     */
    @Override
    void recordSuccess() {
        // Should never be called, because isCallPermitted returns false
        throw new CircuitBreakerOpenException(String.format("CircuitBreaker '%s' is open", stateMachine.getName()));
    }

    /**
     * Get the state of the CircuitBreaker
     */
    @Override
    CircuitBreaker.State getState() {
        return CircuitBreaker.State.OPEN;
    }
}

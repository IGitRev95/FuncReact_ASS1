# Coding Notes
- why the resolving/complete process of an action is not triggering a dependency update?
  - because the action/promise does not know if it is a dependency of some other action. its execution is generic.
- Purpose of ActorThreadLoop - worker thread behavior.
- ActorThreadPool is not A Selector Object, it does not responsible for waking up waiting threads when new task is submitted.
- ActionQueue have 2 fields:
  - concurrent queue - which responsible for taking care of Read/Write consistency.
  - Atomic Boolean - prevents parallel execution of the same actor.
- ActorThreadPool have a common wait object which it purpose is that all the waiting threads should be waiting on the same object and could be awakened when a new task is submitted.

# TODO - Remainders:
- Action final modifying
- compilation
- part 2
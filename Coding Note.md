# Coding Notes
- why the resolving/complete process of an action is not triggering a dependency update?
  - because the action/promise does not if it is a dependency of some other action. its execution is generic.
  - [ consider allocating a thread dedicated to the dependencies updating and re-insertion process]
-
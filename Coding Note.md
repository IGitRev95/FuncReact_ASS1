# Coding Notes
- why the resolving/complete process of an action is not triggering a dependency update?
  - because the action/promise does not if it is a dependency of some other action. its execution is generic.
  - [ consider allocating a thread dedicated to the dependencies updating and re-insertion process]


# TODO - Remainders:
- implement in ```Actor.then()``` the procedure of inserting the dependencies in the suspended actions' data structure

# Wondering:
- should the Action hold its actor creator id name? - so it will be easy to locate the proper actor queue to be re-inserted too in case of suspended action.
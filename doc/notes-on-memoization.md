# Notes on Memoization

## Content
- [Intro](#intro)
- [Understanding Object.is for Effective Memoization](#understanding-objectis-for-effective-memoization)
- [Strategies for Creating Stable References](#strategies-for-creating-stable-references)
- [Key Takeaways](#key-takeaways)
- [Further Readings](#further-readings)

## Intro

Memoization is an optimization technique used to speed up computer programs by storing the results of expensive function calls and returning the cached result when the same inputs occur again. It is a specific form of caching where results of function calls are stored based on their input arguments. However, it's important to note that this technique introduces memory overhead, as it requires additional space to store the results of function calls. While it is a powerful tool for improving performance, we must balance its benefits against the increased memory usage, especially in resource-constrained environments.

Memoization in React is used to ensure that a component or a computation does not re-render or re-calculate `unnecessarily` when its input props or dependencies have not changed. This technique can significantly improve the performance of React applications, especially for `expensive`, `computation-heavy` operations or components that `render frequently`.

In React, we generally have three util functions for memoizing :
- React.memo (or `rn/memo` in the case of our codebase):
Memoizes a component's render output based on its props. It can optionally take a comparison function to customize how changes in props are detected.
 e.g:
  ```clojure
  (def pure-component
    (rn/memo (fn [props]
              [view props])
            (fn [prev-props next-props]
              (= (:value prev-props) (:value next-props)))))
  ```

- React.useMemo (or `rn/use-memo` in the case of our codebase):  Memoizes a computed value so that it does not need to be re-calculated on every render, given that its dependencies haven't changed. e.g:
  ```clojure
  (defn component [{:keys [something] :as props}]
    (let [memoized-value (rn/use-memo (fn [] 
                                      (compute-expensive-value something)) 
                                    [something])]
    [view {:value memoized-value}]))
  ```
- React.useCallback (or `rn/use-callback` in the case of our codebase): Similar to React.useMemo, but for callback functions, ensuring that a function's reference remains stable between renders unless its dependencies change. e.g:
  ```clojure
  (defn component [{:keys [something] :as props}]
    (let [on-done (rn/use-callback (fn [] 
                                      (do-some-thing something)) 
                                    [something])]
    [view {:on-done on-done}]))
  ```

Based on the example for the 3 util functions above, we generally see that they take a second argument which is a function in the case of `React.memo` and is a dependency array (vector) for `React.useMemo` and `React.useCallback` respectively. 

In the case of `React.memo`, the second argument been a function is essentially a predicate which we as the consumer use to determine (or tell React) if the previous prop used to render a component is equal to the next prop it would receive. If this results to `true` we get to skip re-rendering, otherwise we re-render with the new props. The argument is also optional, and if not provided React would use Javascript's [Object.is](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/is) to compare each individual prop (previous prop against next prop) for equality. 

## Understanding Object.is for Effective Memoization

For `React.useMemo` and `React.useCallback` the second argument is a required array (vector) of dependencies which we as the consumer do not have any control of telling React how to compare the value of the previous prop to the next prop. It also uses [Object.is](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/is) for comparisons of each of the dependencies.

Since, we do not have a way of telling React of how to compare the previous prop to the next prop in the case of `React.useMemo` and `React.useCallback`, it would benefit us to understand how `Object.is` compares values. 

There are two types of values in Javascript (Javascript is used here because our Clojurescript code essentially compiles to Javascript when we are using pure React):
- Primitive Values: e.g string, number, boolean, BigInt, null, undefined and Symbol
- Reference (non primitive) values: These are all other values asides the ones stated above. e.g objects (map), arrays (vectors), sets, functions etc...

For primitive values, `Object.is` can simply compare them by their actual values e.g `Object.is(2, 2)` or `Object.is("John Snow", "John Snow")` would always return `true` because the values are essentially the same.

For reference values, `Object.is` compares them based on the reference (which for simplicity are like pointers to values stored in a variable). For example:
```js
const a = {}
const b = {}

Object.is(a, b)
```

`Object.is(a, b)` would return `false` even though the values are actually equal. This is because the reference to `a` and `b` are essentially different and they been non-primitive (reference values), `Object.is` compares them with their references rather than their values. The same holds true if we inline the variables like so `Object.is({}, {})`.

Now that we understand this, let us see how this relates to React. In React functional component we essentially create a function which can have some variables bound to its scope and return some UI. e.g:

```clojure
(defn component [props]
   (let [some-state (rn/use-state {:name "John Snow"
                                   :knowledge 0})
         some-vector [1 2 3]]
    [rn/view props]))
```

When the `props` or the bound `"reactive" state` of this component changes, React would re-render this component by executing the function body and by doing so all variables bound to this function's scope are re-initialized meaning they get a new reference if they are reference (non primitive) values. This "re-initialization" and changing of reference happens anytime there is a re-render even if the actual value(s) did not change between renders. This means that any non primitive value in the scope of the function component would by default have an `unstable reference` between every render cycle.

By having an unstable reference, `Object.is` would always return `false` for reference values when comparing them between cycles. The implication of this is that when you pass a reference value with an unstable reference as a prop to a component that is memoized via `React.memo` (without a custom compare function), React would always see that prop as new value when comparing hence re-rendering the component because the reference to the prop changed even when the value hasn't essentially changed. The same is also true for `React.useMemo` and `React.useCallback`, if an unstable reference is passed as a dependency to them, the function they would recompute their function body and return a new reference to the memoized `value` or `callback`. By passing an unstable value (reference value) to these util functions, we essentially defeat the purpose of memoization in the first place.

## Strategies for Creating Stable References

How then do we create stable references? Well there are a number of ways, and there is no one size fits all way of doing it. But here are a few ways we could do it:
### Using global variables: 
We can get stable references by declaring variables in a more global scope relative to the function's local scope. This essentially means if we can, we should declare the variables outside the functions scope. e.g:
  ```clojure
  (def some-map {:name "John Snow"
                :knowledge 0})

  (defn comp []
    [rn/view {:some-map some-map}])
  ```

- Trade-offs: While global variables ensure reference stability, they can introduce side effects or global state management complexities. Overuse can make components harder to understand and test due to implicit dependencies.

- Best Scenarios: Use for constants or configuration data that truly does not change over the application's lifetime and does not belong to any component's state.
  
### Using `React.useRef`: 
Provides a mutable ref object that remains constant throughout the component's lifecycle. Ideal for holding onto a value that does not trigger re-renders.. e.g
  ```clojure
  (defn comp []
    (let [ref (rn/use-ref-atom {:name "John Snow"
                                :knowledge 0})]
    [rn/view {:some-map @ref}]))
  ```
- Trade-offs: It is not "reactive", meaning changes to its content do not cause the component to re-render. It's best used for values that are incidental to rendering.

- Best Scenarios: Storing references to DOM elements, keeping track of previous props or state for comparison, or holding values that interact with imperative APIs.

### Using `React.useState`: 
Returns a stable reference to a stateful value, with an updater function to change its value. The reference only changes when explicitly updated via the updater. e.g
  ```clojure
  (defn comp []
    (let [[state set-state] (rn/use-state {:name "John Snow"
                                           :knowledge 0})]
    [rn/view {:some-map state}]))
  ```

- Trade-offs: It triggers a component re-render when the state changes, which might not be necessary for all types of stored values. Managing large sets of stateful data here can make the component less efficient.

- Best Scenarios: Managing local component state that directly influences the render output. Ideal for values that change over time and need to trigger updates.

### Using `React.useMemo` and `React.useCallback`: 
They both return stable references, but the catch here is that they also require stable references themselves as dependencies. e.g
  ```clojure
  (defn comp []
    (let [[knowledge set-knowledge] (rn/use-state 0)
          derived-state             (rn/use-memo (fn []
                                                    {:name      "John Snow"
                                                    :knowledge knowledge}) 
                                                  [knowledge])]
    [rn/view {:some-map derived-state}]))
  ```

- Trade-offs: They depend on the stability of their dependency lists, which can lead to unnecessary recalculations if dependencies have unstable references. Overuse can lead to increased memory usage and complexity.

- Best Scenarios: `React.useMemo` is best for expensive calculations that depend on specific props or state and do not change on every render. `React.useCallback` is ideal when passing callback functions to deeply nested child components that need stable references to prevent unnecessary renders.

The list is not exhaustive, but these are the most common ways to get stable references to non-primitive values.

It might be worthy to note that most of these would be abstracted away in React 19 with the introduction of a [compiler that helps you memo when needed](https://react.dev/blog/2024/02/15/react-labs-what-we-have-been-working-on-february-2024#react-compiler).

## Key Takeaways
- Before you memo, profile to identify performance bottlenecks
- When you do want to memo, ensure you pass non primitive values with stable references as a dependency to your memo function.
- You do not need to worry about primitive values that much as they are easily comparable
- Memoization introduces its own complexity and memory overhead. Use it judiciously.

## Further Readings
- [Thinking in React](https://react.dev/learn/thinking-in-react)
- [Mastering React’s Stable Values](https://shopify.engineering/master-reacts-stable-values)

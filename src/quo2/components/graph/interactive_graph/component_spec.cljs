(ns quo2.components.graph.interactive-graph.component-spec
  (:require
    [quo2.components.graph.interactive-graph.view :as interactive-graph]
    [test-helpers.component :as h]))

(defn data
  [num-elements]
  (vec (take num-elements (repeat {:value 10}))))

(h/describe "interactive-graph"
  (h/test "render interactive graph"
    (h/render [interactive-graph/view
               {:data data}])
    (h/is-truthy (h/get-by-label-text :interactive-graph))))

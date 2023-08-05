(ns quo2.components.graph.wallet-graph.component-spec
  (:require [test-helpers.component :as h]
            [quo2.components.graph.wallet-graph.view :as wallet-graph]))

(defn data
  [num-elements]
  (vec (take num-elements (repeat {:value 10}))))

(h/describe "wallet-graph"
  (h/test "render empty wallet graph"
    (h/render [wallet-graph/view
               {:time-frame :empty}])
    (h/is-truthy (h/get-by-label-text :illustration)))

  (h/test "render 1 week wallet graph"
    (h/render [wallet-graph/view
               {:time-frame :1-week
                :data       (data 7)}])
    (h/is-truthy (h/get-by-label-text :line-chart))))

(ns quo.components.graph.wallet-graph.component-spec
  (:require
    [quo.components.graph.wallet-graph.view :as wallet-graph]
    [test-helpers.component :as h]))

(defn data
  [num-elements]
  (vec (take num-elements (repeat {:value 10}))))

(h/describe "wallet-graph"
  (h/test "render empty wallet graph"
    (h/render [wallet-graph/view
               {:time-frame :empty}])
    (h/is-truthy (h/get-by-label-text :illustration)))

  ;; NOTE: Throws error:
  ;;   _reactNative.Animated.timing(widthValue2, {
  ;;   Cannot read properties of undefined (reading 'timing')
  (h/test-skip "render 1 week wallet graph"
    (h/render [wallet-graph/view
               {:time-frame :1-week
                :data       (data 7)}])
    (h/is-truthy (h/get-by-label-text :line-chart))))

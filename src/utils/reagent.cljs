(ns utils.reagent
  (:require [reagent.impl.template :as template]
            [reagent.impl.util :as reagent.util]
            [utils.transforms :as transforms]))

(defn convert-prop-value
  "Based on `reagent.impl.template/kv-conv`.
  Takes the prop map of a reagent component and returns a React-valid property JS Object
  by transforming kebab-case keys -> camelCase, maps -> JS Objects and vectors -> arrays.

  This version adds support to recursively transform properties inside vectors, to have a
  more consistent developer experience in React Native."
  [x]
  (cond
    (reagent.util/js-val? x) x
    (reagent.util/named? x)  (name x)
    (map? x)                 (reduce-kv template/kv-conv #js {} x)
    (vector? x)              (transforms/map-array convert-prop-value x)
    (coll? x)                (clj->js x)
    (ifn? x)                 (fn [& args]
                               (apply x args))
    :else                    (clj->js x)))

(defn set-convert-props-in-vectors!
  "We override the default reagent implementation with the one that supports vectors."
  []
  (set! template/convert-prop-value convert-prop-value))

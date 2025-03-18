(ns reagent.config
  (:require [goog.object :as gobj]
            [reagent.core]
            [reagent.impl.template :as template]
            [reagent.impl.util :as reagent.util]
            [utils.transforms :as transforms]))

(def ^:dynamic ^js *keys-to-convert* #js {})

(declare convert-prop-value)

(defn kv-conv
  [convert-in-vectors?]
  (fn [o k v]
    (let [recursively-convert-in-vectors? (or convert-in-vectors?
                                              (.hasOwnProperty *keys-to-convert* (name k)))]
      (doto o
        (gobj/set (template/cached-prop-name k)
                  (convert-prop-value v recursively-convert-in-vectors?))))))

(defn convert-prop-value
  "Based on `reagent.impl.template/kv-conv`.
  Takes the prop map of a reagent component and returns a React-valid property JS Object
  by transforming kebab-case keys -> camelCase, maps -> JS Objects and vectors -> arrays.

  This version adds support to recursively transform properties inside vectors, to have a
  more consistent developer experience in React Native."
  ([x]
   (convert-prop-value x false))
  ([x convert-in-vectors?]
   (cond
     (reagent.util/js-val? x) x
     (reagent.util/named? x)  (name x)
     (map? x)                 (reduce-kv (kv-conv convert-in-vectors?) #js {} x)
     (and convert-in-vectors?
          (vector? x))        (transforms/map-array #(convert-prop-value % true) x)
     (coll? x)                (clj->js x)
     (ifn? x)                 (fn [& args]
                                (apply x args))
     :else                    (clj->js x))))

(defn set-convert-props-in-vectors!
  "We override the default reagent implementation with the one that supports vectors."
  [keys-to-convert]
  (set! *keys-to-convert* (reduce (fn [o k]
                                    (doto o (gobj/set (name k) true)))
                                  #js {}
                                  keys-to-convert))
  (set! template/convert-prop-value convert-prop-value))

;; Config applied for all the app
(def functional-compiler (reagent.core/create-compiler {:function-components true}))
(reagent.core/set-default-compiler! functional-compiler)
(set-convert-props-in-vectors! #{:style :holes})

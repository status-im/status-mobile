(ns utils.transforms
  (:refer-clojure :exclude [js->clj])
  (:require
    [cljs-bean.core :as clj-bean]
    [oops.core :as oops]
    [reagent.impl.template :as reagent.template]
    [reagent.impl.util :as reagent.util]))

(defn js->clj [data] (cljs.core/js->clj data :keywordize-keys true))

(defn clj->pretty-json
  [data spaces]
  (.stringify js/JSON (clj-bean/->js data) nil spaces))

(defn clj->json [data] (clj->pretty-json data 0))

(defn json->clj
  [json]
  (when-not (= json "undefined")
    (try (js->clj (.parse js/JSON json))
         (catch js/Error _ (when (string? json) json)))))

(defn json->js
  [json]
  (when-not (= json "undefined")
    (try (.parse js/JSON json) (catch js/Error _ (when (string? json) json)))))

(declare styles-with-vectors)

(defn ^:private convert-keys-and-values
  "Takes a JS Object a key and a value.
   Transforms the key from a Clojure style prop to a JS style prop, using the reagent cache.
   Performs a mutual recursion transformation on the value using `styles-with-vectors`.

   Based on `reagent.impl.template/kv-conv`."
  [obj k v]
  (doto obj
    (oops/gobj-set (reagent.template/cached-prop-name k) (styles-with-vectors v))))

(defn styles-with-vectors
  "Takes a Clojure style map or a Clojure vector of style maps and returns a JS Object
   valid to use as React Native styles.
   The transformation is done by performing mutual recursive calls with `convert-keys-and-values`.

   Based on `reagent.impl.template/convert-prop-value`."
  [x]
  (cond (reagent.util/js-val? x) x
        (reagent.util/named? x)  (name x)
        (map? x)                 (reduce-kv convert-keys-and-values #js {} x)
        (vector? x)              (to-array (mapv styles-with-vectors x))
        :else                    (clj->js x)))

(defn copy-js-obj-to-map
  "Copy `obj` keys and values into `m` if `(pred obj-key)` is satisfied."
  [obj m pred]
  (persistent!
   (reduce (fn [acc js-prop]
             (if (pred js-prop)
               (assoc! acc js-prop (oops/gobj-get obj js-prop))
               acc))
           (transient m)
           (js-keys obj))))

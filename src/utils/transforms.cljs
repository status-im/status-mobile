(ns utils.transforms
  (:refer-clojure :exclude [js->clj])
  (:require
    [camel-snake-kebab.core :as csk]
    [cljs-bean.core :as clj-bean]
    [oops.core :as oops]
    [reagent.impl.template :as reagent.template]
    [reagent.impl.util :as reagent.util]))

(defn js->clj [data] (cljs.core/js->clj data :keywordize-keys true))

(defn clj->pretty-json
  [data spaces]
  (.stringify js/JSON (clj-bean/->js data) nil spaces))

(defn clj->json [data] (clj->pretty-json data 0))

(defn js-stringify
  [js-object spaces]
  (.stringify js/JSON js-object nil spaces))

(defn js-parse
  [data]
  (.parse js/JSON data))

(defn js-dissoc
  [js-object & ks]
  (let [object-copy (.assign js/Object #js {} js-object)]
    (doseq [js-key ks]
      (js-delete object-copy (name js-key)))
    object-copy))

(defn json->clj
  [json]
  (when-not (= json "undefined")
    (try (js->clj (.parse js/JSON json))
         (catch js/Error _ (when (string? json) json)))))

(def ->kebab-case-keyword (memoize csk/->kebab-case-keyword))
(def ->PascalCaseKeyword (memoize csk/->PascalCaseKeyword))

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

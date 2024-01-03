(ns utils.transforms
  (:refer-clojure :exclude [js->clj])
  (:require
    [cljs-bean.core :as clj-bean]
    [oops.core :as oops]
    [reagent.impl.template]
    [reagent.impl.util]))

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

;; Functions took from reagent and modified to also transform vectors contained in styles

(def ^:private js-val? reagent.impl.util/js-val?)
(def ^:private named? reagent.impl.util/named?)
(def ^:private cached-prop-name reagent.impl.template/cached-prop-name)
(declare styles-with-vectors)

(defn ^:private kv-conv
  [o k v]
  (doto o
    (oops/gobj-set (cached-prop-name k) (styles-with-vectors v))))

(defn styles-with-vectors
  [x]
  (cond (js-val? x) x
        (named? x)  (name x)
        (map? x)    (reduce-kv kv-conv #js {} x)
        (vector? x) (to-array (map styles-with-vectors x))
        (coll? x)   (clj->js x)
        (ifn? x)    (fn [& args]
                      (apply x args))
        :else       (clj->js x)))

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

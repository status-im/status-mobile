(ns utils.transforms
  (:refer-clojure :exclude [js->clj])
  (:require
    [camel-snake-kebab.core :as csk]
    [cljs-bean.core :as clj-bean]
    [oops.core :as oops]))

(defn js->clj [data] (cljs.core/js->clj data :keywordize-keys true))

(defn clj->pretty-json
  [data spaces]
  (.stringify js/JSON (clj-bean/->js data) nil spaces))

(defn clj->json [data] (clj->pretty-json data 0))

(defn <-js-map
  "Shallowly transforms JS Object keys/values with `key-fn`/`val-fn`.

  Returns m if `m` is not an instance of `js/Object`.

  Implementation taken from `js->clj`, but with the ability to customize how
  keys and/or values are transformed in one loop.

  This function is useful when you don't want to recursively apply the same
  transformation to keys/values. For example, many maps in the app-db are
  indexed by ID, like `community.members`. If we convert the entire community
  with (js->clj m :keywordize-keys true), then IDs will be converted to
  keywords, but we want them as strings. Instead of transforming to keywords and
  then transforming back to strings, it's better to not transform them at all.
  "
  ([^js m]
   (<-js-map m nil))
  ([^js m {:keys [key-fn val-fn]}]
   (assert (or (nil? m) (identical? (type m) js/Object) (map? m)))
   (if (identical? (type m) js/Object)
     (persistent!
      (reduce (fn [r k]
                (let [v       (oops/oget+ m k)
                      new-key (if key-fn (key-fn k v) k)
                      new-val (if val-fn (val-fn k v) v)]
                  (assoc! r new-key new-val)))
              (transient {})
              (js-keys m)))
     m)))

(defn js-stringify
  [js-object spaces]
  (.stringify js/JSON js-object nil spaces))

(defn js-parse
  [data]
  (.parse js/JSON data))

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

(defn map-array
  "Performs an efficient `map` operation on `coll` but returns a JS array"
  [f coll]
  (let [js-array ^js (array)]
    (doseq [e coll]
      (.push js-array (f e)))
    js-array))

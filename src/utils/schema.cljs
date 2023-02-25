(ns utils.schema
  (:require-macros utils.schema)
  (:require
    [malli.core :as malli]
    [malli.dev.pretty :as malli.pretty]
    malli.generator
    malli.util))

(defn gen
  "Same as `malli.generator/generate` but for a particular `path` in `?schema`.
  `path` is a vector, as in `get-in`.

  Usage:

    (let [?schema [:map
                   [:person
                    [:map [:age :int]]]]]
      (gen ?schema [:person :age]))
    ;; => 42
  "
  ([?schema]
   (gen ?schema [] nil))
  ([?schema path]
   (gen ?schema path nil))
  ([?schema path options]
   (-> ?schema
       malli/deref
       (malli.util/get-in path)
       (malli.generator/generate options))))

(defn match
  "Returns `value` when it conforms to `?schema`, otherwise `fallback-value`."
  ([?schema value]
   (match ?schema value nil))
  ([?schema value fallback-value]
   (if (malli.pretty/explain ?schema value)
     fallback-value
     value)))

(defn reporter
  "Custom reporter optimized for small screens.

  We might need to use `malli.dev.pretty/thrower` instead of
  `malli.dev.pretty/report`, otherwise calls to memoized functions won't fail on
  subsequent calls after the first failure."
  ([]
   (reporter nil))
  ([opts]
   (let [printer (malli.pretty/-printer
                  (merge {:width        60
                          :print-length 6
                          :print-level  3
                          :print-meta   false}
                         opts))]
     (if (exists? js/jest)
       ;; Report schema errors as an exception to signal a failure to Jest.
       (malli.pretty/thrower printer)
       (malli.pretty/reporter printer)))))

(defn instrument-fn
  "Similar to `malli/=>`, but should be used to instrument functional Reagent
  components and anonymous functions."
  [id ?schema f]
  (malli/-instrument {:schema ?schema
                      :report (reporter {:title (str "Schema error - " id)})}
                     f))

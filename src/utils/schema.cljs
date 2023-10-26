(ns utils.schema
  (:require-macros utils.schema)
  (:require
    [malli.core :as malli]
    [malli.dev.pretty :as malli.pretty]))

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
  components and anonymous functions in general.

  Avoid using this function directly. Prefer the macro
  `utils.schema/instrument`."
  [schema-id ?schema f]
  (malli/-instrument {:schema ?schema
                      :report (reporter {:title (str "Schema error - " schema-id)})}
                     f))

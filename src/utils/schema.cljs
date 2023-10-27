(ns utils.schema
  (:require-macros utils.schema)
  (:require
    [malli.core :as malli]
    [malli.dev.pretty :as malli.pretty]
    schema.ui))

(defn ui-reporter
  "Prints to STDOUT and signals a schema error should be displayed on screen."
  [schema-id printer]
  (let [report (malli.pretty/reporter printer)]
    (fn [type data]
      (report type data)
      (swap! schema.ui/schema-errors conj schema-id))))

(defn reporter
  "Build custom reporter.

  We might need to use `malli.dev.pretty/thrower` instead of
  `malli.dev.pretty/report`, otherwise calls to memoized functions won't fail on
  subsequent calls after the first failure."
  ([]
   (reporter nil nil))
  ([schema-id]
   (reporter schema-id nil))
  ([schema-id opts]
   (let [printer (malli.pretty/-printer
                  (merge {:width        60
                          :print-length 6
                          :print-level  3
                          :print-meta   false}
                         opts))]
     (if (exists? js/jest)
       ;; Report schema errors as an exception to signal a failure to Jest.
       (malli.pretty/thrower printer)
       (ui-reporter schema-id printer)))))

(defn- with-clear-schema-error
  "Clears current schema error on app if args passed to `f` are valid."
  [schema-id validate-input f]
  (fn [& args]
    (when (validate-input args)
      (swap! schema.ui/schema-errors disj schema-id))
    (apply f args)))

(defn -instrument
  "Similar to `malli/-instrument`, but can be used to instrument anonymous
  functions (e.g. subscriptions) and will automatically clear up visible schema
  errors if `f` is called with valid arguments.

  We use a validator cached by `malli.core/validator`, so that validation is
  performed once.

  Do not use this function directly, this is only used by the macro
  `utils.schema/instrument`."
  [schema-id ?schema f]
  (let [?schema               (malli/schema ?schema)
        {schema-input :input} (malli/-function-info ?schema)
        [validate-input _]    (malli/-vmap malli/validator [schema-input])]
    (malli/-instrument {:schema ?schema
                        :report (reporter schema-id {:title (str "Schema error - " schema-id)})}
                       (with-clear-schema-error schema-id validate-input f))))

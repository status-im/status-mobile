(ns schema.core
  (:require-macros schema.core)
  (:require
    [malli.core :as malli]
    [malli.dev.pretty :as malli.pretty]
    schema.state
    [taoensso.timbre :as log]))

(goog-define throw-on-error? false)

(defn- ui-reporter
  "Prints to STDOUT and signals a schema error should be displayed on screen."
  [schema-id printer]
  (let [report (malli.pretty/reporter printer)]
    (fn [type data]
      (report type data)
      (swap! schema.state/errors conj schema-id))))

(defn- thrower
  "Similar to `malli.dev.pretty/thrower`, but this reporter uses js/Error instead
  of ex-info, otherwise invalid schema errors will be printed in one long and
  incomprehensible line in unit test failures."
  ([] (thrower (malli.pretty/-printer)))
  ([printer]
   (let [report (malli.pretty/reporter printer)]
     (fn [type data]
       (let [message (with-out-str (report type data))]
         (throw (js/Error. (str "\n" message))))))))

(defn reporter
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
     (if throw-on-error?
       ;; The thrower reporter should be used to short-circuit tests on schema errors.
       (thrower printer)
       (ui-reporter schema-id printer)))))

(defn- with-clear-schema-error
  "Clears current schema error on app if args passed to `f` are valid."
  [schema-id validate-input f]
  (fn [& args]
    (when (validate-input args)
      (swap! schema.state/errors disj schema-id))
    (apply f args)))

(defn instrument
  "Similar to `malli/-instrument`, but will automatically clear up visible schema
  errors if `f` is called with valid arguments.

  We use a validator cached by `malli.core/validator`, so that validation is
  performed once.

  If `?schema` is invalid, then behave like a nop, log the error and return `f`.

  `schema-id` is optional, but should be passed when instrumenting anonymous
  functions. Consider using a namespaced keyword. For example:

    (schema/instrument ::foo
                       (fn [x] (inc x))
                       [:=> [:cat :int] :int])
  "
  ([f ?schema]
   (if ^boolean js/goog.DEBUG
     (let [schema-id (when (var? f) (symbol f))]
       (when-not schema-id
         (log/warn "Anonymous function instrumented without an explicit identifier."
                   {:schema ?schema}))
       (instrument schema-id f ?schema))
     f))
  ([schema-id f ?schema]
   (if ^boolean js/goog.DEBUG
     (try
       (let [?schema               (malli/schema ?schema)
             {schema-input :input} (malli/-function-info ?schema)
             [validate-input _]    (malli/-vmap malli/validator [schema-input])]
         (malli/-instrument {:schema ?schema
                             :report (reporter schema-id
                                               {:title (str "Schema error at " schema-id)})}
                            (with-clear-schema-error schema-id validate-input f)))
       (catch js/Error e
         (log/error "Failed to instrument function"
                    {:schema-id schema-id
                     :error     e
                     :function  f})
         (throw e)))
     f)))

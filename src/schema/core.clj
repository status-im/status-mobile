(ns schema.core)

(defmacro =>
  "Similar to `malli.core/=>`, but instrumentation code will be completely removed
  in non-debug environments.

  `value` is first transformed via `malli.core/schema` to make sure we fail fast
  and only register valid schemas.

  Pass raw schema `value` instead of schema instance when it's `:function` schema
  https://github.com/status-im/status-mobile/pull/18364#issuecomment-1875543794"
  [sym value]
  `(if ^boolean js/goog.DEBUG
     (try
       (let [schema-instance# (malli.core/schema ~value)]
         (if (and (seq ~value) (= :function (first ~value)))
           (malli.core/=> ~sym ~value)
           (malli.core/=> ~sym schema-instance#)))
       (catch js/Error e#
         (taoensso.timbre/error "Failed to instrument function"
                                {:symbol ~sym :error e#})
         (throw e#)))
     ~sym))

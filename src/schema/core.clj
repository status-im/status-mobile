(ns schema.core)

(defmacro =>
  "Similar to `malli.core/=>`, but instrumentation code will be completely removed
  in non-debug environments.

  `value` is first transformed via `malli.core/schema` to make sure we fail fast
  and only register valid schemas."
  [sym value]
  `(if ^boolean js/goog.DEBUG
     (try
       (malli.core/=> ~sym (malli.core/schema ~value))
       (catch js/Error e#
         (taoensso.timbre/error "Failed to instrument function"
                                {:symbol ~sym :error e#})
         ~sym))
     ~sym))

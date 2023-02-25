(ns utils.schema)

(defmacro =>
  "Exactly like `malli.core/=>`, but does nothing and return `sym` if running in
  debug mode. In practice, enables instrumentation only in debug mode.

  Note: the type hint is mandatory so the Closure compiler can remove the code
  at compile time."
  [sym value]
  `(if ^boolean js/goog.DEBUG
     (malli.core/=> ~sym ~value)
     ~sym))

(defmacro instrument
  [& args]
  `(if ^boolean js/goog.DEBUG
     (utils.schema/instrument-fn ~@args)
     ~(last args)))

(ns utils.schema)

(defmacro =>
  "Same as `malli.core/-instrument`, but instrumentation code will be completely
  removed in non-debug environments.

  Note: the type hint is mandatory so the Closure compiler can remove the code
  at compile time."
  [sym value]
  `(if ^boolean js/goog.DEBUG
     (malli.core/=> ~sym (malli.core/schema ~value))
     ~sym))

(defmacro instrument
  "Same as `malli.core/-instrument`, but instrumentation code will be completely
  removed in non-debug environments."
  [schema-id ?schema f]
  `(if ^boolean js/goog.DEBUG
     (utils.schema/-instrument ~schema-id ~?schema ~f)
     ~f))

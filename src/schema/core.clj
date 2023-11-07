(ns schema.core)

(defmacro instrument
  "Same as `schema.core/-instrument`, but instrumentation code will be completely
  removed in non-debug environments."
  [schema-id ?schema f]
  `(if ^boolean js/goog.DEBUG
     (schema.core/-instrument ~schema-id ~?schema ~f)
     ~f))

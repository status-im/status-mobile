(ns quo.previews.preview)

(defmacro list-comp
  [[binding seq-expr & bindings] body-expr]
  (cond (not binding)
        `(list ~body-expr)

        :else
        `(mapcat (fn [~binding] (list-comp ~bindings ~body-expr))
          ~seq-expr)))

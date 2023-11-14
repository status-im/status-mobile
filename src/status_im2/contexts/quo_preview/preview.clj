(ns status-im2.contexts.quo-preview.preview)

(defmacro list-comp
  [[the-binding seq-expr & bindings] body-expr]
  (cond (not the-binding)
        `(list ~body-expr)

        :else
        `(mapcat (fn [~the-binding] (list-comp ~bindings ~body-expr))
          ~seq-expr)))

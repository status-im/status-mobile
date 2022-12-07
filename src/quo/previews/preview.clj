(ns quo.previews.preview)

(defn descriptor->values [{:keys [key options type]}]
  {key (case type
         :boolean [false true]
         :text    [nil "Just simple text"] ; NOTE(Ferossgp): add example with long text?
         :select  (mapv :key options))})

(defmacro list-comp [[binding seq-expr & bindings] body-expr]
  (cond (not binding)
        `(list ~body-expr)

        :else
        `(mapcat (fn [~binding] (list-comp ~bindings ~body-expr))
                 ~seq-expr)))

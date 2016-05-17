(ns syng-im.utils.views)

(defn prepare-subs [subs]
  (let [pairs (map (fn [[form sub]]
                     {:form form
                      :sub  sub
                      :sym  (gensym)})
                   (partition 2 subs))]
    [(mapcat (fn [{:keys [sym sub]}]
               [sym `(re-frame.core/subscribe ~sub)])
             pairs)
     (mapcat (fn [{:keys [sym form]}]
               [form `(deref ~sym)])
             pairs)]))

(defmacro defview
  [n params & rest]
  (let [[subs body] (if (= 1 (count rest))
                      [nil (first rest)]
                      rest)
        [subs-bindings vars-bindings] (prepare-subs subs)]
    `(defn ~n ~params
       (let [~@subs-bindings]
         (fn ~params
           (let [~@vars-bindings]
             ~body))))))


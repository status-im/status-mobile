(ns status-im.utils.views
  (:require [clojure.walk :as walk]))

(defn atom?
  [sub]
  (and (seq sub)
       (#{`reagent.core/atom} (first sub))))

(defn walk-sub
  [sub form->sym]
  (if (coll? sub)
    (walk/postwalk (fn [f]
                     (or (form->sym f) f))
                   sub)
    (or (form->sym sub) sub)))

(defn prepare-subs
  [subscriptions]
  (let [pairs     (map (fn [[form sub]]
                         {:form form
                          :sub  sub
                          :sym  (if (atom? sub)
                                  (gensym (str (if (map? form) "keys" form)))
                                  form)})
                       (partition 2 subscriptions))
        form->sym (->> pairs
                       (map (fn [{:keys [form sym]}]
                              [form sym]))
                       (into {}))]
    [(mapcat (fn [{:keys [form sub]}]
               (when-not (vector? sub)
                 [form (walk-sub sub form->sym)]))
      pairs)
     (apply concat
            (keep (fn [{:keys [sym form sub]}]
                    (cond
                      (vector? sub)
                      [form `(deref (re-frame.core/subscribe ~(walk-sub sub form->sym)))]

                      (atom? sub)
                      [form `(deref ~sym)]))
                  pairs))]))

(defmacro letsubs [_ & _])

(defmacro defview
  [n params & rest-body]
  (let [first-symbol                       (ffirst rest-body)
        rest-body'                         (if (and (symbol? first-symbol)
                                                    (= (name first-symbol) "letsubs"))
                                             (rest (first rest-body))
                                             rest-body)
        [subscriptions component-map body] (case (count rest-body')
                                             1 [nil {} (first rest-body')]
                                             2 (let [first-element (first rest-body')]
                                                 (if (map? first-element)
                                                   [nil first-element (second rest-body')]
                                                   [(first rest-body') {} (second rest-body')]))
                                             3 rest-body')
        [subs-bindings vars-bindings]      (prepare-subs subscriptions)]
    `(defn ~n
       ~params
       (let [~@subs-bindings]
         (reagent.core/create-class
          (merge ~(->> component-map
                       (map (fn [[k f]]
                              (let [args (gensym "args")]
                                [k
                                 `(fn [& ~args]
                                    (let [~@vars-bindings]
                                      (apply ~f ~args)))])))
                       (into {}))
                 {:display-name (name '~n)
                  :reagent-render
                  (fn ~params
                    (let [~@vars-bindings]
                      ~body))}))))))

(ns status-im.utils.handlers-macro)

(defmacro merge-fx*
  "This macro is called recursively from merge-fx
  It wraps each form in a let binding that captures
  - the new co-effect after updating the db key if a db effect was in the map returned
  by the previous form
  - the new fx map after merging previous fx map with the map returned by the previous form
  We use safe-merge for that which is returning a map with a :merging-fx-with-common-keys effect
  in case at least one effect that is not a :db effect was overwritten by merge"
  {:added "1.0"}
  [fx cofx & forms]
  (if forms
    (let [form (first forms)
          temp-cofx (gensym 'temp-cofx)]
      `(let [~temp-cofx (update-db ~cofx ~fx)
             fx# (safe-merge ~fx ~(with-meta `(~(first form) ~@(next form) ~temp-cofx) (meta form)))]
         (merge-fx* fx# ~temp-cofx ~@(next forms))))
    fx))

(defmacro merge-fx
  "Takes a map of co-effects and forms as argument.
  The first optional form can be map of effects/
  The next forms are functions applying effects and returning a map of effects.
  The macro ensures that updates to db are passed from function to function within the cofx :db key and
  that only a :merging-fx-with-common-keys effect is returned if some functions are trying
  to produce the same effects (excepted :db effect)"
  {:added "1.0"}
  [cofx & forms]
  (let [form (first forms)]
    (if (or (symbol? form)
            (map? form))
      `(merge-fx* ~form ~cofx ~@(next forms))
      `(merge-fx* {} ~cofx ~@forms))))

(comment (defn fn1 [{:keys [db]}]
           {:db (assoc db :a 0)
            :a "1"})

         (defn fn2 [a {:keys [db]}]
           {:db (update db :a + a)})

         (defn fn3 [a {:keys [db u]}]
           {:db (update db :a + u)})

         (let [a    1
               b    2
               cofx {:db {} :u 1}]
           (merge-fx cofx
                     {:db {:hello 2}}
                     (fn1)
                     (fn2 a)
                     (fn3 b))))

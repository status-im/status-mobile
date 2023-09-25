(ns quo2.components.list-items.preview-list.properties)

(def ^:private types-for-squared-border #{:accounts :collectibles})

(defn border-type
  [type]
  (if (types-for-squared-border type) :squared :rounded))

(def sizes
  {:size-32 {:size             32
             :user-avatar-size :small
             :border-radius    {:rounded 16 :squared 10}
             :hole-radius      {:rounded 18 :squared 12}
             :margin-left      -8
             :hole-size        36
             :hole-x           22
             :hole-y           -2}
   :size-24 {:size             24
             :user-avatar-size :xs
             :border-radius    {:rounded 12 :squared 8}
             :hole-radius      {:rounded 13 :squared 9}
             :margin-left      -4
             :hole-size        26
             :hole-x           19
             :hole-y           -1}
   :size-20 {:size             20
             :user-avatar-size :xxs
             :border-radius    {:rounded 10 :squared 8}
             :hole-radius      {:rounded 11 :squared 9}
             :margin-left      -4
             :hole-size        22
             :hole-x           15
             :hole-y           -1}
   :size-16 {:size             16
             :user-avatar-size :xxxs
             :border-radius    {:rounded 8 :squared 8}
             :hole-radius      {:rounded 9 :squared 9}
             :margin-left      -4
             :hole-size        18
             :hole-x           11
             :hole-y           -1}
   :size-14 {:size             14
             :user-avatar-size :xxxs
             :border-radius    {:rounded 7 :squared 7}
             :hole-radius      {:rounded 8 :squared 8}
             :margin-left      -2
             :hole-size        16
             :hole-x           11
             :hole-y           -1}})

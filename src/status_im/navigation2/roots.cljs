(ns status-im.navigation2.roots)

(defn roots []
  {:home-stack
   {:root
    {:stack {:id       :home-stack
             :children [{:component {:name    :home-stack
                                     :id      :home-stack
                                     :options {:topBar {:visible false}}}}]}}}})

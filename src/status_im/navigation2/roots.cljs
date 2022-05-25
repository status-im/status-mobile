(ns status-im.navigation2.roots)

(defn roots []
  {:home-stack
   {:root
    {:stack {:id       :home-stack
             :children [{:component {:name    :chat-stack
                                     :id      :chat-stack
                                     :options {:topBar {:visible false}}}}]}}}})

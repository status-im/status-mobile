(ns quo2.components.markdown.list.style)

(defn container
  [container-style]
  (merge container-style
         {:flex-direction :row
          :flex           1
          :align-items    :flex-start}))

(def index
  {:margin-left 5})

(def text-container
  {:margin-left 8
   :flex        1})

(ns quo.components.markdown.list.style)

(defn container
  [container-style]
  (merge container-style
         {:flex-direction :row
          :align-items    :flex-start}))

(def index
  {:margin-left 5})

(def text-container
  {:margin-left 8})

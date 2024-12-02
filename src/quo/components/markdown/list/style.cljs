(ns quo.components.markdown.list.style)

(defn container
  [container-style]
  (merge
   {:padding-vertical   7
    :padding-horizontal 20
    :flex-direction     :row
    :align-items        :flex-start}
   container-style))

(def index
  {:margin-left 5})

(def text-container
  {:margin-left 8})

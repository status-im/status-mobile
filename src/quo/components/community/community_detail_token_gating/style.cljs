(ns quo.components.community.community-detail-token-gating.style)

(def container
  {:padding-horizontal 20
   :margin-vertical    -4})

(defn token-row
  [first?]
  {:flex-direction :row
   :margin-top     (if first? 8 4)
   :row-gap        10
   :column-gap     8
   :flex-wrap      :wrap
   :margin-bottom  16})

(def divider
  {:padding-left 0
   :height       28
   :padding-top  0
   :align-items  :flex-start})

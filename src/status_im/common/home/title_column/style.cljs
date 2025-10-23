(ns status-im.common.home.title-column.style)

(def title-column
  {:flex-direction     :row
   :align-items        :center
   :height             56
   :padding-vertical   12
   :padding-horizontal 20
   :background-color   :transparent})

(def title
  {:flex           1
   :align-items    :center
   :flex-direction :row})

(def beta-label {:padding-top 6 :padding-bottom 2})

(def title-column-text
  {:accessibility-label :communities-screen-title
   :margin-right        6
   :weight              :semi-bold
   :size                :heading-1})

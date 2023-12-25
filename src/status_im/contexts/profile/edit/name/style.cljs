(ns status-im.contexts.profile.edit.name.style)

(defn page-wrapper
  [insets]
  {:padding-top    (:top insets)
   :padding-bottom (:bottom insets)
   :flex           1})

(def screen-container
  {:flex               1
   :padding-top        14
   :padding-horizontal 20
   :justify-content    :space-between})

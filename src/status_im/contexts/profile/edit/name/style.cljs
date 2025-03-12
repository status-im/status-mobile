(ns status-im.contexts.profile.edit.name.style)

(defn page-wrapper
  [insets]
  {:padding-top        (:top insets)
   :padding-bottom     (:bottom insets)
   :padding-horizontal 1
   :flex               1})

(def screen-container
  {:flex               1
   :padding-top        14
   :padding-horizontal 20
   :justify-content    :space-between})

(def button-wrapper
  {:margin-vertical 12})

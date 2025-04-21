(ns status-im.contexts.onboarding.enable-notifications.style)

(def default-margin 20)

(defn page-container
  [insets]
  {:flex            1
   :justify-content :space-between
   :padding-top     (:top insets)})

(defn page-illustration
  [width]
  {:flex  1
   :width width})

(def page-title
  {:margin-top        12
   :margin-horizontal 20
   :margin-bottom     8})

(def page-heading {:z-index 1})

(defn buttons
  [insets]
  {:margin        default-margin
   :margin-bottom (+ 14 (:bottom insets))})

(def background-image
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def news-notifications-checkbox-container
  {:flex-direction     :row
   :gap                8
   :padding-top        8
   :padding-bottom     12
   :padding-horizontal 20})

(def news-notifications-checkbox-text
  {:flex 1})

(ns status-im.contexts.communities.actions.invite-contacts.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]))

(def contact-selection-heading
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :flex-end
   :margin-top      24
   :margin-bottom   16})

(def chat-button
  {:position :absolute
   :bottom   (safe-area/get-bottom)
   :left     20
   :right    20})

(defn no-contacts
  []
  {:margin-bottom   (+ 96 (safe-area/get-bottom))
   :flex            1
   :justify-content :center
   :align-items     :center})

(def context-tag
  {:align-self    :flex-start
   :margin-top    -8
   :margin-bottom 12})

(def no-contacts-text
  {:margin-bottom 2
   :margin-top    12})

(def no-contacts-button-container
  {:margin-top    20
   :margin-bottom 12})

(defn section-list-container-style
  [theme]
  {:padding-bottom   70
   :background-color (colors/theme-colors colors/white
                                          colors/neutral-95
                                          theme)})

(defn invite-to-community-text
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

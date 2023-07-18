(ns status-im2.contexts.quo-preview.settings.category
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]))

(def data
  [{:title     "Item 1"
    :left-icon :i/browser
    :chevron?  true}
   {:title     "Item 2"
    :left-icon :i/browser
    :chevron?  true}
   {:title     "Item 3"
    :left-icon :i/browser
    :chevron?  true}
   {:title     "Item 4"
    :left-icon :i/browser
    :chevron?  true}
   {:title     "Item 5"
    :left-icon :i/browser
    :chevron?  true}])

(def label "Label")

(defn preview
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)
            :flex             1}}
   [quo/category {:label label :data data}]])

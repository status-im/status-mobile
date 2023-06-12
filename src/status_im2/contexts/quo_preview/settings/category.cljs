(ns status-im2.contexts.quo-preview.settings.category
  (:require [quo2.core :as quo]
            [react-native.core :as rn]))

(def mockData
  [{:title               "Jazz"
    :accessibility-label :settings-list-item
    :left-icon           :browser-context
    :chevron?            true
    :border              true
    :on-press            (fn [] (js/alert "Jazz pressed"))}
   {:title               "Hip hop"
    :accessibility-label :settings-list-item
    :left-icon           :browser-context
    :chevron?            false
    :border              true
    :on-press            (fn [] (js/alert "Hip hop pressed"))}
   {:title               "Folk"
    :accessibility-label :settings-list-item
    :left-icon           :browser-context
    :chevron?            true
    :border              true
    :on-press            (fn [] (js/alert "Folk pressed"))}])

(defn cool-preview [_]
  [quo/category {:label "Music"
                 :settings-list-data mockData}])

(defn preview-category
  []
  [rn/view {:style {:flex 1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

(ns status-im2.contexts.quo-preview.community.discover-card
  (:require [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [quo2.components.community.discover-card :as discover-card]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]))

(def descriptor
  [{:label "Joined:"
    :key   :joined?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:joined? :false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:flex    1
          :padding 16}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :justify-content  :center}
         [discover-card/discover-card
          {:joined?     (:joined? @state)
           :title       (i18n/label :t/discover)
           :description (i18n/label :t/whats-trending)}]]]])))

(defn preview-discoverd-card
  []
  [rn/view
   {:background-color (colors/theme-colors colors/neutral-5
                                           colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])

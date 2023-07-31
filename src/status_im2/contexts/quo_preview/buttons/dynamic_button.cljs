(ns status-im2.contexts.quo-preview.buttons.dynamic-button
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :jump-to
               :value "Jump To"}
              {:key   :mention
               :value "Mention"}
              {:key   :notification-down
               :value "Notification Down"}
              {:key   :notification-up
               :value "Notification Up"}
              {:key   :search
               :value "Search"}
              {:key   :search-with-label
               :value "Search With Label"}
              {:key   :scroll-to-bottom
               :value "Bottom"}]}
   {:label "Count"
    :key   :count
    :type  :text}])

(defn cool-preview
  []
  (let [state (reagent/atom {:count  "5"
                             :type   :jump-to
                             :labels {:jump-to           (i18n/label :t/jump-to)
                                      :search-with-label (i18n/label :t/back)}})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo/dynamic-button @state]]]])))

(defn preview-dynamic-button
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

(ns status-im2.contexts.quo-preview.navigation.floating-shell-button
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Show jump to?"
    :key   :show-jump-to?
    :type  :boolean}
   {:label "Show search?"
    :key   :show-search?
    :type  :boolean}
   {:label "Show mention?"
    :key   :show-mention?
    :type  :boolean}
   {:label   "Scroll Type"
    :key     :scroll-type
    :type    :select
    :options [{:key   :notification-up
               :value "Notification Up"}
              {:key   :notification-down
               :value "Notification Down"}
              {:key   :scroll-to-bottom
               :value "Scroll To Bottom"}]}])

(defn mock-data
  [{:keys [show-jump-to? show-search? show-mention? scroll-type]}]
  (cond-> {}
    show-jump-to?
    (assoc :jump-to {:on-press #() :label (i18n/label :t/jump-to)})
    show-search?
    (assoc :search {:on-press #()})
    show-mention?
    (assoc :mention {:on-press #() :count 6 :customization-color :turquoise})
    (= scroll-type :notification-up)
    (assoc :notification-up {:on-press #() :count 8})
    (= scroll-type :notification-down)
    (assoc :notification-down {:on-press #() :count 8})
    (= scroll-type :scroll-to-bottom)
    (assoc :scroll-to-bottom {:on-press #()})))

(defn cool-preview
  []
  (let [state (reagent/atom {:show-jump-to? true
                             :scroll-type   :notification-down})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo/floating-shell-button (mock-data @state) nil]]]])))

(defn preview-floating-shell-button
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

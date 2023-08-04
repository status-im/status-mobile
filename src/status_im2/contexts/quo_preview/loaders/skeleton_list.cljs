(ns status-im2.contexts.quo-preview.loaders.skeleton-list
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as icon]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Content:"
    :key     :content
    :type    :select
    :options [{:key   :list-items
               :value "List items"}
              {:key   :notifications
               :value "Notifications"}
              {:key   :messages
               :value "Messages"}]}
   {:label "Blur?"
    :key   :blur?
    :type  :boolean}
   {:label "Animated?"
    :key   :animated?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:content       :messages
                             :blur?         false
                             :animated?     true
                             :parent-height 600})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:style {:margin-vertical 40
                  :padding-left    40
                  :flex-direction  :row
                  :align-items     :center}}
         [text/text
          {:size   :heading-1
           :weight :semi-bold}
          "Skeleton list"]
         [rn/view
          {:style {:width            20
                   :height           20
                   :border-radius    60
                   :background-color colors/success-50
                   :align-items      :center
                   :justify-content  :center
                   :margin-left      8}}
          [icon/icon :i/check {:color colors/white :size 16}]]]
        [preview/customizer state descriptor]
        [rn/view
         [quo/skeleton-list @state]]]])))

(defn preview-skeleton
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

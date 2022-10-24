(ns quo2.screens.navigation.bottom-nav-tab
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.reanimated :as reanimated]
            [quo2.components.navigation.bottom-nav-tab :as quo2]
            [quo2.foundations.colors :as colors]))

(def descriptor [{:label   "Type"
                  :key     :icon
                  :type    :select
                  :options [{:key   :main-icons2/communities
                             :value "Communities"}
                            {:key   :main-icons2/messages
                             :value "Messages"}
                            {:key   :main-icons2/wallet
                             :value "Wallet"}
                            {:key   :main-icons2/browser
                             :value "Browser"}]}
                 {:label "Selected?"
                  :key   :selected?
                  :type  :boolean}
                 {:label "Pass through?"
                  :key   :pass-through?
                  :type  :boolean}
                 {:label "New Notifications?"
                  :key   :new-notifications?
                  :type  :boolean}
                 {:label   "Notification Indicator"
                  :key     :notification-indicator
                  :type    :select
                  :options [{:key   :counter
                             :value :counter}
                            {:key   :unread-dot
                             :value :unread-dot}]}
                 {:label "Counter Label"
                  :key   :counter-label
                  :type  :text}])

(defn get-icon-color [selected? pass-through?]
  (cond
    selected?     colors/white
    pass-through? colors/white-opa-40
    :else          colors/neutral-50))

(defn cool-preview []
  (let [state         (reagent/atom {:icon                   :main-icons2/communities
                                     :new-notifications?     true
                                     :notification-indicator :counter
                                     :counter-label          8
                                     :preview-label-color    colors/white})
        selected?     (reagent/cursor state [:selected?])
        pass-through? (reagent/cursor state [:pass-through?])]
    [:f>
     (fn []
       (let [icon-color-anim (reanimated/use-shared-value colors/white)]
         (reanimated/set-shared-value
          icon-color-anim
          (get-icon-color @selected? @pass-through?))
         [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
          [rn/view {:padding-bottom 150}
           [preview/customizer state descriptor]
           [rn/view {:padding-vertical 60
                     :align-items      :center}
            [quo2/bottom-nav-tab
             (merge @state {:icon-color-anim icon-color-anim})
             (:value @state)]]]]))]))

(defn preview-bottom-nav-tab []
  [rn/view {:background-color colors/neutral-100
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])

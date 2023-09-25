(ns status-im2.contexts.quo-preview.navigation.bottom-nav-tab
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :icon
    :type    :select
    :options [{:key   :i/communities
               :value "Communities"}
              {:key   :i/messages
               :value "Messages"}
              {:key   :i/wallet
               :value "Wallet"}
              {:key   :i/browser
               :value "Browser"}]}
   {:key :selected? :type :boolean}
   {:key :pass-through? :type :boolean}
   {:key :new-notifications? :type :boolean}
   {:key     :notification-indicator
    :type    :select
    :options [{:key :counter}
              {:key :unread-dot}]}
   {:key :counter-label :type :text}
   (preview/customization-color-option)])

(defn get-icon-color
  [selected? pass-through?]
  (cond
    selected?     colors/white
    pass-through? colors/white-opa-40
    :else         colors/neutral-50))

(defn- f-bottom-tab
  [state selected? pass-through?]
  (let [icon-color-anim (reanimated/use-shared-value colors/white)]
    (reanimated/set-shared-value
     icon-color-anim
     (get-icon-color selected? pass-through?))
    [quo/bottom-nav-tab
     (merge state {:icon-color-anim icon-color-anim})
     (:value state)]))

(defn view
  []
  (let [state         (reagent/atom {:icon                   :i/communities
                                     :new-notifications?     true
                                     :notification-indicator :counter
                                     :counter-label          8
                                     :preview-label-color    colors/white
                                     :customization-color    :turquoise})
        selected?     (reagent/cursor state [:selected?])
        pass-through? (reagent/cursor state [:pass-through?])]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60
                                    :align-items      :center}}
       [:f> f-bottom-tab @state @selected? @pass-through?]])))

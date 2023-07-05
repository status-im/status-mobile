(ns stories.avatars.account-avatar-story
  (:require
    [react-native.core :as rn]
    [quo2.components.avatars.account-avatar :as account-avatar]
    [reagent.core :as reagent]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as theme]))

(def ^:export default
  #js
   {:title     "Avatar Stories"
    :component account-avatar/account-avatar})

(def avatar-configs
  [{:size 80 :customization-color :purple :emoji "🍑"}
   {:size 48 :customization-color :purple :emoji "🍑"}
   {:size 32 :customization-color :purple :emoji "🍑"}
   {:size 28 :customization-color :purple :emoji "🍑"}
   {:size 24 :customization-color :purple :emoji "🍑"}
   {:size 20 :customization-color :purple :emoji "🍑"}
   {:size 16 :customization-color :purple :emoji "🍑"}])

(defn ^:export AccountAvatar
  []
  (reagent/as-element
   [:<>
    [rn/view {:style {:background-color :red}}]
    [rn/view
     {:style {:flex           1
              :width          200
              :flex-direction :row}}
     [theme/provider {:theme :light}
      [rn/view
       {:style {:flex          1
                :align-items   :center
                :border-radius 16
                :padding-top   26}}
       (map (fn [props]
              [rn/view
               {:style {:margin-bottom 28}}
               [account-avatar/account-avatar props]])
            avatar-configs)]]
     [theme/provider {:theme :dark}
      [rn/view
       {:style {:flex             1
                :align-items      :center
                :border-radius    16
                :padding-top      26
                :background-color colors/neutral-95}}
       (map (fn [props]
              [rn/view
               {:style {:margin-bottom 28}}
               [account-avatar/account-avatar props]])
            avatar-configs)]]]]))




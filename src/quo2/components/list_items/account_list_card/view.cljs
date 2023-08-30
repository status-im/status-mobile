(ns quo2.components.list-items.account-list-card.view
  (:require
    [quo2.components.avatars.account-avatar.view :as account-avatar]
    [quo2.components.icon :as icon]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [quo2.components.list-items.account-list-card.style :as style]))

(defn- internal-view
  [{:keys [theme account-props networks state action on-press]}]
  [rn/view
   {:style               (style/container state theme)
    :accessibility-label :container}
   [rn/view
    {:style {:flex-direction :row
             :align-items    :center}}
    [account-avatar/view account-props]
    [rn/view {:style {:margin-left 8}}
     [text/text
      {:weight :semi-bold
       :size   :paragraph-2}
      (:name account-props)]
     [text/text {:size :paragraph-2}
      (map (fn [network]
             ^{:key (str network)}
             [text/text
              {:size  :paragraph-2
               :style {:color (get colors/networks network)}} (str (subs (name network) 0 3) ":")])
           networks)
      [text/text
       {:size  :paragraph-2
        :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
       (:address account-props)]]]]
   (when (= action :icon)
     [rn/pressable {:on-press on-press}
      [icon/icon :i/options
       {:color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
        :accessibility-label :icon}]])])

(def view (quo.theme/with-theme internal-view))

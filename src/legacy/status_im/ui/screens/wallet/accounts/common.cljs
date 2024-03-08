(ns legacy.status-im.ui.screens.wallet.accounts.common
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon]
    [legacy.status-im.ui.components.core :as components]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.screens.wallet.components.views :as wallet.components]
    [legacy.status-im.utils.utils :as utils.utils]
    [legacy.status-im.wallet.utils :as wallet.utils]
    [quo.core :as quo]
    [quo.foundations.colors :as quo.colors]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

;; Note(rasom): sometimes `refreshing` might get stuck on iOS if action happened
;; too fast. By updating this atom in 1s we ensure that `refreshing?` property
;; is updated properly in this case.
(def updates-counter (reagent/atom 0))

(defn schedule-counter-reset
  []
  (utils.utils/set-timeout
   (fn []
     (swap! updates-counter inc)
     (when @(re-frame/subscribe [:wallet-legacy/refreshing-history?])
       (schedule-counter-reset)))
   1000))

(defn refresh-action
  []
  (schedule-counter-reset)
  (re-frame/dispatch [:wallet-legacy.ui/pull-to-refresh-history]))

(defn refresh-control
  [refreshing?]
  (reagent/as-element
   [rn/refresh-control
    {:refreshing (boolean refreshing?)
     :onRefresh  refresh-action}]))

(defn render-asset
  [{:keys [icon decimals amount color value] :as token} _ _ currency]
  [list.item/list-item
   {:title               [components/text {:weight :medium}
                          [components/text {:weight :inherit}
                           (str (if amount
                                  (wallet.utils/format-amount amount decimals)
                                  "...")
                                " ")]
                          [components/text
                           {:color  :secondary
                            :weight :inherit}
                           (wallet.utils/display-symbol token)]]
    :subtitle            (str (if value value "...") " " currency)
    :accessibility-label (str (:symbol token) "-asset-value")
    :icon                (if icon
                           [wallet.components/token-icon icon]
                           [chat-icon/custom-icon-view-list (:name token) color])}])

(defn render-asset-new
  [{:keys [icon decimals amount color value name] :as token} _ _ currency]
  [rn/view {:height 56 :margin-horizontal 8 :margin-top 4}
   [rn/view {:position :absolute :left 12 :top 12}
    (if icon
      [wallet.components/token-icon (merge icon {:width 32 :height 32})]
      [chat-icon/custom-icon-view-list (:name token) color])]
   [rn/view {:position :absolute :left 52 :top 8 :right 12}
    [rn/view {:flex-direction :row :justify-content :space-between :align-items :center}
     [quo/text {:weight :semi-bold :style {:height 22}}
      name]
     [quo/text {:size :paragraph-2 :weight :medium}
      (str (if value value "...") " " currency)]]
    [quo/text
     {:size   :paragraph-2
      :weight :medium
      :style  {:color (quo.colors/theme-colors quo.colors/neutral-50 quo.colors/neutral-40)}}
     (str (if amount
            (wallet.utils/format-amount amount decimals)
            "...")
          " "
          (wallet.utils/display-symbol token))]]])

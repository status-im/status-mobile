(ns quo2.components.wallet.summary-info.view
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.resources :as resources]
    [quo2.theme :as quo.theme]
    [quo2.components.avatars.account-avatar.view :as account-avatar]
    [react-native.core :as rn]))


(defn- view-internal
  [{:keys [theme]}]
  [rn/view {:style {:width          335
                    :height         90
                    :border-radius  16
                    :border-width   1
                    :border-color   (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
                    :padding-top    8
                    :padding-bottom 4}}
   [rn/view {:style {:flex-direction     :row
                     :padding-horizontal 12
                     :align-items :center}}
    [account-avatar/view {:customization-color :purple
                          :size                32
                          :emoji               "🍑"
                          :type                :default}]
    [rn/view {:style {:margin-left 8}}
     [text/text {:weight :semi-bold} "Collectibles vault"]
     [text/text {:size :paragraph-2
                 :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}} "0x0ah...78b"]]]
   [rn/view {:style {:width "100%"
                     :height 1
                     :margin-vertical 6
                     :background-color (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)}}]
   [rn/view {:style {:padding-horizontal 12
                     :padding-vertical 4}}
    [rn/view {:style {:flex-direction :row
                      :align-items :center}}
     [rn/image {:style  {:width  16
                         :height 16}
                :source (resources/networks :ethereum)}]
     [text/text {:size :paragraph-2
                 :weight :medium
                 :style {:margin-left 4
                         :margin-right 8}} "150 ETH"]
     [rn/view {:style {:width 2
                       :height 2
                       :border-radius 2
                       :background-color (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)}}]]]])

(def view (quo.theme/with-theme view-internal))

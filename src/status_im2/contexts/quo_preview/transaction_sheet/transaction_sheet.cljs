(ns status-im2.contexts.quo-preview.transaction-sheet.transaction-sheet
  (:require [quo2.components.tabs.account-selector :as acc-sel]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.quo-preview.tabs.segmented-tab]
            [quo2.components.tabs.segmented-tab :as quo2]
            [status-im2.contexts.shell.jump-to.utils :as utils]
            [quo2.components.inputs.locked-input.view :as locked-input]))

(let [{:keys [width]} (utils/dimensions)]
  (def screen-width width))
(def account-selector-width (* 0.91 screen-width))
(def shared-selector-list-data
  {:show-label?  false
   :transparent? false
   :style        {:width account-selector-width}})
(def unique-selector-list-data
  [{:account-text  "Drakarys account"
    :account-emoji "🔥"}
   {:account-text  "Daenerys account"
    :account-emoji "👸"}])
(def selector-list-data
  (map #(merge % shared-selector-list-data)
       unique-selector-list-data))

(def label-style
  {:font-size 12
   :color     colors/neutral-50})

(defn- render-account-selectors
  [item]
  [rn/view {:style {:margin-right 10}}
   [rn/touchable-opacity {:on-press #(js/alert (str "Pressed " (item :account-text)))}
    [acc-sel/account-selector item]]])
(defn preview-transaction-sheet
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1
    :flex-direction   :column
    :padding-top      20
    :padding-left     15}
   [rn/text
    {:style {:font-size   20
             :font-weight :bold
             :color       (colors/theme-colors
                           colors/black
                           colors/white)
            }} "Sign transactions with Rarible"]
   [quo2/segmented-control
    {:size            28
     :blur?           false
     :default-active  1
     :container-style {:margin-top   19
                       :margin-right 20}
     :data            [{:id    1
                        :label (i18n/label :t/simple)}
                       {:id    2
                        :label (i18n/label :t/advanced)}]}]
   [rn/view {:style {:margin-top 21}}
    [rn/text {:style label-style} (i18n/label :t/select-account)]
    [rn/flat-list
     {:data                              selector-list-data
      :render-fn                         render-account-selectors
      :horizontal                        true
      :shows-horizontal-scroll-indicator false
      :style                             {:margin-top 4}}]
    [rn/view
     {:style {:margin-top     11
              :flex-direction :row}}
     [locked-input/locked-input
      {:value-text  "$1,648.34"
       :icon        :i/gas
       :label-text  (i18n/label :t/network-fee)
       :label-style label-style
       :style       {:padding-right 15
                     :width         (/ account-selector-width 2)}}]
     [locked-input/locked-input
      {:value-text  "~3 min"
       :icon        :i/duration
       :label-text  (i18n/label :t/duration-estimate)
       :label-style label-style
       :style       {:width (/ account-selector-width 2)}}]]]])

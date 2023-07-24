(ns status-im2.contexts.quo-preview.transaction-sheet.transaction-sheet
  (:require [quo2.components.tabs.account-selector :as acc-sel]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.quo-preview.tabs.segmented-tab]
            [quo2.components.tabs.segmented-tab :as quo2]
            [status-im2.contexts.shell.jump-to.utils :as utils]
            [quo2.components.inputs.locked-input.view :as locked-input]
            [quo2.components.markdown.text :as text]))

(let [{:keys [width]} (utils/dimensions)]
  (def screen-width width))
(def account-selector-width (* 0.895 screen-width))
(def shared-selector-list-data
  {:show-label?  false
   :transparent? false
   :style        {:width  account-selector-width
                  :height 40}})
(def unique-selector-list-data
  [{:account-text  "Drakaris account"
    :account-emoji "🔥"}
   {:account-text  "Daenerys account"
    :account-emoji "👸"}])
(def selector-list-data
  (map #(merge % shared-selector-list-data)
       unique-selector-list-data))

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
    :padding-left     20}
   [text/text
    {:size  :heading-2
     :weight :semi-bold
     :style {:color (colors/theme-colors
                      colors/black
                      colors/white)
             }} "Sign transaction with Rarible"]
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
   [rn/view {:style {:margin-top 19}}
    [text/text {:size   :paragraph-2
                :weight :regular
                :style  {:color colors/neutral-50}} (i18n/label :t/select-account)]
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
      {:icon       :i/gas
       :label-text (i18n/label :t/network-fee)
       :style      {:margin-right 16
                    :width        160}} "$1,648.34"]
     [locked-input/locked-input
      {:icon       :i/duration
       :label-text (i18n/label :t/duration-estimate)
       :style      {:margin-right 18
                    :width        160}} "~3 min"]]]])

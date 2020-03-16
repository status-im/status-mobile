(ns status-im.ui.screens.wallet.custom-tokens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.common.common :as components.common]
            [clojure.string :as string]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.topbar :as topbar]))

(def debounce-timers (atom {}))

(defn debounce-and-save [field-key value]
  (let [timeout (get @debounce-timers field-key)]
    (when timeout (js/clearTimeout timeout))
    (swap! debounce-timers assoc field-key
           (js/setTimeout
            #(re-frame/dispatch [:wallet.custom-token.ui/field-is-edited field-key (string/trim value)])
            500))))

(defview add-custom-token []
  (letsubs [{:keys [contract name symbol balance decimals in-progress? error error-name error-symbol]}
            [:wallet/custom-token-screen]]
    [react/keyboard-avoiding-view {:flex 1 :background-color colors/white}
     [topbar/topbar {:title :t/add-custom-token}]
     [react/scroll-view {:keyboard-should-persist-taps :handled :style {:flex 1 :margin-top 8 :padding-horizontal 16}}
      [react/view {:style {:flex-direction :row :justify-content :space-between :padding-vertical 10}}
       [react/text (i18n/label :t/contract-address)]
       (if in-progress?
         [react/view {:flex-direction :row :justify-content :center}
          [react/view {:height 20}
           [react/activity-indicator {:width 24 :height 24 :animating true}]]
          [react/text {:style {:color colors/gray :margin-left 5}}
           (i18n/label :t/processing)]])]
      (when-not in-progress?
        ;;tooltip covers button
        [react/view {:position :absolute :z-index 1000 :right 0 :top 10}
         [react/touchable-highlight {:on-press #(re-frame/dispatch [:wallet.custom-token.ui/contract-address-paste])}
          [react/text {:style {:color colors/blue}}
           (i18n/label :t/paste)]]])
      [text-input/text-input-with-label
       {:on-change-text #(debounce-and-save :contract %)
        :error          error
        :default-value  contract
        :multiline      true
        :height         78
        :auto-focus     false
        :placeholder    (i18n/label :t/specify-address)}]
      [react/view {:height 16}]
      [text-input/text-input-with-label
       {:on-change-text #(debounce-and-save :name %)
        :label          (i18n/label :t/name)
        :default-value  name
        :error          error-name
        :auto-focus     false
        :placeholder    (i18n/label :t/name-of-token)}]
      [react/view {:height 16}]
      [react/view {:style {:flex-direction :row}}
       [react/view {:flex 1}
        [text-input/text-input-with-label
         {:on-change-text #(debounce-and-save :symbol %)
          :label          (i18n/label :t/symbol)
          :error          error-symbol
          :default-value  symbol
          :auto-focus     false
          :placeholder    "ABC"}]]
       [react/view {:flex 1 :margin-left 33}
        [text-input/text-input-with-label
         {:label          (i18n/label :t/decimals)
          :on-change-text #(debounce-and-save :decimals %)
          :default-value  decimals
          :keyboard-type  :number-pad
          :max-length     2
          :auto-focus     false
          :placeholder    "18"}]]]
      [react/view {:height 16}]
      #_[text-input/text-input-with-label
         {:label         (i18n/label :t/balance)
          :default-value (when (and balance decimals)
                           (wallet.utils/format-amount balance decimals))
          :editable      false
          :placeholder   (i18n/label :t/no-tokens-found)}]]
     [react/view {:style {:height 1 :background-color colors/gray-lighter}}]
     [react/view {:flex-direction    :row
                  :margin-horizontal 12
                  :margin-vertical   15
                  :align-items       :center}

      [react/view {:style {:flex 1}}]
      [components.common/bottom-button
       {:forward?  true
        :label     (i18n/label :t/add)
        :disabled? (boolean
                    (or in-progress?
                        error error-name error-symbol
                        (string/blank? contract) (string/blank? name)
                        (string/blank? symbol) (string/blank? decimals)))
        :on-press  #(re-frame/dispatch [:wallet.custom-token.ui/add-pressed])}]]]))

(defview custom-token-details []
  (letsubs [{:keys [address name symbol decimals custom?] :as token}
            [:get-screen-params]]
    [react/keyboard-avoiding-view {:flex 1 :background-color colors/white}
     [topbar/topbar {:title name}]
     [react/scroll-view {:keyboard-should-persist-taps :handled
                         :style {:flex 1 :margin-top 8}}
      [react/view {:padding-horizontal 16}
       [text-input/text-input-with-label
        {:label          (i18n/label :t/contract-address)
         :default-value  address
         :multiline      true
         :height         78
         :editable       false}]
       [react/view {:height 16}]
       [text-input/text-input-with-label
        {:label          (i18n/label :t/name)
         :default-value  name
         :editable       false}]
       [react/view {:height 16}]
       [react/view {:style {:flex-direction :row}}
        [react/view {:flex 1}
         [text-input/text-input-with-label
          {:label          (i18n/label :t/symbol)
           :editable       false
           :default-value  symbol}]]
        [react/view {:flex 1 :margin-left 33}
         [text-input/text-input-with-label
          {:label          (i18n/label :t/decimals)
           :default-value  (str decimals)
           :editable       false}]]]]
      [react/view {:height 24}]
      (when custom?
        [list-item/list-item
         {:theme        :action-destructive
          :title        :t/remove-token
          :icon         :main-icons/delete
          :on-press     #(re-frame/dispatch [:wallet.custom-token.ui/remove-pressed token true])}])]]))

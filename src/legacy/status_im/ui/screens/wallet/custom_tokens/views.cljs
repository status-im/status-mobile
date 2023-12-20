(ns legacy.status-im.ui.screens.wallet.custom-tokens.views
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]])
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(def debounce-timers (atom {}))

(defn debounce-and-save
  [field-key value]
  (let [timeout (get @debounce-timers field-key)]
    (when timeout (js/clearTimeout timeout))
    (swap! debounce-timers assoc
      field-key
      (js/setTimeout
       #(re-frame/dispatch [:wallet-legacy.custom-token.ui/field-is-edited field-key
                            (string/trim value)])
       500))))

(defview add-custom-token
  []
  (letsubs [{:keys [contract name decimals in-progress? error error-name error-symbol]
             :as   m}
            [:wallet-legacy/custom-token-screen]]
    (let [sym (:symbol m)]
      [react/keyboard-avoiding-view {:flex 1 :background-color colors/white}
       [react/scroll-view
        {:keyboard-should-persist-taps :handled
         :style                        {:flex               1
                                        :padding-horizontal 16}}
        [react/view {:padding-vertical 8}
         [react/view
          {:style {:flex-direction   :row
                   :justify-content  :space-between
                   :padding-vertical 10}}
          [react/text (i18n/label :t/contract-address)]
          (when in-progress?
            [react/view {:flex-direction :row :justify-content :center}
             [react/view {:height 20}
              [react/activity-indicator {:width 24 :height 24 :animating true}]]
             [react/text {:style {:color colors/gray :margin-left 5}}
              (i18n/label :t/processing)]])]
         (when-not in-progress?
           ;;tooltip covers button
           [react/view {:position :absolute :z-index 1000 :right 0 :top 10}
            [react/touchable-highlight
             {:on-press #(re-frame/dispatch [:wallet-legacy.custom-token.ui/contract-address-paste])}
             [react/text {:style {:color colors/blue}}
              (i18n/label :t/paste)]]])
         [quo/text-input
          {:on-change-text #(debounce-and-save :contract %)
           :error          error
           :default-value  contract
           :monospace      true
           :multiline      true
           :height         78
           :auto-focus     false
           :placeholder    (i18n/label :t/specify-address)}]]
        [react/view {:padding-vertical 8}
         [quo/text-input
          {:on-change-text #(debounce-and-save :name %)
           :label          (i18n/label :t/name)
           :default-value  name
           :error          error-name
           :auto-focus     false
           :placeholder    (i18n/label :t/name-of-token)}]]
        [react/view {:padding-vertical 8}
         [react/view {:style {:flex-direction :row}}
          [react/view
           {:flex          1
            :padding-right 8}
           [quo/text-input
            {:on-change-text #(debounce-and-save :symbol %)
             :label          (i18n/label :t/symbol)
             :error          error-symbol
             :default-value  sym
             :auto-focus     false
             :show-cancel    false
             :placeholder    "ABC"}]]
          [react/view
           {:flex         1
            :padding-left 8}
           [quo/text-input
            {:label          (i18n/label :t/decimals)
             :on-change-text #(debounce-and-save :decimals %)
             :default-value  decimals
             :keyboard-type  :number-pad
             :max-length     2
             :auto-focus     false
             :show-cancel    false
             :placeholder    "18"}]]]]
        #_[quo/text-input
           {:label         (i18n/label :t/balance)
            :default-value (when (and balance decimals)
                             (wallet.utils/format-amount balance decimals))
            :editable      false
            :placeholder   (i18n/label :t/no-tokens-found)}]]

       [toolbar/toolbar
        {:show-border? true
         :right
         [quo/button
          {:type     :secondary
           :after    :main-icon/next
           :disabled (boolean
                      (or in-progress?
                          error
                          error-name
                          error-symbol
                          (string/blank? contract)
                          (string/blank? name)
                          (string/blank? sym)
                          (string/blank? decimals)))
           :on-press #(re-frame/dispatch [:wallet-legacy.custom-token.ui/add-pressed])}
          (i18n/label :t/add)]}]])))

(defview custom-token-details
  []
  (letsubs [{:keys [address name decimals custom?] :as token}
            [:get-screen-params]]
    [react/keyboard-avoiding-view
     {:style         {:flex 1}
      :ignore-offset true}
     [topbar/topbar {:title name}]
     [react/scroll-view
      {:keyboard-should-persist-taps :handled
       :style                        {:flex 1}}
      [react/view {:padding-horizontal 16}
       [react/view {:padding-vertical 8}
        [quo/text-input
         {:label         (i18n/label :t/contract-address)
          :default-value address
          :multiline     true
          :height        78
          :editable      false}]]
       [react/view {:padding-vertical 8}
        [quo/text-input
         {:label         (i18n/label :t/name)
          :default-value name
          :editable      false}]]
       [react/view {:padding-vertical 8}
        [react/view {:style {:flex-direction :row}}
         [react/view
          {:flex          1
           :padding-right 8}
          [quo/text-input
           {:label         (i18n/label :t/symbol)
            :editable      false
            :show-cancel   false
            :default-value (:symbol token)}]]
         [react/view {:flex 1 :padding-left 8}
          [quo/text-input
           {:label         (i18n/label :t/decimals)
            :show-cancel   false
            :default-value (str decimals)
            :editable      false}]]]]]
      [react/view {:height 24}]
      (when custom?
        [list.item/list-item
         {:theme    :negative
          :title    (i18n/label :t/remove-token)
          :icon     :main-icons/delete
          :on-press #(re-frame/dispatch [:wallet-legacy.custom-token.ui/remove-pressed token
                                         true])}])]]))

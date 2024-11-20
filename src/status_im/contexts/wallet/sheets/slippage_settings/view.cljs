(ns status-im.contexts.wallet.sheets.slippage-settings.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.common.utils :as utils]
            [status-im.contexts.wallet.sheets.slippage-settings.style :as style]
            [utils.i18n :as i18n]
            [utils.money :as money]
            [utils.number :as number]
            [utils.re-frame :as rf]))

(defn- validate-slippage
  [slippage]
  (let [slippage-value (number/parse-float slippage)]
    (cond
      (<= slippage-value 0)
      {:message (i18n/label :t/slippage-should-be-more-than-0)
       :type    :error}
      (> slippage-value constants/max-slippage)
      {:message (i18n/label :t/slippage-cant-be-more-than-30)
       :type    :error}
      (> (count (second (string/split slippage ".")))
         constants/max-slippage-decimal-places)
      {:message (i18n/label :t/max-2-decimals)
       :type    :error}
      (> slippage-value constants/max-recommended-slippage)
      {:message (i18n/label :t/slippage-may-be-higher-than-necessary)
       :type    :warning})))

(defn- on-cancel
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn- update-string-on-keypress
  [k s]
  (if (= :i/backspace k)
    (subs s 0 (dec (count s)))
    (str s k)))

(defn- calculate-receive-amount
  [asset-to-receive amount-out max-slippage]
  (let [receive-token-decimals    (:decimals asset-to-receive)
        amount-out-whole-number   (number/hex->whole amount-out receive-token-decimals)
        amount-out-num            (number/to-fixed amount-out-whole-number
                                                   receive-token-decimals)
        slippage-num              (-> (money/bignumber amount-out-num)
                                      (money/mul (money/bignumber max-slippage))
                                      (money/div (money/bignumber 100)))
        amount-out-minus-slippage (money/sub (money/bignumber amount-out-num) slippage-num)
        display-decimals          (min receive-token-decimals
                                       constants/min-token-decimals-to-display)
        receive-amount            (if (money/above-zero? amount-out-minus-slippage)
                                    (str (utils/sanitized-token-amount-to-display
                                          amount-out-minus-slippage
                                          display-decimals))
                                    0)]
    receive-amount))

(defn view
  []
  (let [current-slippage                (rf/sub [:wallet/swap-max-slippage])
        account-color                   (rf/sub [:wallet/current-viewing-account-color])
        asset-to-receive                (rf/sub [:wallet/swap-asset-to-receive])
        amount-out                      (rf/sub [:wallet/swap-proposal-amount-out])
        [max-slippage set-max-slippage] (rn/use-state (str current-slippage))
        [error set-error]               (rn/use-state nil)
        [custom? set-custom?]           (rn/use-state (not-any? #{current-slippage}
                                                                constants/slippages))
        receive-token-symbol            (:symbol asset-to-receive)
        receive-amount                  (calculate-receive-amount asset-to-receive
                                                                  amount-out
                                                                  max-slippage)
        handle-slippage-change          (rn/use-callback
                                         (fn [value]
                                           (let [new-slippage (update-string-on-keypress value
                                                                                         max-slippage)]
                                             (set-max-slippage new-slippage)
                                             (set-error (validate-slippage new-slippage))))
                                         [max-slippage set-max-slippage set-error])
        on-select-slippage              (rn/use-callback (fn [slippage]
                                                           (set-max-slippage (str slippage))
                                                           (set-custom? (nil? slippage))
                                                           (when slippage (set-error nil)))
                                                         [set-max-slippage set-custom?])
        save-disabled?                  (rn/use-memo (fn []
                                                       (or (= max-slippage (str current-slippage))
                                                           (= (:type error) :error)
                                                           (and custom?
                                                                (empty? max-slippage))))
                                                     [max-slippage current-slippage error custom?])
        on-save                         (rn/use-callback (fn []
                                                           (rf/dispatch [:wallet.swap/set-max-slippage
                                                                         max-slippage])
                                                           (rf/dispatch [:hide-bottom-sheet]))
                                                         [max-slippage])]
    [:<>
     [quo/drawer-top
      {:title       (i18n/label :t/slippage-settings)
       :description (i18n/label :t/slippage-settings-description)}]
     [rn/view {:style style/slippages}
      (map (fn [slippage]
             ^{:key slippage}
             [quo/drawer-action
              (cond-> {:title    (str slippage "%")
                       :on-press #(on-select-slippage slippage)}
                (and (= (str slippage) max-slippage) (not custom?)) (assoc :state :selected))])
           constants/slippages)
      [quo/drawer-action
       (cond-> {:title       (i18n/label :t/custom)
                :action      :input
                :on-press    #(on-select-slippage nil)
                :input-props {:auto-focus               true
                              :customization-color      account-color
                              :placeholder              (i18n/label :t/type-slippage)
                              :right-icon               {:icon-name :i/percentage
                                                         :on-press  identity
                                                         :style-fn  style/percentage-icon}
                              :value                    max-slippage
                              :show-soft-input-on-focus false
                              :on-focus                 (fn []
                                                          (when platform/android?
                                                            (rf/dispatch [:dismiss-keyboard])))}}
         custom? (assoc :state :selected))]]
     (when (and custom? error)
       [quo/info-message
        {:status          (:type error)
         :size            :default
         :container-style style/info-message
         :icon            :i/alert}
        (:message error)])
     [quo/bottom-actions
      {:actions              :two-actions
       :button-one-label     (i18n/label :t/save-changes)
       :button-one-props     {:disabled?           save-disabled?
                              :customization-color account-color
                              :on-press            on-save}
       :button-two-label     (i18n/label :t/cancel)
       :button-two-props     {:on-press            on-cancel
                              :customization-color account-color
                              :type                :grey}
       :description          :top
       :context-tag-props    {:size   24
                              :type   :token
                              :token  receive-token-symbol
                              :amount receive-amount}
       :description-top-text (i18n/label :t/receive-at-least)}]
     (when custom?
       [quo/numbered-keyboard
        {:left-action :dot
         :delete-key? true
         :on-press    handle-slippage-change
         :on-delete   handle-slippage-change}])]))

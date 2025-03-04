(ns status-im.contexts.wallet.trading.swap.components.keyboard
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [status-im.common.controlled-input.utils :as controlled-input]
            [status-im.contexts.wallet.trading.swap.components.footer :as footer]
            [status-im.contexts.wallet.trading.swap.style :as style]
            [utils.re-frame :as rf]
            [utils.string :as utils.string]))

(defn- should-allow-input-char?
  [input-state character token-decimals]
  (let [new-state-amount (-> (controlled-input/input-value input-state)
                             (str character))]
    (utils.string/valid-amount-for-token-decimals? token-decimals
                                                   new-state-amount)))

(defn keyboard-view
  [{:keys [input input-token-decimals]}]
  (fn []
    (let [{:keys [value add-input-char
                  delete-last-input-char
                  clear-input-value]} input
          on-keyboard-long-press      (rn/use-callback
                                       (fn []
                                         (clear-input-value)
                                         ;; TODO: investigate
                                         ;;(rf/dispatch
                                         ;;[:wallet/clean-suggested-routes])
                                       )
                                       [clear-input-value])
          on-keyboard-press           (rn/use-callback
                                       (fn [c]
                                         (when (should-allow-input-char?
                                                value
                                                c
                                                input-token-decimals)
                                           (add-input-char c value)))
                                       [value input-token-decimals add-input-char])
          on-keyboard-delete          (rn/use-callback
                                       (fn []
                                         (delete-last-input-char)
                                         ;; TODO: investigate
                                         ;; (rf/dispatch
                                         ;; [:wallet/clean-swap-proposal
                                         ;;               {:clean-amounts? true
                                         ;;                :clean-approval-transaction?
                                         ;;                true}])
                                       )
                                       [delete-last-input-char])]
      [rn/view
       [footer/view]
       [quo/numbered-keyboard
        {:container-style      style/keyboard-container
         :left-action          :dot
         :delete-key?          true
         :on-press             on-keyboard-press
         :on-delete            on-keyboard-delete
         :on-long-press-delete on-keyboard-long-press}]])))

(defn show
  [{:keys [on-close] :as keyboard-opts} theme]
  (rf/dispatch
   [:show-bottom-sheet
    {:hide-handle?      true
     :theme             theme
     :dim-background?   false
     :shell?            platform/ios?
     :disable-gestures? true
     :on-close          on-close
     :content           (keyboard-view keyboard-opts)}]))

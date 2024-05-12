(ns status-im.contexts.wallet.common.address-input.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.contexts.wallet.common.address-input.style :as style]
            [status-im.contexts.wallet.utils :as wallet.utils]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- on-press-scan-address
  []
  (rn/dismiss-keyboard!)
  (rf/dispatch [:open-modal :screen/wallet.scan-address]))

(defn view
  [{:keys [input-value validate clear-input set-validation-message set-input-value input-title
           accessibility-label]}]
  (let [scanned-address (rf/sub [:wallet/scanned-address])
        empty-input?    (and (string/blank? input-value)
                             (string/blank? scanned-address))
        on-change-text  (fn [new-text]
                          (set-validation-message (validate new-text))
                          (set-input-value new-text)
                          (reagent/flush)
                          (if (and (not (string/blank? new-text)) (nil? (validate new-text)))
                            (debounce/debounce-and-dispatch [:wallet/get-address-details new-text]
                                                            500)
                            (rf/dispatch [:wallet/clear-address-activity]))
                          (when (and scanned-address (not= scanned-address new-text))
                            (wallet.utils/clear-activity-and-scanned-address)))
        paste-on-input  #(clipboard/get-string
                          (fn [clipboard-text]
                            (on-change-text clipboard-text)))]
    (rn/use-effect (fn []
                     (when-not (string/blank? scanned-address)
                       (on-change-text scanned-address)))
                   [scanned-address])
    [rn/view {:style style/input-container}
     [quo/input
      {:accessibility-label accessibility-label
       :placeholder         (i18n/label :t/address-placeholder)
       :container-style     style/input
       :label               input-title
       :auto-capitalize     :none
       :multiline?          true
       :on-clear            clear-input
       :return-key-type     :done
       :clearable?          (not empty-input?)
       :on-change-text      on-change-text
       :button              (when empty-input?
                              {:on-press paste-on-input
                               :text     (i18n/label :t/paste)})
       :value               (or scanned-address input-value)}]
     [quo/button
      {:type            :outline
       :on-press        on-press-scan-address
       :container-style style/scan
       :size            40
       :icon-only?      true}
      :i/scan]]))

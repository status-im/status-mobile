(ns status-im2.contexts.syncing.enter-sync-code.view
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [react-native.clipboard :as clipboard]
            [utils.i18n :as i18n]
            [status-im2.contexts.syncing.enter-sync-code.style :as style]
            [utils.debounce :as debounce]
            [utils.re-frame :as rf]
            [status-im2.contexts.syncing.utils :as sync-utils]))

(defn view
  []
  (let [sync-code-value (reagent/atom "")]
    (fn []
      (let [invalid?           false
            show-paste-button? (string/blank? @sync-code-value)
            profile-color      (rf/sub [:profile/customization-color])]
        [:<>
         [rn/view
          {:style style/label-container}
          [quo/text
           {:style  style/label-pairing
            :weight :medium
            :size   :paragraph-2}
           (i18n/label :t/type-pairing-code)]]
         [rn/view {:style style/container-text-input}
          [rn/view {:style (style/text-input-container invalid?)}
           [rn/text-input
            {:style                  (style/text-input)
             :value                  @sync-code-value
             :placeholder            (i18n/label :t/scan-sync-code-placeholder)
             :on-change-text         (fn [scan-code]
                                       (reset! sync-code-value scan-code)
                                       (reagent/flush))
             :blur-on-submit         true
             :return-key-type        :done
             :accessibility-label    :enter-sync-code-input
             :auto-capitalize        :none
             :placeholder-text-color colors/white-opa-40
             :multiline              true}]
           (if show-paste-button?
             [quo/button
              {:on-press        (fn [_]
                                  (clipboard/get-string #(reset! sync-code-value %)))
               :type            :outline
               :container-style style/button-paste
               :size            24}
              (i18n/label :t/paste)]

             [rn/pressable
              {:accessibility-label :input-right-icon
               :style               style/right-icon-touchable-area
               :on-press            (fn [_]
                                      (reset! sync-code-value nil))}
              [quo/icon :i/clear style/clear-icon]])]]
         [quo/button
          {:type                :primary
           :disabled?           (string/blank? @sync-code-value)
           :customization-color profile-color
           :container-style     style/continue-button-container
           :on-press            (fn [_]
                                  (if (sync-utils/valid-connection-string? @sync-code-value)
                                    (debounce/debounce-and-dispatch
                                     [:syncing/input-connection-string-for-bootstrapping
                                      @sync-code-value]
                                     300)
                                    (rf/dispatch [:toasts/upsert
                                                  {:icon :i/info
                                                   :icon-color colors/danger-50
                                                   :theme :dark
                                                   :text (i18n/label
                                                          :t/error-this-is-not-a-sync-qr-code)}])))}
          (i18n/label :t/confirm)]]))))

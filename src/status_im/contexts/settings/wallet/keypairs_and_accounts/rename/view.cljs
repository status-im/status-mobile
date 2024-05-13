(ns status-im.contexts.settings.wallet.keypairs-and-accounts.rename.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.floating-button-page.view :as floating-button-page]
            [status-im.common.validation.keypair :as keypair-validator]
            [status-im.constants :as constants]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.rename.style :as style]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn navigate-back [] (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [{:keys [name key-uid]}                          (rf/sub [:get-screen-params])
        customization-color                             (rf/sub [:profile/customization-color])
        [unsaved-keypair-name set-unsaved-keypair-name] (rn/use-state name)
        [error-msg set-error-msg]                       (rn/use-state nil)
        [typing? set-typing?]                           (rn/use-state false)
        validate-keypair-name                           (rn/use-callback
                                                         (debounce/debounce
                                                          (fn [name]
                                                            (set-error-msg
                                                             (keypair-validator/validation-keypair-name
                                                              name))
                                                            (set-typing? false))
                                                          300))
        on-change-text                                  (rn/use-callback (fn [text]
                                                                           (set-typing? true)
                                                                           (set-unsaved-keypair-name
                                                                            text)
                                                                           (validate-keypair-name text)))
        on-clear                                        (rn/use-callback (fn []
                                                                           (on-change-text "")))
        on-save                                         (rn/use-callback #(rf/dispatch
                                                                           [:wallet/rename-keypair
                                                                            {:key-uid key-uid
                                                                             :keypair-name
                                                                             unsaved-keypair-name}])
                                                                         [unsaved-keypair-name key-uid])]
    [floating-button-page/view
     {:header [quo/page-nav
               {:icon-name           :i/close
                :on-press            navigate-back
                :accessibility-label :top-bar}]
      :footer [quo/bottom-actions
               {:actions          :one-action
                :button-one-label (i18n/label :t/save)
                :button-one-props {:disabled?           (or typing?
                                                            (string/blank? unsaved-keypair-name)
                                                            (not (string/blank? error-msg)))
                                   :customization-color customization-color
                                   :on-press            on-save}
                :container-style  style/bottom-action}]}
     [quo/page-top
      {:container-style style/header-container
       :title           (i18n/label :t/rename-key-pair)
       :description     :context-tag
       :context-tag     {:type    :icon
                         :size    24
                         :context name
                         :icon    :i/seed-phrase}}]
     [quo/input
      {:container-style {:margin-horizontal 20}
       :placeholder     (i18n/label :t/keypair-name-input-placeholder)
       :label           (i18n/label :t/keypair-name)
       :default-value   unsaved-keypair-name
       :char-limit      constants/key-pair-name-max-length
       :max-length      constants/key-pair-name-max-length
       :auto-focus      true
       :clearable?      (not (string/blank? unsaved-keypair-name))
       :on-clear        on-clear
       :on-change-text  on-change-text
       :error?          (not (string/blank? error-msg))}]
     (when-not (string/blank? error-msg)
       [quo/info-message
        {:type            :error
         :size            :default
         :icon            :i/info
         :container-style style/error-container}
        error-msg])]))


(ns status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.encrypted-qr.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [status-im.common.qr-codes.view :as qr-codes]
    [status-im.common.resources :as resources]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.encrypted-qr.countdown.view
     :as countdown]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.encrypted-qr.style
     :as style]
    [status-im.contexts.syncing.utils :as sync-utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn navigate-back [] (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [{:keys [key-uid]}             (rf/sub [:get-screen-params])
        {:keys [customization-color]} (rf/sub [:profile/profile-with-image])
        [code set-code]               (rn/use-state nil)
        valid-connection-string?      (rn/use-memo #(sync-utils/valid-connection-string? code) [code])
        validate-and-set-code         (rn/use-callback (fn [connection-string]
                                                         (when (sync-utils/valid-connection-string?
                                                                connection-string)
                                                           (set-code connection-string))))
        cleanup-clock                 (rn/use-callback #(set-code nil))
        on-auth-success               (rn/use-callback (fn [entered-password]
                                                         (rf/dispatch
                                                          [:wallet/get-keypair-export-connection
                                                           {:sha3-pwd        entered-password
                                                            :keypair-key-uid key-uid
                                                            :callback        validate-and-set-code}]))
                                                       [key-uid])]
    [rn/view {:style (style/container-main)}
     [rn/scroll-view
      [quo/page-nav
       {:type       :no-title
        :icon-name  :i/close
        :background :blur
        :on-press   navigate-back}]
      [rn/view {:style style/page-container}
       [rn/view {:style style/title-container}
        [quo/text
         {:size   :heading-1
          :weight :semi-bold
          :style  {:color colors/white}}
         (i18n/label :t/encrypted-key-pairs)]]
       [rn/view {:style style/qr-container}
        (if valid-connection-string?
          [qr-codes/qr-code {:url code}]
          [rn/view {:style {:flex-direction :row}}
           [rn/image
            {:source (resources/get-image :qr-code)
             :style  {:width            "100%"
                      :background-color colors/white-opa-70
                      :border-radius    12
                      :aspect-ratio     1}}]])
        (when valid-connection-string?
          [rn/view {:style style/valid-cs-container}
           [rn/view {:style style/sub-text-container}
            [quo/text
             {:size  :paragraph-2
              :style {:color colors/white-opa-40}}
             (i18n/label :t/encrypted-key-pairs-code)]
            [countdown/view cleanup-clock]]
           [quo/input
            {:default-value  code
             :type           :password
             :default-shown? true
             :editable       false}]
           [quo/button
            {:on-press        (fn []
                                (clipboard/set-string code)
                                (rf/dispatch [:toasts/upsert
                                              {:type :positive
                                               :text (i18n/label
                                                      :t/sharing-copied-to-clipboard)}]))
             :type            :grey
             :container-style {:margin-top 12}
             :icon-left       :i/copy}
            (i18n/label :t/copy-qr)]])
        (when-not valid-connection-string?
          [rn/view {:style style/standard-auth}
           [standard-auth/slide-button
            {:blur?                 true
             :size                  :size-40
             :track-text            (i18n/label :t/slide-to-reveal-qr-code)
             :customization-color   customization-color
             :on-auth-success       on-auth-success
             :auth-button-label     (i18n/label :t/reveal-qr-code)
             :auth-button-icon-left :i/reveal}]])]]
      (when-not valid-connection-string?
        [quo/text
         {:size  :paragraph-2
          :style style/warning-text}
         (i18n/label :t/make-sure-no-camera-warning)])]]))

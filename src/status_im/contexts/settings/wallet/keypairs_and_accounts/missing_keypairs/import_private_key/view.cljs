(ns status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.import-private-key.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.import-private-key.style
     :as style]
    [status-im.contexts.wallet.common.validation :as validation]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [keypair                       (rf/sub [:get-screen-params])
        blur?                         true
        insets                        (safe-area/get-insets)
        customization-color           (rf/sub [:profile/customization-color])
        [private-key set-private-key] (rn/use-state "")
        [flow-state set-flow-state]   (rn/use-state nil)
        error?                        (case flow-state
                                        (:incorrect-private-key
                                         :invalid-private-key) true
                                        false)
        clear-errors                  (rn/use-callback
                                       #(set-flow-state nil))
        show-invalid                  (rn/use-callback
                                       #(set-flow-state :invalid-private-key))
        show-correct                  (rn/use-callback
                                       #(set-flow-state :correct-private-key))
        show-incorrect                (rn/use-callback
                                       #(set-flow-state :incorrect-private-key))
        verify-private-key            (rn/use-callback
                                       (fn [input]
                                         (rf/dispatch [:wallet/verify-private-key-for-keypair
                                                       (:key-uid keypair)
                                                       (security/mask-data input)
                                                       show-correct
                                                       show-incorrect]))
                                       [keypair])
        validate-private-key          (rn/use-callback
                                       (debounce/debounce
                                        (fn [input]
                                          (if-not (validation/private-key? input)
                                            (show-invalid)
                                            (do (clear-errors)
                                                (verify-private-key input))))
                                        500)
                                       [verify-private-key])
        on-change                     (rn/use-callback
                                       (fn [input]
                                         (set-private-key input)
                                         (validate-private-key input))
                                       [validate-private-key])
        on-paste                      (rn/use-callback
                                       #(clipboard/get-string
                                         (fn [clipboard]
                                           (when-not (empty? clipboard)
                                             (on-change clipboard))))
                                       [on-change])
        on-import-error               (rn/use-callback
                                       (fn [_error]
                                         (rf/dispatch [:hide-bottom-sheet])))
        on-import-success             (rn/use-callback
                                       (fn []
                                         (rf/dispatch [:hide-bottom-sheet])
                                         (rf/dispatch [:navigate-back]))
                                       [])
        on-auth-success               (rn/use-callback
                                       (fn [password]
                                         (rf/dispatch [:wallet/import-missing-keypair-by-private-key
                                                       {:keypair-key-uid (:key-uid keypair)
                                                        :private-key     (security/mask-data private-key)
                                                        :password        password
                                                        :on-success      on-import-success
                                                        :on-error        on-import-error}]))
                                       [keypair private-key on-import-success on-import-error])]
    [quo/overlay {:type :shell}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:margin-top (:top insets)
                                   :background :blur
                                   :icon-name  :i/close
                                   :on-press   navigate-back}]
       :footer                   [rn/view {:style style/slide-container}
                                  [standard-auth/slide-auth
                                   {:blur?                 true
                                    :size                  :size-48
                                    :customization-color   customization-color
                                    :track-text            (i18n/label :t/slide-to-import)
                                    :on-success            on-auth-success
                                    :auth-button-label     (i18n/label :t/import-key-pair)
                                    :auth-button-icon-left :i/key
                                    :disabled?             (or error? (string/blank? private-key))
                                    :dependencies          [on-auth-success]}]]}
      [quo/page-top
       {:blur?       true
        :title       (i18n/label :t/import-private-key)
        :description :context-tag
        :context-tag {:type    :icon
                      :icon    :i/password
                      :size    24
                      :context (:name keypair)}}]
      [rn/view {:style style/form-container}
       [quo/input
        {:accessibility-label :import-private-key
         :placeholder         (i18n/label :t/enter-private-key-placeholder)
         :label               (i18n/label :t/private-key)
         :type                :password
         :blur?               blur?
         :error?              error?
         :return-key-type     :done
         :auto-focus          true
         :on-change-text      on-change
         :button              (when (empty? private-key)
                                {:on-press on-paste
                                 :text     (i18n/label :t/paste)})
         :default-value       private-key}]
       (when flow-state
         [quo/info-message
          {:status (if (= flow-state :correct-private-key) :success :error)
           :size   :default
           :icon   :i/info}
          (case flow-state
            :correct-private-key   (i18n/label :t/correct-private-key)
            :invalid-private-key   (i18n/label :t/invalid-private-key)
            :incorrect-private-key (i18n/label :t/incorrect-private-key)
            nil)])]]]))

(ns status-im.contexts.settings.wallet.keypairs-and-accounts.missing-keypairs.import-seed-phrase.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.enter-seed-phrase.view :as enter-seed-phrase]
    [status-im.common.standard-authentication.core :as standard-auth]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn import-seed-phrase-controls
  [{:keys [submit-disabled?
           container-style
           prepare-seed-phrase
           seed-phrase
           set-incorrect-seed-phrase
           focus-input]}]
  (let [keypair             (quo.theme/use-screen-params)
        customization-color (rf/sub [:profile/customization-color])
        show-errors         (rn/use-callback
                             (fn [_error]
                               (js/setTimeout
                                (fn []
                                  (focus-input)
                                  (reagent/next-tick set-incorrect-seed-phrase))
                                600)))
        on-import-error     (rn/use-callback
                             (fn [_error]
                               (rf/dispatch [:hide-bottom-sheet])
                               (show-errors)))
        on-import-success   (rn/use-callback
                             (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (rf/dispatch [:navigate-back])))
        on-auth-success     (rn/use-callback
                             (fn [password]
                               (rf/dispatch [:wallet/import-missing-keypair-by-seed-phrase
                                             {:keypair-key-uid (:key-uid keypair)
                                              :seed-phrase     (prepare-seed-phrase seed-phrase)
                                              :password        password
                                              :on-success      on-import-success
                                              :on-error        on-import-error}]))
                             [keypair seed-phrase on-import-success on-import-error])]
    [standard-auth/slide-auth
     {:blur?                 true
      :size                  :size-48
      :customization-color   customization-color
      :track-text            (i18n/label :t/slide-to-import)
      :on-success            on-auth-success
      :auth-button-label     (i18n/label :t/import-key-pair)
      :auth-button-icon-left :i/seed
      :container-style       container-style
      :disabled?             submit-disabled?
      :dependencies          [on-auth-success]}]))

(defn view
  []
  (let [keypair (quo.theme/use-screen-params)]
    [quo/overlay {:type :shell}
     [enter-seed-phrase/screen
      {:keypair         keypair
       :navigation-icon :i/close
       :render-controls import-seed-phrase-controls
       :title           (i18n/label :t/enter-recovery-phrase)
       :initial-insets  (safe-area/get-insets)}]]))

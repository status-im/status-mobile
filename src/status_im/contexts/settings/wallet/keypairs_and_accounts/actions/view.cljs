(ns status-im.contexts.settings.wallet.keypairs-and-accounts.actions.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.remove.view :as remove-keypair]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [drawer-props keypair]}]
  (let [has-paired-device     (rf/sub [:pairing/has-paired-devices])
        missing-keypair?      (= (:stored drawer-props) :missing)
        on-scan-qr            (rn/use-callback #(rf/dispatch [:open-modal
                                                              :screen/settings.scan-keypair-qr
                                                              [(:key-uid keypair)]])
                                               [keypair])
        on-show-qr            (rn/use-callback #(rf/dispatch [:open-modal
                                                              :screen/settings.encrypted-keypair-qr
                                                              keypair])
                                               [keypair])
        on-remove-keypair     (rn/use-callback #(rf/dispatch
                                                 [:show-bottom-sheet
                                                  {:theme   :dark
                                                   :content (fn []
                                                              [remove-keypair/view keypair])}])
                                               [keypair])
        on-rename-keypair     (rn/use-callback #(rf/dispatch [:open-modal :screen/settings.rename-keypair
                                                              keypair])
                                               [keypair])
        on-import-seed-phrase (rn/use-callback
                               #(rf/dispatch [:open-modal
                                              :screen/settings.missing-keypair.import-seed-phrase
                                              keypair])
                               [keypair])
        on-import-private-key (rn/use-callback
                               #(rf/dispatch [:open-modal
                                              :screen/settings.missing-keypair-import-private-key
                                              keypair])
                               [keypair])]
    [:<>
     [quo/drawer-top drawer-props]
     [quo/action-drawer
      [(when has-paired-device
         (if-not missing-keypair?
           [{:blur?               true
             :icon                :i/qr-code
             :accessibility-label :show-key-pr-qr
             :label               (i18n/label :t/show-encrypted-qr-of-key-pair)
             :on-press            on-show-qr}]
           [{:blur?               true
             :icon                :i/scan
             :accessibility-label :import-by-scan-qr
             :label               (i18n/label :t/import-by-scanning-encrypted-qr)
             :on-press            on-scan-qr}]))
       (when (= (:type drawer-props) :keypair)
         [(when missing-keypair?
            (case (:type keypair)
              :seed {:blur?               true
                     :icon                :i/seed
                     :accessibility-label :import-seed-phrase
                     :label               (i18n/label :t/import-by-entering-recovery-phrase)
                     :on-press            on-import-seed-phrase}
              :key  {:blur?               true
                     :icon                :i/key
                     :accessibility-label :import-private-key
                     :label               (i18n/label :t/import-by-entering-private-key)
                     :on-press            on-import-private-key}
              nil))
          {:blur?               true
           :icon                :i/edit
           :accessibility-label :rename-key-pair
           :label               (i18n/label :t/rename-key-pair)
           :on-press            on-rename-keypair}
          {:blur?               true
           :icon                :i/delete
           :accessibility-label :remove-key-pair
           :add-divider?        true
           :danger?             true
           :label               (i18n/label :t/remove-key-pair-and-derived-accounts)
           :on-press            on-remove-keypair}])]]]))

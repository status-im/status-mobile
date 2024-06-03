(ns status-im.contexts.settings.wallet.keypairs-and-accounts.actions.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.remove.view :as remove-key-pair]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [props keypair]
  (let [has-paired-device (rf/sub [:pairing/has-paired-devices])
        missing-keypair?  (rn/use-memo #(= (:stored props) :missing) [props])
        on-scan-qr        (rn/use-callback #(rf/dispatch [:open-modal :screen/settings.scan-keypair-qr
                                                          [(:key-uid keypair)]])
                                           [keypair])
        on-show-qr        (rn/use-callback #(rf/dispatch [:open-modal
                                                          :screen/settings.encrypted-key-pair-qr
                                                          keypair])
                                           [keypair])
        on-remove-keypair (rn/use-callback #(rf/dispatch
                                             [:show-bottom-sheet
                                              {:theme   :dark
                                               :content (fn []
                                                          [remove-key-pair/view keypair])}])
                                           [keypair])
        on-rename-keypair (rn/use-callback #(rf/dispatch [:open-modal :screen/settings.rename-keypair
                                                          keypair])
                                           [keypair])]
    [:<>
     [quo/drawer-top props]
     [quo/action-drawer
      [(when has-paired-device
         (concat
          (if-not missing-keypair?
            [{:icon                :i/qr-code
              :accessibility-label :show-key-pr-qr
              :label               (i18n/label :t/show-encrypted-qr-of-key-pairs)
              :on-press            on-show-qr}]
            [{:icon                :i/scan
              :accessibility-label :import-by-scan-qr
              :label               (i18n/label :t/import-by-scanning-encrypted-qr)
              :on-press            on-scan-qr}])))
       (when (= (:type props) :keypair)
         (concat
          [{:icon                :i/edit
            :accessibility-label :rename-key-pair
            :label               (i18n/label :t/rename-key-pair)
            :on-press            on-rename-keypair}]
          [{:icon                :i/delete
            :accessibility-label :remove-key-pair
            :add-divider?        true
            :danger?             true
            :label               (i18n/label :t/remove-key-pair-and-derived-accounts)
            :on-press            on-remove-keypair}]))]]]))

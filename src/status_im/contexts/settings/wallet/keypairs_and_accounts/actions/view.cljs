(ns status-im.contexts.settings.wallet.keypairs-and-accounts.actions.view
  (:require [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-rename-request
  [data]
  (rf/dispatch [:open-modal :screen/settings.rename-keypair data]))

(defn on-show-qr
  [data]
  (rf/dispatch [:open-modal :screen/settings.encrypted-key-pair-qr data]))

(defn view
  [props data]
  (let [has-paired-device (rf/sub [:pairing/has-paired-devices])]
    [:<>
     [quo/drawer-top props]
     [quo/action-drawer
      [(when has-paired-device
         [{:icon                :i/qr-code
           :accessibility-label :show-key-pr-qr
           :label               (i18n/label :t/show-encrypted-qr-of-key-pairs)
           :on-press            #(on-show-qr data)}])
       (when (= (:type props) :keypair)
         [{:icon                :i/edit
           :accessibility-label :rename-key-pair
           :label               (i18n/label :t/rename-key-pair)
           :on-press            #(on-rename-request data)}])]]]))

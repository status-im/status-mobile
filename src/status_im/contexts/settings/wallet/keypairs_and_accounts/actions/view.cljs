(ns status-im.contexts.settings.wallet.keypairs-and-accounts.actions.view
  (:require [quo.core :as quo]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.remove.view :as remove-key-pair]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-rename-key-pair
  [key-pair]
  (rf/dispatch [:open-modal :screen/settings.rename-keypair key-pair]))

(defn on-remove-key-pair
  [key-pair]
  (rf/dispatch [:show-bottom-sheet
                {:theme   :dark
                 :content (fn []
                            [remove-key-pair/view key-pair])}]))

(defn on-show-qr
  [data]
  (rf/dispatch [:open-modal :screen/settings.encrypted-key-pair-qr data]))

(defn view
  [props key-pair]
  (let [has-paired-device (rf/sub [:pairing/has-paired-devices])]
    [:<>
     [quo/drawer-top props]
     [quo/action-drawer
      [(when has-paired-device
         [{:icon                :i/qr-code
           :accessibility-label :show-key-pr-qr
           :label               (i18n/label :t/show-encrypted-qr-of-key-pairs)
           :on-press            #(on-show-qr key-pair)}])
       (when (= (:type props) :keypair)
         (concat
          [{:icon                :i/edit
            :accessibility-label :rename-key-pair
            :label               (i18n/label :t/rename-key-pair)
            :on-press            #(on-rename-key-pair key-pair)}]
          [{:icon                :i/delete
            :accessibility-label :remove-key-pair
            :add-divider?        true
            :danger?             true
            :label               (i18n/label :t/remove-key-pair-and-derived-accounts)
            :on-press            #(on-remove-key-pair key-pair)}]))]]]))

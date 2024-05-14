(ns status-im.contexts.settings.wallet.keypairs-and-accounts.actions.view
  (:require [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-rename-request
  [data]
  (rf/dispatch [:open-modal :screen/settings.rename-keypair data]))

(defn view
  [props data]
  [:<>
   [quo/drawer-top props]
   [quo/action-drawer
    [(when (= (:type props) :keypair)
       [{:icon                :i/edit
         :accessibility-label :rename-key-pair
         :label               (i18n/label :t/rename-key-pair)
         :on-press            #(on-rename-request data)}])]]])

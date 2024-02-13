(ns status-im.contexts.wallet.create-account.new-keypair.keypair-name.view
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.create-account.new-keypair.keypair-name.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [keypair-name (reagent/atom "")]
    (fn []
      (let [{:keys [secret-phrase]} (rf/sub [:wallet/ui])]
        [rn/view {:style {:flex 1}}
         [quo/page-nav
          {:icon-name           :i/arrow-left
           :on-press            #(rf/dispatch [:navigate-back])
           :accessibility-label :top-bar}]
         [quo/text-combinations
          {:container-style style/header-container
           :title           (i18n/label :t/keypair-name)
           :description     (i18n/label :t/keypair-name-description)}]
         [quo/input
          {:container-style {:margin-horizontal 20}
           :placeholder     (i18n/label :t/keypair-name-input-placeholder)
           :label           (i18n/label :t/keypair-name)
           :char-limit      15
           :on-change-text  #(reset! keypair-name %)}]
         [quo/bottom-actions
          {:actions          :one-action
           :button-one-label (i18n/label :t/continue)
           :button-one-props {:disabled?           (or (zero? (count @keypair-name))
                                                       (> (count @keypair-name) 15))
                              :customization-color :blue
                              :on-press            #(native-module/create-account-from-mnemonic
                                                     {:MnemonicPhrase (string/join " " secret-phrase)}
                                                     (fn [new-keypair]
                                                       (rf/dispatch [:wallet/new-keypair-continue
                                                                     {:new-keypair (assoc
                                                                                    new-keypair
                                                                                    :keypair-name
                                                                                    @keypair-name)}])))
                             }
           :container-style  style/bottom-action}]]))))

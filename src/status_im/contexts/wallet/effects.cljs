(ns status-im.contexts.wallet.effects
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [re-frame.core :as rf]))

(rf/reg-fx
 :effects.wallet/create-account-from-mnemonic
 (fn [{:keys [seed-phrase keypair-name]}]
   (native-module/create-account-from-mnemonic
    {:MnemonicPhrase (if (string? seed-phrase)
                       seed-phrase
                       (string/join " " seed-phrase))}
    (fn [new-keypair]
      (rf/dispatch [:wallet/new-keypair-created
                    {:new-keypair (assoc new-keypair :keypair-name keypair-name)}])))))

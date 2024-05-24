(ns status-im.contexts.wallet.effects
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [re-frame.core :as rf]
    [taoensso.timbre :as log]))

(rf/reg-fx
 :effects.wallet/create-account-from-mnemonic
 (fn [{:keys [mnemonic-phrase paths on-success]
       :or   {paths []}}]
   (let [phrase (condp #(%1 %2) mnemonic-phrase
                  string? mnemonic-phrase
                  coll?   (string/join " " mnemonic-phrase)
                  (log/error "Unexpected value " mnemonic-phrase))]
     (native-module/create-account-from-mnemonic
      {:MnemonicPhrase phrase
       :paths          paths}
      on-success))))

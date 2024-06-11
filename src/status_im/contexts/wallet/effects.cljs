(ns status-im.contexts.wallet.effects
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [re-frame.core :as rf]
    [status-im.common.json-rpc.events :as json-rpc]
    [taoensso.timbre :as log]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(defn- error-message
  [kw]
  (-> kw symbol str))

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

(defn validate-mnemonic
  [mnemonic]
  (-> mnemonic
      (security/safe-unmask-data)
      (native-module/validate-mnemonic)
      (promesa/then (fn [result]
                      (let [{:keys [keyUID]} (transforms/json->clj result)]
                        {:key-uid keyUID})))))

(rf/reg-fx
 :multiaccount/validate-mnemonic
 (fn [[mnemonic on-success on-error]]
   (-> (validate-mnemonic mnemonic)
       (promesa/then (fn [{:keys [key-uid]}]
                       (when (fn? on-success)
                         (on-success mnemonic key-uid))))
       (promesa/catch (fn [error]
                        (when (and error (fn? on-error))
                          (on-error error)))))))

(defn make-seed-phrase-fully-operable
  [mnemonic password]
  (promesa/create
   (fn [resolver rejecter]
     (json-rpc/call {:method     "accounts_makeSeedPhraseKeypairFullyOperable"
                     :params     [(security/safe-unmask-data mnemonic)
                                  (-> password security/safe-unmask-data native-module/sha3)]
                     :on-error   (fn [error]
                                   (rejecter (ex-info (str error) {:error error})))
                     :on-success (fn [value]
                                   (resolver {:value value}))}))))

(defn import-keypair-by-seed-phrase
  [keypair-key-uid seed-phrase password]
  (-> (validate-mnemonic seed-phrase)
      (promesa/then
       (fn [{:keys [key-uid]}]
         (if (not= keypair-key-uid key-uid)
           (promesa/rejected
            (ex-info
             (error-message :import-keypair-by-seed-phrase/import-error)
             {:hint :incorrect-seed-phrase-for-keypair}))
           (make-seed-phrase-fully-operable seed-phrase password))))
      (promesa/catch
        (fn [error]
          (promesa/rejected
           (ex-info
            (error-message :import-keypair-by-seed-phrase/import-error)
            (ex-data error)))))))

(rf/reg-fx
 :import-keypair-by-seed-phrase
 (fn [{:keys [keypair-key-uid seed-phrase password on-success on-error]}]
   (-> (import-keypair-by-seed-phrase keypair-key-uid seed-phrase password)
       (promesa/then (fn [_result]
                       (cond
                         (vector? on-success) (rf/dispatch on-success)
                         (fn? on-success)     (on-success))))
       (promesa/catch (fn [error]
                        (cond
                          (vector? on-error) (rf/dispatch (conj on-error error))
                          (fn? on-error)     (on-error error)))))))

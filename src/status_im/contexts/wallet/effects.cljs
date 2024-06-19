(ns status-im.contexts.wallet.effects
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [status-im.common.json-rpc.events :as json-rpc]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
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

(defn create-account-from-private-key
  [private-key]
  (-> private-key
      (security/safe-unmask-data)
      (native-module/create-account-from-private-key)
      (promesa/then (fn [result]
                      (let [{:keys [address emojiHash keyUid
                                    publicKey privateKey]} (transforms/json->clj result)]
                        {:address     address
                         :emoji-hash  emojiHash
                         :key-uid     keyUid
                         :public-key  publicKey
                         :private-key privateKey})))))

(rf/reg-fx
 :effects.wallet/create-account-from-private-key
 (fn [[private-key on-success on-error]]
   (-> (create-account-from-private-key private-key)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

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

(defn import-missing-keypair-by-seed-phrase
  [keypair-key-uid seed-phrase password]
  (-> (validate-mnemonic seed-phrase)
      (promesa/then
       (fn [{:keys [key-uid]}]
         (if (not= keypair-key-uid key-uid)
           (promesa/rejected
            (ex-info
             (error-message :import-missing-keypair-by-seed-phrase/import-error)
             {:hint :incorrect-seed-phrase-for-keypair}))
           (make-seed-phrase-fully-operable seed-phrase password))))
      (promesa/catch
        (fn [error]
          (promesa/rejected
           (ex-info
            (error-message :import-missing-keypair-by-seed-phrase/import-error)
            (ex-data error)))))))

(rf/reg-fx
 :effects.wallet/import-missing-keypair-by-seed-phrase
 (fn [{:keys [keypair-key-uid seed-phrase password on-success on-error]}]
   (-> (import-missing-keypair-by-seed-phrase keypair-key-uid seed-phrase password)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

(defn verify-private-key-for-keypair
  [keypair-key-uid private-key]
  (-> (create-account-from-private-key private-key)
      (promesa/then
       (fn [{:keys [key-uid] :as result}]
         (if (= keypair-key-uid key-uid)
           result
           (promesa/rejected
            (ex-info
             (error-message :verify-private-key-for-keypair/verification-error)
             {:hint :incorrect-private-key-for-keypair})))))))

(rf/reg-fx
 :effects.wallet/verify-private-key-for-keypair
 (fn [{:keys [keypair-key-uid private-key on-success on-error]}]
   (-> (verify-private-key-for-keypair keypair-key-uid private-key)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

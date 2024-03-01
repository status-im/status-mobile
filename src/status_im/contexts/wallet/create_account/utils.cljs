(ns status-im.contexts.wallet.create-account.utils)

(defn prepare-new-keypair
  [{:keys [new-keypair address account-name account-color emoji derivation-path]}]
  (assoc new-keypair
         :name         (:keypair-name new-keypair)
         :key-uid      (:keyUid new-keypair)
         :type         :seed
         :derived-from address
         :accounts     [{:keypair-name (:keypair-name new-keypair)
                         :key-uid      (:keyUid new-keypair)
                         :seed-phrase  (:mnemonic new-keypair)
                         :public-key   (:publicKey new-keypair)
                         :name         account-name
                         :type         :seed
                         :emoji        emoji
                         :colorID      account-color
                         :path         derivation-path
                         :address      (:address new-keypair)}]))

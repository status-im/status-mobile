(ns status-im.contexts.settings.wallet.effects
  (:require [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.contexts.syncing.utils :as sync-utils]
            [utils.re-frame :as rf]
            [utils.security.core :as security]
            [utils.transforms :as transforms]))

(rf/reg-fx :effects.connection-string/export-keypair
 (fn [{:keys [key-uid sha3-pwd keypair-key-uid on-success on-fail]}]
   (let [config-map (transforms/clj->json {:senderConfig {:loggedInKeyUid   key-uid
                                                          :keystorePath     ""
                                                          :keypairsToExport [keypair-key-uid]
                                                          :password         (security/safe-unmask-data
                                                                             sha3-pwd)}
                                           :serverConfig {:timeout 0}})]
     (-> (native-module/get-connection-string-for-exporting-keypairs-keystores
          config-map)
         (promesa/then (fn [response]
                         (if (sync-utils/valid-connection-string? response)
                           (on-success response)
                           (on-fail (js/Error.
                                     "generic-error: failed to get connection string")))))
         (promesa/catch on-fail)))))

(rf/reg-fx :effects.connection-string/import-keypair
 (fn [{:keys [key-uid sha3-pwd keypairs-key-uids connection-string on-success on-fail]}]
   (let [config-map (transforms/clj->json {:receiverConfig
                                           {:loggedInKeyUid   key-uid
                                            :keystorePath     ""
                                            :password         (security/safe-unmask-data
                                                               sha3-pwd)
                                            :keypairsToImport keypairs-key-uids}})]
     (-> (native-module/input-connection-string-for-importing-keypairs-keystores
          connection-string
          config-map)
         (promesa/then #(on-success keypairs-key-uids))
         (promesa/catch on-fail)))))

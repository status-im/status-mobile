(ns status-im.multiaccounts.recover.core
  (:require [re-frame.core :as re-frame]
            [status-im2.constants :as constants]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [native-module.core :as native-module]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(re-frame/reg-fx
 ::import-multiaccount
 (fn [{:keys [passphrase password success-event]}]
   (log/debug "[recover] ::import-multiaccount")
   (native-module/multiaccount-import-mnemonic
    passphrase
    password
    (fn [result]
      (let [{:keys [id] :as root-data}
            (multiaccounts.create/normalize-multiaccount-data-keys
             (types/json->clj result))]
        (native-module.core/multiaccount-derive-addresses
         id
         [constants/path-wallet-root
          constants/path-eip1581
          constants/path-whisper
          constants/path-default-wallet]
         (fn [result]
           (let [derived-data (multiaccounts.create/normalize-derived-data-keys
                               (types/json->clj result))
                 public-key   (get-in derived-data [constants/path-whisper-keyword :public-key])]
             (native-module/gfycat-identicon-async
              public-key
              (fn [name _]
                (let [derived-data-extended
                      (update derived-data constants/path-whisper-keyword assoc :name name)]
                  (re-frame/dispatch [success-event root-data derived-data-extended]))))))))))))


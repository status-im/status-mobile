(ns legacy.status-im.multiaccounts.create.core
  (:require
    [legacy.status-im.utils.deprecated-types :as types]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]
    [utils.re-frame :as rf]))

(defn normalize-derived-data-keys
  [derived-data]
  (->> derived-data
       (map (fn [[path {:keys [publicKey compressedKey] :as data}]]
              [path
               (cond-> (-> data
                           (dissoc :publicKey :compressedKey)
                           (assoc
                            :public-key     publicKey
                            :compressed-key compressedKey)))]))
       (into {})))

(defn normalize-multiaccount-data-keys
  [{:keys [publicKey keyUid derived compressedKey] :as data}]
  (cond-> (-> data
              (dissoc :keyUid :publicKey)
              (assoc :key-uid        keyUid
                     :compressed-key compressedKey
                     :public-key     publicKey))
    derived
    (update :derived normalize-derived-data-keys)))

(re-frame/reg-fx
 :multiaccount-generate-and-derive-addresses
 (fn []
   (native-module/multiaccount-generate-and-derive-addresses
    5
    12
    [constants/path-whisper
     constants/path-wallet-root
     constants/path-default-wallet]
    #(re-frame/dispatch [:multiaccount-generate-and-derive-addresses-success
                         (mapv normalize-multiaccount-data-keys
                               (types/json->clj %))]))))

(rf/defn multiaccount-generate-and-derive-addresses-success
  {:events [:multiaccount-generate-and-derive-addresses-success]}
  [{:keys [db]} result]
  {:db          (update db
                        :intro-wizard
                        (fn [data]
                          (-> data
                              (dissoc :processing?)
                              (assoc :multiaccounts         result
                                     :selected-storage-type :default
                                     :selected-id           (-> result first :id)
                                     :step                  :choose-key))))
   :navigate-to [:choose-name (:theme db)]})

(rf/defn generate-and-derive-addresses
  {:events [:generate-and-derive-addresses]}
  [{:keys [db]}]
  {:db                                         (-> db
                                                   (update :intro-wizard
                                                           #(-> %
                                                                (assoc :processing? true)
                                                                (dissoc :recovering?)))
                                                   (dissoc :recovered-account?))
   :multiaccount-generate-and-derive-addresses nil})

(rf/defn save-multiaccount-and-login-with-keycard
  [_ args]
  {:keycard/save-multiaccount-and-login args})

(ns status-im.data-store.realm.schemas.base.migrations
  (:require [taoensso.timbre :as log]
            [cognitect.transit :as transit]
            [clojure.set :as set]
            [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.utils.random :as random]))

(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn serialize [o] (transit/write writer o))
(defn deserialize [o] (try (transit/read reader o) (catch :default e nil)))

(defn v1 [old-realm new-realm]
  (log/debug "migrating base database v1: " old-realm new-realm))

(defn v2 [old-realm new-realm]
  (log/debug "migrating base database v2: " old-realm new-realm))

(defn v3 [old-realm new-realm]
  (log/debug "migrating base database v3: " old-realm new-realm))

(defn v4 [old-realm new-realm]
  (log/debug "migrating base database v4: " old-realm new-realm))

(def removed-tokens-v5
  #{:ATMChain :Centra :ROL})

(def removed-fiat-currencies
  #{:bmd :bzd :gmd :gyd :kyd :lak :lrd :ltl :mkd :mnt :nio :sos :srd :yer})

(defn v5 [old-realm new-realm]
  (log/debug "migrating accounts schema v4")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)
            old-settings (deserialize (aget account "settings"))
            new-settings (-> old-settings
                             (update-in [:wallet :visible-tokens :mainnet]
                                        #(set/difference % removed-tokens-v5))
                             (update-in [:wallet :currency]
                                        #(if (removed-fiat-currencies %) :usd %)))
            updated      (serialize new-settings)]
        (aset account "settings" updated)))))

(defn v6 [old-realm new-realm]
  (log/debug "migrating base database v6: " old-realm new-realm))

(defn v7 [old-realm new-realm]
  (log/debug "migrating base database v7: " old-realm new-realm))

(def removed-tokens-v8
  #{:ATT})

(defn v8 [old-realm new-realm]
  (log/debug "migrating accounts schema v8")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)
            old-settings (deserialize (aget account "settings"))
            new-settings (-> old-settings
                             (update-in [:wallet :visible-tokens :testnet]
                                        #(set/difference % removed-tokens-v8)))
            updated      (serialize new-settings)]
        (aset account "settings" updated)))))

(defn v9 [old-realm new-realm]
  (log/debug "migrating accounts schema v9")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)
            old-settings (deserialize (aget account "settings"))
            new-settings (-> old-settings
                             (dissoc :wnode))
            updated      (serialize new-settings)]
        (aset account "settings" updated)))))

(defn v10 [old-realm new-realm]
  (log/debug "migrating base database v10: " old-realm new-realm))

(defn v11 [old-realm new-realm]
  (log/debug "migrating accounts schema v11")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account             (aget accounts i)
            old-installation-id (aget account "installation-id")
            installation-id     (random/guid)]
        (when (string/blank? old-installation-id)
          (aset account "installation-id" installation-id))))))

(defn v12 [old-realm new-realm]
  (log/debug "migrating base database v12: " old-realm new-realm))

(defn v13 [old-realm new-realm]
  (log/debug "migrating base database v13: " old-realm new-realm))

(defn v14
  "Rename wnode to mailserver in account settings"
  [old-realm new-realm]
  (log/debug "migrating accounts schema v14")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)
            {:keys [wnode] :as old-settings} (deserialize (aget account "settings"))
            new-settings (when wnode
                           (-> old-settings
                               (dissoc :wnode)
                               (assoc :mailserver wnode)))
            updated      (serialize new-settings)]
        (aset account "settings" updated)))))

(defn v15 [old-realm new-realm]
  (log/debug "migrating base database v15: " old-realm new-realm))

(defn v16 [old-realm new-realm]
  (log/debug "migrating base database v16: " old-realm new-realm))

(defn v17 [old-realm new-realm]
  (log/debug "migrating accounts schema v17")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account      (aget accounts i)
            old-settings (deserialize (aget account "settings"))
            new-settings (update-in old-settings [:wallet :visible-tokens :mainnet]
                                    #(cond-> %
                                       true (disj :BQX)
                                       (contains? % :BQX) (conj :ETHOS)))
            updated      (serialize new-settings)]
        (aset account "settings" updated)))))

;; transform inactive legacy network values into the current ones
(defn transition-rpc-url [rpc-url]
  (case rpc-url
    "https://mainnet.infura.io/z6GCTmjdP3FETEJmMBI4"
    (get-in constants/mainnet-networks ["mainnet_rpc" :config :UpstreamConfig :URL])
    "https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4"
    (get-in constants/testnet-networks ["testnet_rpc" :config :UpstreamConfig :URL])
    "https://rinkeby.infura.io/z6GCTmjdP3FETEJmMBI4"
    (get-in constants/testnet-networks ["rinkeby_rpc" :config :UpstreamConfig :URL])
    rpc-url))

(defn- update-infura-project-id! [network-js]
  (let [old-config (js->clj
                    (.parse js/JSON
                            (aget network-js "config")))]
    ;; we only transition rpc networks
    (when (get-in old-config ["UpstreamConfig" "Enabled"])
      (let [new-config (update-in
                        old-config
                        ["UpstreamConfig" "URL"]
                        transition-rpc-url)]
        (aset network-js
              "config"
              (.stringify js/JSON (clj->js new-config)))))))

(defn- update-infura-project-ids! [networks-js]
  (dotimes [i (.-length networks-js)]
    (let [network-js (aget networks-js i)]
      (update-infura-project-id! network-js))))

(defn- migrate-infura-project-ids! [realm-js]
  (let [accounts (.objects realm-js "account")]
    (dotimes [i (.-length accounts)]
      (let [account  (aget accounts i)
            networks (aget account "networks")]
        (update-infura-project-ids! networks)
        (aset account "networks" networks)))))

(defn v18 [old-realm new-realm]
  (log/debug "migrating accounts database v18: " old-realm new-realm)
  (migrate-infura-project-ids! new-realm))

;; used to be v18 migration
(defn v19 [old-realm new-realm]
  (log/debug "migrating base database v19: " old-realm new-realm))

(defn v20 [old-realm new-realm]
  (log/debug "migrating accounts database v20: " old-realm new-realm)
  ;; Why is this function called twice?
  ;; We had to release a hotfix that didn't have v18 migration while we had nightlies
  ;; that already have it. so this `migrate-infura-project-ids!` went as
  ;; migration no. 18 in the release.
  ;;
  ;; Hence, we ended up with 2 branches where migration v18 meant different things:
  ;; - release 0.9.32-infura-hotfix: v18 means migration of Infura project IDs
  ;; - nightlies: v18 means empty migration but schema change
  ;;
  ;; To be able to migrate from both of these, we
  ;; (1) swapped migration v18 and v19 in the nigtlies (`develop` branch).
  ;; (2) create a migration v20 that applies Infura migration again.
  ;;
  ;; That way we can upgrade both of the paths:
  ;; - 0.9.32 that had v18 with Infura transition will apply both v19 and v20, where
  ;;    v20 will be a no-op.
  ;; - nightlies that had v18 as an empty transition will apply the empty transition
  ;;    v19 again, and migrate Infura IDs as v20.
  (migrate-infura-project-ids! new-realm))

(defn v21 [old-realm new-realm]
  (log/debug "migrating base database v21: " old-realm new-realm))

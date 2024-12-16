(ns react-native.wallet-connect
  (:require
    ["@reown/walletkit$default" :as WalletKit]
    ["@walletconnect/core" :as wc-core]
    ["@walletconnect/utils" :as wc-utils]
    [cljs-bean.core :as bean]
    [oops.core :as oops]))

(defn- wallet-connect-core
  [project-id]
  (new ^js wc-core/Core (clj->js {:projectId project-id})))

(defn init
  [project-id metadata]
  (let [core (wallet-connect-core project-id)]
    (oops/ocall WalletKit
                "init"
                (bean/->js {:core     core
                            :metadata metadata}))))

(defn build-approved-namespaces
  [proposal supported-namespaces]
  (oops/ocall wc-utils
              "buildApprovedNamespaces"
              (bean/->js {:proposal            proposal
                          :supportedNamespaces (clj->js supported-namespaces)})))

;; Get an error from this list:
;; https://github.com/WalletConnect/walletconnect-monorepo/blob/c6e9529418a0c81d4efcc6ac4e61f242a50b56c5/packages/utils/src/errors.ts
(defn get-sdk-error
  [error-key]
  (oops/ocall wc-utils "getSdkError" error-key))

(defn parse-uri
  [uri]
  (-> (oops/ocall wc-utils "parseUri" uri)
      (bean/->clj)))

(defn respond-session-request
  [{:keys [web3-wallet topic id result error]}]
  (oops/ocall web3-wallet
              "respondSessionRequest"
              (bean/->js {:topic topic
                          :response
                          (merge {:id      id
                                  :jsonrpc "2.0"}
                                 (when result
                                   {:result result})
                                 (when error
                                   {:error error}))})))

(defn reject-session
  [{:keys [web3-wallet id reason]}]
  (oops/ocall web3-wallet
              "rejectSession"
              (bean/->js {:id     id
                          :reason reason})))

(defn approve-session
  [{:keys [web3-wallet id approved-namespaces]}]
  (oops/ocall web3-wallet
              "approveSession"
              (bean/->js {:id         id
                          :namespaces approved-namespaces})))

(defn disconnect-session
  [{:keys [web3-wallet reason topic]}]
  (oops/ocall web3-wallet
              "disconnectSession"
              (bean/->js {:topic  topic
                          :reason reason})))

(defn get-active-sessions
  [web3-wallet]
  (oops/ocall web3-wallet "getActiveSessions"))

(defn core-pairing-pair
  [web3-wallet url]
  (oops/ocall web3-wallet
              "core.pairing.pair"
              (bean/->js {:uri url})))

(defn get-pairings
  [web3-wallet]
  (oops/ocall web3-wallet "core.pairing.getPairings"))

(defn register-handler
  [{:keys [web3-wallet event handler]}]
  (oops/ocall web3-wallet
              "on"
              event
              #(-> (bean/->clj %)
                   handler)))

(ns status-im.protocol.web3.inbox
  (:require [status-im.native-module.core :as status]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.protocol.web3.keys :as keys]))

(def peers (atom #{}))
(def trusted-peers (atom #{}))

;; NOTE(dmitryn) Expects JSON response like:
;; {"error": "msg"} or {"result": true}
(defn- parse-json [s]
  (try
    (-> s
        js/JSON.parse
        (js->clj :keywordize-keys true))
    (catch :default e
      {:error (.-message e)})))

(defn- response-handler [error-fn success-fn]
  (fn [response]
    (let [{:keys [error result]} (parse-json response)]
      ;; NOTE(dmitryn): AddPeer() may return {"error": ""}
      ;; assuming empty error is a success response
      (if (seq error)
        (error-fn error)
        (success-fn result)))))

(defn add-peer [enode success-fn error-fn]
  (if (@peers enode)
    (success-fn true)
    (status/add-peer enode
                     (response-handler error-fn (fn [result]
                                                  (swap! peers conj enode)
                                                  (success-fn result))))))

;; TODO(oskarth): Use web3 binding to do (.markTrustedPeer web3 enode cb)
(defn mark-trusted-peer [enode success-fn error-fn]
  (if (@trusted-peers enode)
    (success-fn true)
    (let [args {:jsonrpc "2.0"
                :id      1
                :method  "shh_markTrustedPeer"
                :params  [enode]}
          payload (.stringify js/JSON (clj->js args))]
      (status/call-web3 payload
                        (response-handler error-fn (fn [result]
                                                     (swap! trusted-peers conj enode)
                                                     (success-fn result)))))))

;; TODO(oskarth): Use web3 binding instead of raw RPC above, pending binding and deps:
;; (.requestMessages (utils/shh web3)
;;                   (clj->js opts)
;;                   callback
;;                   #(log/warn :request-messages-error
;;                             (.stringify js/JSON (clj->js opts)) %))
(defn request-messages [wnode topic sym-key-id success-fn error-fn]
  (log/info "offline inbox: sym-key-id" sym-key-id)
  (let [args {:jsonrpc "2.0"
              :id      2
              :method  "shh_requestMessages"
              ;; NOTE: "from" and "to" parameters omitted here
              ;; by default "from" is 24 hours ago and "to" is time now
              :params  [{:mailServerPeer wnode
                         :topic          topic
                         :symKeyID       sym-key-id}]}
        payload (.stringify js/JSON (clj->js args))]
    (log/info "offline inbox: request-messages request")
    (log/info "offline inbox: request-messages args" (pr-str args))
    (log/info "offline inbox: request-messages payload" (pr-str payload))
    (status/call-web3 payload
                      (response-handler error-fn success-fn))))

(defn initialize! [web3]
  (re-frame/dispatch [:initialize-offline-inbox web3]))

(ns status-im.protocol.web3.inbox
  (:require [status-im.native-module.core :as status]
            [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.protocol.web3.keys :as keys]
            [status-im.protocol.web3.utils :as utils]))

(def peers (atom #{}))
(def trusted-peers (atom #{}))

;; NOTE(dmitryn) Expects JSON response like:
;; {"error": "msg"} or {"result": true}
(defn- parse-json [s]
  (try
    (let [res (-> s
                  js/JSON.parse
                  (js->clj :keywordize-keys true))]
      ;; NOTE(dmitryn): AddPeer() may return {"error": ""}
      ;; assuming empty error is a success response
      ;; by transforming {"error": ""} to {:result true}
      (if (and (:error res)
               (= (:error res) ""))
        {:result true}
        res))
    (catch :default e
      {:error (.-message e)})))

(defn- response-handler [error-fn success-fn]
  (fn handle-response
    ([response]
     (let [{:keys [error result]} (parse-json response)]
       (handle-response error result)))
    ([error result]
     (if error
       (error-fn error)
       (success-fn result)))))

(defn add-peer [enode success-fn error-fn]
  (if (@peers enode)
    (success-fn true)
    (status/add-peer enode
                     (response-handler error-fn (fn [result]
                                                  (swap! peers conj enode)
                                                  (success-fn result))))))

(defn mark-trusted-peer [web3 enode success-fn error-fn]
  (if (@trusted-peers enode)
    (success-fn true)
    (.markTrustedPeer (utils/shh web3)
                      enode
                      (response-handler error-fn (fn [result]
                                                   (swap! trusted-peers conj enode)
                                                   (success-fn result))))))

(defn request-messages [web3 wnode topic sym-key-id success-fn error-fn]
  (log/info "offline inbox: sym-key-id" sym-key-id)
  (let [opts {:mailServerPeer wnode
              :topic          topic
              :symKeyID       sym-key-id}]
    (log/info "offline inbox: request-messages request")
    (log/info "offline inbox: request-messages args" (pr-str opts))
    (.requestMessages (utils/shh web3)
                      (clj->js opts)
                      (response-handler error-fn success-fn))))

(defn initialize! [web3]
  (re-frame/dispatch [:initialize-offline-inbox web3]))

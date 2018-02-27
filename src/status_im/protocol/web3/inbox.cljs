(ns status-im.protocol.web3.inbox
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.protocol.web3.utils :as web3.utils]
            [taoensso.timbre :as log]))

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

(defn registered-peer? [peers enode]
  (let [peer-ids (set (map :id peers))
        enode-id (web3.utils/extract-enode-id enode)]
    (contains? peer-ids enode-id)))

(defn mark-trusted-peer [web3 enode peers success-fn error-fn]
  (if (@trusted-peers enode)
    (success-fn true)
    (.markTrustedPeer (web3.utils/shh web3)
                       enode
                       (response-handler error-fn (fn [result]
                                                    (swap! trusted-peers conj enode)
                                                    (success-fn result))))))

;; TODO(dmitryn): use web3 instead of rpc call
(defn fetch-peers [success-fn error-fn]
  (let [args {:jsonrpc "2.0"
              :id 2
              :method "admin_peers"
              :params []}
        payload (.stringify js/JSON (clj->js args))]
    (status/call-web3 payload
                      (response-handler error-fn success-fn))))

(defn request-messages
  [{:keys [web3 wnode topic sym-key-id on-success on-error from to]}]
  (log/info "offline inbox: sym-key-id" sym-key-id)
  (let [opts (cond-> {:mailServerPeer wnode
                      :topic          topic
                      :symKeyID       sym-key-id}

                     from
                     (assoc :from from)

                     to
                     (assoc :to to))]
    (log/info "offline inbox: request-messages request")
    (log/info "offline inbox: request-messages args" (pr-str opts))
    (.requestMessages (web3.utils/shh web3)
                      (clj->js opts)
                      (response-handler on-error on-success))))

(defn initialize! [web3]
  (re-frame/dispatch [:initialize-offline-inbox web3]))

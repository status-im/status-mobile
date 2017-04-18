(ns status-im.components.status
  (:require-macros
    [cljs.core.async.macros :refer [go-loop go]])
  (:require [status-im.components.react :as r]
            [status-im.utils.types :as t]
            [re-frame.core :refer [dispatch]]
            [taoensso.timbre :as log]
            [cljs.core.async :refer [<! timeout]]
            [status-im.utils.js-resources :as js-res]
            [status-im.i18n :as i]
            [status-im.utils.platform :as p]))

;; if StatusModule is not initialized better to store
;; calls and make them only when StatusModule is ready
;; this flag helps to handle this
(defonce module-initialized? (atom (or p/ios? js/goog.DEBUG)))

;; array of calls to StatusModule
(defonce calls (atom []))

(defn module-initialized! []
  (reset! module-initialized? true))

(defn store-call [args]
  (log/debug :store-call args)
  (swap! calls conj args))

(defn call-module [f]
  ;(log/debug :call-module f)
  (if @module-initialized?
    (f)
    (store-call f)))

(defonce loop-started (atom false))

(when-not @loop-started
  (go-loop [_ nil]
    (reset! loop-started true)
    (if (and (seq @calls) @module-initialized?)
      (do (swap! calls (fn [calls]
                         (doseq [call calls]
                           (call))))
          (reset! loop-started false))
      (recur (<! (timeout 500))))))

(def status
  (when (exists? (.-NativeModules r/react-native))
    (.-Status (.-NativeModules r/react-native))))

(defn init-jail []
  (let [init-js (str js-res/status-js "I18n.locale = '" i/i18n.locale "';")]
    (.initJail status init-js #(log/debug "jail initialized"))))

(defonce listener-initialized (atom false))

(when-not @listener-initialized
  (reset! listener-initialized true)
  (.addListener r/device-event-emitter "gethEvent"
                #(dispatch [:signal-event (.-jsonEvent %)])))

(defn start-node [on-result]
  (when status
    (call-module #(.startNode status on-result))))

(defn stop-rpc-server []
  (when status
    (call-module #(.stopNodeRPCServer status))))

(defn start-rpc-server []
  (when status
    (call-module #(.startNodeRPCServer status))))

(defonce restarting-rpc (atom false))

(defn restart-rpc []
  (when-not @restarting-rpc
    (reset! restarting-rpc true)
    (log/debug :restart-rpc-on-post-error)

    ;; todo maybe it would be better to use something like
    ;; restart-rpc-server on status-go side
    (stop-rpc-server)
    (start-rpc-server)

    (go (<! (timeout 3000))
        (reset! restarting-rpc false))))

(defonce account-creation? (atom false))

(defn create-account [password on-result]
  (when status
    (let [callback (fn [data]
                     (reset! account-creation? false)
                     (on-result data))]
      (swap! account-creation?
             (fn [creation?]
               (if-not creation?
                 (do
                   (call-module #(.createAccount status password callback))
                   true)
                 false))))))

(defn recover-account [passphrase password on-result]
  (when status
    (call-module #(.recoverAccount status passphrase password on-result))))

(defn login [address password on-result]
  (when status
    (call-module #(.login status address password on-result))))

(defn complete-transaction
  [hash password callback]
  (log/debug :complete-transaction (boolean status) hash password)
  (when status
    (call-module #(.completeTransaction status (str hash) password callback))))

(defn discard-transaction
  [id]
  (log/debug :discard-transaction id)
  (when status
    (call-module #(.discardTransaction status id))))

(defn parse-jail [chat-id file callback]
  (when status
    (call-module #(.parseJail status chat-id file callback))))

(defn cljs->json [data]
  (.stringify js/JSON (clj->js data)))

(defn call-jail [chat-id path params callback]
  (when status
    (call-module
      #(do
         (log/debug :call-jail :chat-id chat-id)
         (log/debug :call-jail :path path)
         (log/debug :call-jail :params params)
         (let [params' (update params :context assoc
                               :debug js/goog.DEBUG
                               :locale i/i18n.locale)
               cb      (fn [r]
                         (let [{:keys [result] :as r'} (t/json->clj r)
                               {:keys [messages]} result]
                           (log/debug r')
                           (doseq [{:keys [type message]} messages]
                             (log/debug (str "VM console(" type ") - " message)))
                           (callback r')))]
           (.callJail status chat-id (cljs->json path) (cljs->json params') cb))))))


(defn set-soft-input-mode [mode]
  (when status
    (call-module #(.setSoftInputMode status mode))))

(defn clear-web-data []
  (when status
    (call-module #(.clearCookies status))
    (call-module #(.clearStorageAPIs status))))

(def adjust-resize 16)
(def adjust-pan 32)

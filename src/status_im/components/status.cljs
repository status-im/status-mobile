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

(defn cljs->json [data]
  (.stringify js/JSON (clj->js data)))

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

(when status
  (.startAPI status))

(defn init-jail []
  (let [init-js (str js-res/status-js "I18n.locale = '" i/i18n.locale "';")]
    (.initJail status init-js #(log/debug "jail initialized"))))

(defonce listener-initialized (atom false))

(when-not @listener-initialized
  (reset! listener-initialized true)
  (.addListener r/device-event-emitter "gethEvent"
                #(dispatch [:signal-event (.-jsonEvent %)])))

(defn should-move-to-internal-storage? [on-result]
  (when status
    (call-module #(.shouldMoveToInternalStorage status on-result))))

(defn move-to-internal-storage [on-result]
  (when status
    (call-module #(.moveToInternalStorage status on-result))))

(defonce starting-in-process? (atom false))
(defonce stopping-in-process? (atom false))
(defonce postponed-stop-request (atom nil))
(defonce postponed-start-request (atom nil))

(declare after-stop! after-start!)

(defn start-node [config]
  (log/debug :START-NODE)
  (cond
    @stopping-in-process? (reset! postponed-start-request
                                  {:config config})
    @starting-in-process? (reset! postponed-stop-request false)
    :else (when status
            (reset! postponed-stop-request false)
            (reset! starting-in-process? true)
            (call-module
              #(.startNode
                 status
                 (or config "{}")
                 (fn [result]
                   (log/debug :start-node-res result)
                   (let [json (.parse js/JSON result)
                         {:keys [error]} (js->clj json :keywordize-keys true)]
                     (when-not (= "" error)
                       (after-start!)))))))))

(defn stop-node []
  (log/debug :STOP-NODE)
  (cond
    @starting-in-process? (reset! postponed-stop-request true)
    @stopping-in-process? (reset! postponed-start-request nil)
    :else (when status
            (reset! postponed-start-request nil)
            (reset! stopping-in-process? true)
            (call-module
              #(.stopNode
                 status
                 (fn [result]
                   (log/debug :stop-node-res result)
                   (let [json (.parse js/JSON result)
                         {:keys [error]} (js->clj json :keywordize-keys true)]
                     (when-not (= "" error)
                       (after-stop!)))))))))

(defn after-start! []
  (log/debug :AFTER-START-NODE)
  (reset! starting-in-process? false)
  (when @postponed-stop-request
    (stop-node)))

(defn after-stop! []
  (log/debug :AFTER-STOP-NODE)
  (reset! stopping-in-process? false)
  (when-let [{:keys [config]} @postponed-start-request]
    (start-node config)))

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

(defn wrap-callback-with-stopwatch
  [message callback]
  (let [start (.now js/Date)]
    (fn [& args]
      (let [stop (.now js/Date)]
        (log/debug :stopwatch message (str (- stop start) "ms")))
      (apply callback args))))

(defn login [address password config restart? on-result]
  (when status
    (let [callback (wrap-callback-with-stopwatch :login on-result)]
      (call-module #(.login status address password callback)))))

(defn verify-account [address password callback]
  (let [callback' (wrap-callback-with-stopwatch :verify-account callback)]
    (when status
      (call-module (.verifyAccountPassword status address password callback')))))

(defn complete-transactions
  [hashes password callback]
  (log/debug :complete-transactions (boolean status) hashes password)
  (when status
    (call-module #(.completeTransactions status (cljs->json hashes) password callback))))

(defn discard-transaction
  [id]
  (log/debug :discard-transaction id)
  (when status
    (call-module #(.discardTransaction status id))))

(defn parse-jail [chat-id file callback]
  (when status
    (call-module #(.parseJail status chat-id file callback))))

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

(defn call-function!
  [{:keys [chat-id function callback] :as opts}]
  (let [path   [:functions function]
        params (select-keys opts [:parameters :context])]
    (call-jail
      chat-id
      path
      params
      (or callback #(dispatch [:received-bot-response {:chat-id chat-id} %])))))

(defn set-soft-input-mode [mode]
  (when status
    (call-module #(.setSoftInputMode status mode))))

(defn clear-web-data []
  (when status
    (call-module #(.clearCookies status))
    (call-module #(.clearStorageAPIs status))))

(def adjust-resize 16)
(def adjust-pan 32)

(defn call-web3 [host payload callback]
  (when status
    (call-module #(.sendWeb3Request status host payload callback))))

(ns status-im.components.status
  (:require-macros
    [cljs.core.async.macros :refer [go-loop go]])
  (:require [status-im.components.react :as r]
            [status-im.utils.types :as t]
            [re-frame.core :refer [dispatch]]
            [taoensso.timbre :as log]
            [cljs.core.async :refer [<! timeout]]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.platform :as p]
            [status-im.utils.scheduler :as scheduler]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

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
  (when (exists? (.-NativeModules rn-dependencies/react-native))
    (.-Status (.-NativeModules rn-dependencies/react-native))))

(defn init-jail []
  (let [init-js (str js-res/status-js "I18n.locale = '" rn-dependencies/i18n.locale "';")]
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

(defn complete-transactions
  [hashes password callback]
  (log/debug :complete-transactions (boolean status) hashes)
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

(defn execute-call [{:keys [jail-id path params callback]}]
  (when status
    (call-module
      #(do
         (log/debug :call-jail :jail-id jail-id)
         (log/debug :call-jail :path path)
         ;; this debug message can contain sensetive info
         #_(log/debug :call-jail :params params)
         (let [params' (update params :context assoc
                               :debug js/goog.DEBUG
                               :locale rn-dependencies/i18n.locale)
               cb      (fn [r]
                         (let [{:keys [result] :as r'} (t/json->clj r)
                               {:keys [messages]} result]
                           (log/debug r')
                           (doseq [{:keys [type message]} messages]
                             (log/debug (str "VM console(" type ") - " message)))
                           (callback r')))]
           (.callJail status jail-id (t/clj->json path) (t/clj->json params') cb))))))

;; TODO(rasom): temporal solution, should be fixed on status-go side
(def check-raw-calls-interval 400)
(def interval-between-calls 100)
;; contains all calls to jail before with duplicates
(def raw-jail-calls (atom '()))
;; contains only calls that passed duplication check
(def jail-calls (atom '()))

(defn remove-duplicate-calls
  "Removes duplicates by [jail path] keys, remains the last one."
  [[all-keys calls] {:keys [jail-id path] :as call}]
  (if (contains? all-keys [jail-id path])
    [all-keys calls]
    [(conj all-keys [jail-id path])
     (conj calls call)]))

(defn check-raw-calls-loop!
  "Only the last call with [jail path] key is added to jail-calls list
  from raw-jail-calls"
  []
  (go-loop [_ nil]
    (let [[_ new-calls] (reduce remove-duplicate-calls [#{} '()] @raw-jail-calls)]
      (reset! raw-jail-calls '())
      (swap! jail-calls (fn [old-calls]
                          (concat new-calls old-calls))))
    (recur (<! (timeout check-raw-calls-interval)))))

(defn execute-calls-loop!
  "Calls to jail are executed ne by one with interval-between-calls,
  which reduces chances of response shuffling"
  []
  (go-loop [_ nil]
    (let [next-call (last @jail-calls)]
      (swap! jail-calls butlast)
      (when next-call
        (execute-call next-call)))
    (recur (<! (timeout interval-between-calls)))))

(check-raw-calls-loop!)
(execute-calls-loop!)

(defn call-jail [call]
  (swap! raw-jail-calls conj call))
;; TODO(rasom): end of sick magic, should be removed ^

(defn call-function!
  [{:keys [chat-id function callback] :as opts}]
  (let [path   [:functions function]
        params (select-keys opts [:parameters :context])]
    (call-jail
      {:jail-id  chat-id
       :path     path
       :params   params
       :callback (or callback #(dispatch [:received-bot-response {:chat-id chat-id} %]))})))

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

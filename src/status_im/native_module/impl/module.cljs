(ns status-im.native-module.impl.module
  (:require-macros
   [cljs.core.async.macros :as async :refer [go-loop go]])
  (:require [status-im.components.react :as r]
            [re-frame.core :refer [dispatch]]
            [taoensso.timbre :as log]
            [cljs.core.async :as async :refer [<! timeout]]
            [status-im.utils.js-resources :as js-res]
            [status-im.utils.platform :as p]
            [status-im.utils.scheduler :as scheduler]
            [status-im.utils.types :as types]
            [status-im.utils.transducers :as transducers]
            [status-im.utils.async :as async-util]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.native-module.module :as module]))

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
  ;;(log/debug :call-module f)
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
  (when status
    (call-module
     (fn []
       (let [init-js (str js-res/status-js "I18n.locale = '" rn-dependencies/i18n.locale "';")]
         (.initJail status init-js #(log/debug "jail initialized")))))))

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

(defn stop-node []
  (when status
    (call-module #(.stopNode status))))

(defn start-node [config]
  (when status
    (call-module #(.startNode status config))))

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

(defn notify [token on-result]
  (when status
    (call-module #(.notify status token on-result))))

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
    (call-module #(.completeTransactions status (types/clj->json hashes) password callback))))

(defn discard-transaction
  [id]
  (log/debug :discard-transaction id)
  (when status
    (call-module #(.discardTransaction status id))))

(defn parse-jail [bot-id file callback]
  (when status
    (call-module #(.parseJail status bot-id file callback))))

(defn execute-call [{:keys [jail-id path params callback]}]
  (when status
    (call-module
     #(do
        (log/debug :call-jail :jail-id jail-id)
        (log/debug :call-jail :path path)
        ;; this debug message can contain sensitive info
        #_(log/debug :call-jail :params params)
        (let [params' (update params :context assoc
                              :debug js/goog.DEBUG
                              :locale rn-dependencies/i18n.locale)
              cb      (fn [jail-result]
                        (let [result (-> jail-result
                                         types/json->clj
                                         (assoc :bot-id jail-id))] 
                          (callback result)))]
          (.callJail status jail-id (types/clj->json path) (types/clj->json params') cb))))))

;; We want the mainting (time) windowed queue of all calls to the jail
;; in order to de-duplicate certain type of calls made in rapid succession
;; where it's beneficial to only execute the last call of that type.
;;
;; The reason why is to improve performance and user feedback, for example
;; when making command argument suggestion lookups, everytime the command
;; input changes (so the user types/deletes a character), we need to fetch
;; new suggestions.
;; However the process of asynchronously fetching and displaying them
;; is unfortunately not instant, so without de-duplication, given that user
;; typed N characters in rapid succession, N percievable suggestion updates
;; will be performed after user already stopped typing, which gives
;; impression of slow, unresponsive UI.
;;
;; With de-duplication in some timeframe (set to 400ms currently), only
;; the last suggestion call for given jail-id jail-path combination is
;; made, and the UI feedback is much better + we save some unnecessary
;; calls to jail.

(def ^:private queue-flush-time 400)

(def ^:private call-queue (async/chan))
(def ^:private deduplicated-calls (async/chan))

(async-util/chunked-pipe! call-queue deduplicated-calls queue-flush-time)

(defn compare-calls
  "Used as comparator deciding which calls should be de-duplicated.
  Whenever we fetch suggestions, we only want to issue the last call
  done in the `queue-flush-time` window, for all other calls, we have
  de-duplicate based on call identity"
  [{:keys [jail-id path] :as call}]
  (if (= :suggestions (last path))
    [jail-id path]
    call))

(go-loop []
  (doseq [call (sequence (transducers/last-distinct-by compare-calls) (<! deduplicated-calls))]
    (execute-call call))
  (recur))

(defn call-jail [call]
  (async/put! call-queue call))

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

(defn call-web3 [host payload callback]
  (when status
    (call-module #(.sendWeb3Request status host payload callback))))

(defn close-application []
  (.closeApplication status))

(defrecord ReactNativeStatus []
  module/IReactNativeStatus
  ;; status-go calls
  (-init-jail [this]
    (init-jail))
  (-start-node [this config]
    (start-node config))
  (-stop-node [this]
    (stop-node))
  (-create-account [this password callback]
    (create-account password callback))
  (-recover-account [this passphrase password callback]
    (recover-account passphrase password callback))
  (-login [this address password callback]
    (login address password callback))
  (-complete-transactions [this hashes password callback]
    (complete-transactions hashes password callback))
  (-discard-transaction [this id]
    (discard-transaction id))
  (-parse-jail [this chat-id file callback]
    (parse-jail chat-id file callback))
  (-call-jail [this params]
    (call-jail params))
  (-call-function! [this params]
    (call-function! params))
  (-call-web3 [this host payload callback]
    (call-web3 host payload callback))
  (-notify [this token callback]
    (notify token callback))

  ;; other calls
  (-move-to-internal-storage [this callback]
    (move-to-internal-storage callback))
  (-set-soft-input-mode [this mode]
    (set-soft-input-mode mode))
  (-clear-web-data [this]
    (clear-web-data))
  (-module-initialized! [this]
    (module-initialized!))
  (-should-move-to-internal-storage? [this callback]
    (should-move-to-internal-storage? callback))
  (-close-application [this]
    (close-application)))

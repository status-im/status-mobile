(ns status-im.debug.handlers
  (:require [re-frame.core :refer [after dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.components.react :refer [http-bridge]]
            [status-im.data-store.accounts :as accounts]
            [taoensso.timbre :as log]))

(def debug-server-port 5561)

(register-handler :init-debug-mode
  (u/side-effect!
    (fn [_ [_ address]]
      (let [{:keys [debug?]} (accounts/get-by-address address)]
        (when debug?
          (dispatch [:debug-server-start]))))))

(register-handler :debug-server-start
  (u/side-effect!
    (fn [_]
      (.start http-bridge
              debug-server-port
              (fn [req]
                (try
                  (let [{:keys [postData url]} (js->clj req :keywordize-keys true)
                        postData (if (string? postData)
                                   (-> (.parse js/JSON postData)
                                       (js->clj :keywordize-keys true))
                                   postData)]
                    (dispatch [:debug-request {:url url :postData postData}]))
                  (catch js/Error e
                    (log/debug "Error: " e))))))))

(register-handler :debug-server-stop
  (u/side-effect!
    (fn [_]
      (.stop http-bridge))))

(register-handler :debug-request
  (u/side-effect!
    (fn [{:keys [web3]} [_ {url               :url
                            {:keys [encoded]} :postData :as d}]]
      (try
        (let [json (->> (.toAscii web3 encoded)
                        (.parse js/JSON))
              obj  (js->clj json :keywordize-keys true)]
          (case url
            "/add-dapp"     (dispatch [:debug-add-dapp obj])
            "/remove-dapp"  (dispatch [:debug-remove-dapp obj])
            "/dapp-changed" (dispatch [:debug-dapp-changed obj])
            :default))
        (catch js/Error e
          (log/debug "Error: " e))))))

(register-handler :debug-add-dapp
  (u/side-effect!
    (fn [{:keys [contacts]} [_ {:keys [name whisper-identity dapp-url] :as dapp-data}]]
      (when (and name
                 whisper-identity
                 dapp-url
                 (or (not (get contacts whisper-identity))
                     (get-in contacts [whisper-identity :debug?])))
        (let [dapp (merge dapp-data {:dapp?  true
                                     :debug? true})]
          (dispatch [:add-chat whisper-identity {:name   name
                                                 :debug? true}])
          (dispatch [:add-contacts [dapp]]))))))

(register-handler :debug-remove-dapp
  (u/side-effect!
    (fn [{:keys [chats]} [_ {:keys [whisper-identity]}]]
      (when (get-in chats [whisper-identity :debug?])
        (dispatch [:remove-chat whisper-identity]))
      (dispatch [:remove-contact whisper-identity #(and (:dapp? %) (:debug? %))]))))

(register-handler :debug-dapp-changed
  (u/side-effect!
    (fn [{:keys [webview-bridge current-chat-id chats]} [_ {:keys [whisper-identity]}]]
      (when (and (= current-chat-id whisper-identity)
                 (get-in chats [whisper-identity :debug?])
                 webview-bridge)
        (.reload webview-bridge)))))


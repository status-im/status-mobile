(ns status-im.common.shared-urls.events
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    [status-im.common.shared-urls.data-store :as data-store]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn parse-shared-url-success
  [url response]
  (let [parsed-data (data-store/<-rpc response)
        cb          #(rf/dispatch [:universal-links/match-value url %])]
    (cond
      (:channel parsed-data)
      (cb {:type         :community-chat
           :chat-id      (str (get-in parsed-data [:community :community-id])
                              (get-in parsed-data [:channel :channel-uuid]))
           :community-id (get-in parsed-data [:community :community-id])})

      (:community parsed-data)
      (cb {:type :community :community-id (get-in parsed-data [:community :community-id])}))))

(defn parse-shared-url
  [_ [uri]]
  (let [url (string/replace uri #"status-app:/" "https://status.app")]
    {:json-rpc/call
     [{:method     "wakuext_parseSharedURL"
       :params     [url]
       :on-success #(parse-shared-url-success url %)
       :on-error   (fn [err]
                     (log/error "failed to parse shared url" {:error err :url url}))}]}))

(re-frame/reg-event-fx :shared-urls/parse-shared-url parse-shared-url)

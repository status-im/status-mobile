(ns status-im.utils.universal-links.core
  (:require
   [taoensso.timbre :as log]
   [re-frame.core :as re-frame]
   [status-im.utils.config :as config]
   [status-im.chat.events :as chat.events]
   [status-im.models.account :as models.account]
   [status-im.ui.components.list-selection :as list-selection]
   [status-im.ui.components.react  :as react]
   [status-im.utils.handlers-macro :as handlers-macro]
   [cljs.spec.alpha :as spec]
   [status-im.ui.screens.navigation :as navigation]
   [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]))

;; TODO(yenda) investigate why `handle-universal-link` event is
;; dispatched 7 times for the same link

(def public-chat-regex #".*/chat/public/(.*)$")
(def profile-regex #".*/user/(.*)$")
(def browse-regex #".*/browse/(.*)$")

(defn match-url [url regex]
  (some->> url
           (re-matches regex)
           peek))

(defn universal-link? [url]
  (boolean
   (re-matches #"((^https?://get.status.im/)|(^status-im://))[\x00-\x7F]+$" url)))

(defn open! [url]
  (log/info "universal-links:  opening " url)
  (if-let [dapp-url (match-url url browse-regex)]
    (list-selection/browse-dapp dapp-url)
    ;; We need to dispatch here, we can't openURL directly
    ;; as it is opened in safari on iOS
    (re-frame/dispatch [:handle-universal-link url])))

(defn handle-browse [url cofx]
  (log/info "universal-links: handling browse " url)
  {:browse url})

(defn handle-public-chat [public-chat cofx]
  (log/info "universal-links: handling public chat " public-chat)
  (chat.events/create-new-public-chat public-chat cofx))

(defn handle-view-profile [profile-id {:keys [db] :as cofx}]
  (log/info "universal-links: handling view profile" profile-id)
  (if (new-chat.db/own-whisper-identity? db profile-id)
    (navigation/navigate-to-cofx :my-profile nil cofx)
    (chat.events/show-profile profile-id true cofx)))

(defn handle-not-found [full-url]
  (log/info "universal-links: no handler for " full-url))

(defn dispatch-url
  "Dispatch url so we can get access to re-frame/db"
  [url]
  (if-not (nil? url)
    (re-frame/dispatch [:handle-universal-link url])
    (log/debug "universal-links: no url")))

(defn route-url
  "Match a url against a list of routes and handle accordingly"
  [url cofx]
  (cond
    (match-url url public-chat-regex)
    (handle-public-chat (match-url url public-chat-regex) cofx)

    (spec/valid? :global/public-key (match-url url profile-regex))
    (handle-view-profile (match-url url profile-regex) cofx)

    (match-url url browse-regex)
    (handle-browse url cofx)

    :else (handle-not-found url)))

(defn store-url-for-later
  "Store the url in the db to be processed on login"
  [url {:keys [db]}]
  (log/info :store-url-for-later)
  {:db (assoc db :universal-links/url url)})

(defn handle-url
  "Store url in the database if the user is not logged in, to be processed
  on login, otherwise just handle it"
  [url cofx]
  (if (models.account/logged-in? cofx)
    (route-url url cofx)
    (store-url-for-later url cofx)))

(defn process-stored-event
  "Return an event description for processing a url if in the database"
  [{:keys [db] :as cofx}]
  (when-let [url (:universal-links/url db)]
    (handlers-macro/merge-fx cofx
                             {:db (dissoc db :universal-links/url)}
                             (handle-url url))))

(defn unwrap-js-url [e]
  (-> e
      (js->clj :keywordize-keys true)
      :url))

(def url-event-listener
  (comp dispatch-url unwrap-js-url))

(defn initialize
  "Add an event listener for handling background->foreground transition
  and handles incoming url if the app has been started by clicking on a link"
  []
  (log/debug "universal-links: initializing")
  (.. react/linking
      (getInitialURL)
      (then dispatch-url))
  (.. react/linking
      (addEventListener "url" url-event-listener)))

(defn finalize
  "Remove event listener for url"
  []
  (log/debug "universal-links: finalizing")
  (.. react/linking
      (removeEventListener "url" url-event-listener)))

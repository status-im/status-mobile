(ns status-im.utils.universal-links.core
  (:require
   [taoensso.timbre :as log]
   [re-frame.core :as re-frame]
   [status-im.utils.config :as config]
   [status-im.chat.events :as chat.events]
   [status-im.models.account :as models.account]
   [status-im.ui.components.react  :as react]))

(def public-chat-regex #".*/chat/public/(.*)$")
(def profile-regex #".*/user/(.*)$")

(defn handle-public-chat [public-chat cofx]
  (log/info "universal-links: handling public chat " public-chat)
  (chat.events/create-new-public-chat public-chat cofx))

(defn handle-view-profile [profile-id cofx]
  (log/info "universal links: handling view profile" profile-id)
  (chat.events/show-profile profile-id cofx))

(defn handle-not-found [full-url]
  (log/info "universal links: no handler for " full-url))

(defn match-url [url regex]
  (some->> url
           (re-matches regex)
           peek))

(defn stored-url-event
  "Return an event description for processing a url if in the database"
  [{:keys [db]}]
  (when-let [url (:universal-links/url db)]
    [:handle-universal-link url]))

(defn dispatch-url
  "Dispatch url so we can get access to re-frame/db"
  [url]
  (if-not (nil? url)
    (re-frame/dispatch [:handle-universal-link url])
    (log/debug "universal links: no url")))

(defn store-url-for-later
  "Store the url in the db to be processed on login"
  [url {:keys [db]}]
  (assoc-in {:db db} [:db :universal-links/url] url))

(defn clear-url
  "Remove a url from the db"
  [{:keys [db]}]
  (update {:db db} :db dissoc :universal-links/url))

(defn route-url
  "Match a url against a list of routes and handle accordingly"
  [url cofx]
  (cond
    (match-url url public-chat-regex)
    (handle-public-chat (match-url url public-chat-regex) cofx)

    (match-url url profile-regex)
    (handle-view-profile (match-url url profile-regex) cofx)

    :else (handle-not-found url)))

(defn handle-url
  "Store url in the database if the user is not logged in, to be processed
  on login, otherwise just handle it"
  [url cofx]
  (if (models.account/logged-in? cofx)
    (do
      (clear-url cofx)
      (route-url url cofx))
    (store-url-for-later url cofx)))

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
  (when config/universal-links-enabled?
    (log/debug "universal-links: initializing")
    (.. react/linking
        (getInitialURL)
        (then dispatch-url))
    (.. react/linking
        (addEventListener "url" url-event-listener))))

(defn finalize
  "Remove event listener for url"
  []
  (when config/universal-links-enabled?
    (log/debug "universal-links: finalizing")
    (.. react/linking
        (removeEventListener "url" url-event-listener))))

(ns status-im.utils.universal-links.core
  (:require [cljs.spec.alpha :as spec]
            [goog.string :as gstring]
            [goog.string.format]
            [re-frame.core :as re-frame]
            [status-im.accounts.db :as accounts.db]
            [status-im.chat.models :as chat]
            [status-im.extensions.registry :as extensions.registry]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.add-new.new-chat.db :as new-chat.db]
            [status-im.ui.screens.desktop.main.chat.events :as desktop.events]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as platform]))

;; TODO(yenda) investigate why `handle-universal-link` event is
;; dispatched 7 times for the same link

(def public-chat-regex #".*/chat/public/(.*)$")
(def profile-regex #".*/user/(.*)$")
(def browse-regex #".*/browse/(.*)$")
(def extension-regex #".*/extension/(.*)$")

;; domains should be without the trailing slash
(def domains {:external "https://get.status.im"
              :internal "status-im:/"})

(def links {:public-chat "%s/chat/public/%s"
            :user        "%s/user/%s"
            :browse      "%s/browse/%s"})

(defn generate-link [link-type domain-type param]
  (gstring/format (get links link-type)
                  (get domains domain-type)
                  param))

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

(fx/defn handle-browse [cofx url]
  (log/info "universal-links: handling browse" url)
  {:browser/show-browser-selection url})

(fx/defn handle-public-chat [cofx public-chat]
  (log/info "universal-links: handling public chat" public-chat)
  (chat/start-public-chat cofx public-chat {:navigation-reset? true}))

(fx/defn handle-view-profile [{:keys [db] :as cofx} public-key]
  (log/info "universal-links: handling view profile" public-key)
  (if (new-chat.db/own-public-key? db public-key)
    (navigation/navigate-to-cofx cofx :my-profile nil)
    (if platform/desktop?
      (desktop.events/show-profile-desktop public-key cofx)
      (navigation/navigate-to-cofx (assoc-in cofx [:db :contacts/identity] public-key) :profile nil))))

(fx/defn handle-extension [cofx url]
  (log/info "universal-links: handling url profile" url)
  (extensions.registry/load cofx url false))

(defn handle-not-found [full-url]
  (log/info "universal-links: no handler for " full-url))

(defn dispatch-url
  "Dispatch url so we can get access to re-frame/db"
  [url]
  (if-not (nil? url)
    (re-frame/dispatch [:handle-universal-link url])
    (log/debug "universal-links: no url")))

(fx/defn route-url
  "Match a url against a list of routes and handle accordingly"
  [cofx url]
  (cond
    (match-url url public-chat-regex)
    (handle-public-chat cofx (match-url url public-chat-regex))

    (spec/valid? :global/public-key (match-url url profile-regex))
    (handle-view-profile cofx (match-url url profile-regex))

    (match-url url browse-regex)
    (handle-browse cofx (match-url url browse-regex))

    (and config/extensions-enabled? (match-url url extension-regex))
    (handle-extension cofx url)

    :else (handle-not-found url)))

(fx/defn store-url-for-later
  "Store the url in the db to be processed on login"
  [{:keys [db]} url]
  (log/info :store-url-for-later)
  {:db (assoc db :universal-links/url url)})

(fx/defn handle-url
  "Store url in the database if the user is not logged in, to be processed
  on login, otherwise just handle it"
  [cofx url]
  (if (accounts.db/logged-in? cofx)
    (route-url cofx url)
    (store-url-for-later cofx url)))

(fx/defn process-stored-event
  "Return an event description for processing a url if in the database"
  [{:keys [db] :as cofx}]
  (when-let [url (:universal-links/url db)]
    (fx/merge cofx
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

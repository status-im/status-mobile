(ns status-im.ui.screens.wallet.collectibles.cryptokitties
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.utils.handlers :as handlers])
  (:refer-clojure :exclude [symbol]))

(def symbol :CK)

(handlers/register-handler-fx
 :load-kitty-success
 [re-frame/trim-v]
 (fn [{db :db} [[id collectible]]]
   {:db (update-in db [:collectibles symbol] assoc id collectible)}))

(handlers/register-handler-fx
 :load-kitty-failure
 [re-frame/trim-v]
 (fn [{db :db} [_]]
   {:db db}))

(defn parse-payload [o]
  (js->clj (js/JSON.parse o)
           :keywordize-keys true))

(handlers/register-handler-fx
 :load-kitties
 (fn [{db :db} [_ ids]]
   {:db db
    :http-get-n (mapv (fn [id]
                        {:url (str "https://api.cryptokitties.co/kitties/" id)
                         :success-event-creator (fn [o]
                                                  [:load-kitty-success [id (parse-payload o)]])
                         :failure-event-creator (fn [o]
                                                  [:load-kitty-failure [id (parse-payload o)]])})
                      ids)}))

(defn kitties-url [address]
  (str "https://api.cryptokitties.co/kitties?offset=0&limit=100&owner_wallet_address=" address "&parents=false"))

(handlers/register-handler-fx
 :load-kitties-success
 (fn [{db :db} [_ ids]]
   {:db db
    :dispatch [:load-kitties ids]}))

(defmethod collectibles/load-collectibles-fx symbol [_ address]
  {:http-get {:url                   (kitties-url address)
              :success-event-creator (fn [o]
                                       [:load-kitties-success (map :id (:kitties (parse-payload o)))])
              :failure-event-creator (fn [o]
                                       [:load-collectibles-failure (parse-payload o)])
              :timeout-ms            10000}})

(defn- kitty-name [{:keys [id name]}]
  (or name (str (i18n/label :t/cryptokitty-name) id)))

(def view-style
  {:padding-vertical 10})

(def text-style
  {:flex               1
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 16})

(def name-style
  {:color         colors/black
   :margin-bottom 10})

(defmethod collectibles/render-collectible symbol [_ {:keys [id bio image_url] :as m}]
  [react/view {:style view-style}
   [react/view {:style text-style}
    ;; TODO reenable image once SVG is supported
    #_[react/image {:style {:width 80 :height 80 :margin 10 :background-color "red"} :source {:uri image_url}}]
    [react/view {}
     [react/text {:style name-style}
      (kitty-name m)]
     [react/text {:number-of-lines 3
                  :ellipsize-mode :tail}
      bio]]]
   [action-button/action-button {:label               (i18n/label :t/view-cryptokitties)
                                 :icon                :icons/address
                                 :icon-opts           {:color colors/blue}
                                 :accessibility-label :open-collectible-button
                                 :on-press            #(re-frame/dispatch [:open-browser {:url (str "https://www.cryptokitties.co/kitty/" id)}])}]])

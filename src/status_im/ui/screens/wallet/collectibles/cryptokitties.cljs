(ns status-im.ui.screens.wallet.collectibles.cryptokitties
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.http :as http]
            [status-im.ui.components.svgimage :as svgimage]))

(def ck :CK)

(handlers/register-handler-fx
 :load-kitties
 (fn [{db :db} [_ ids]]
   {:db db
    :http-get-n (mapv (fn [id]
                        {:url (str "https://api.cryptokitties.co/kitties/" id)
                         :success-event-creator (fn [o]
                                                  [:load-collectible-success ck {id (http/parse-payload o)}])
                         :failure-event-creator (fn [o]
                                                  [:load-collectible-failure ck {id (http/parse-payload o)}])})
                      ids)}))

(defn kitties-url [address]
  (str "https://api.cryptokitties.co/kitties?offset=0&limit=20&owner_wallet_address=" address "&parents=false"))

(handlers/register-handler-fx
 :load-kitties-success
 (fn [{db :db} [_ ids]]
   {:db db
    :dispatch [:load-kitties ids]}))

;; TODO(julien) Each HTTP call will return up to 20 kitties. Make sure all extra kitties are fetched
(defmethod collectibles/load-collectibles-fx ck [_ _ _ address]
  {:http-get {:url                   (kitties-url address)
              :success-event-creator (fn [o]
                                       [:load-kitties-success (map :id (:kitties (http/parse-payload o)))])
              :failure-event-creator (fn [o]
                                       [:load-collectibles-failure (http/parse-payload o)])
              :timeout-ms            10000}})

(def base-url "https://www.cryptokitties.co/kitty/")

(defmethod collectibles/render-collectible ck [_ {:keys [id name bio image_url]}]
  [react/view {:style styles/details}
   [react/view {:style styles/details-text}
    [svgimage/svgimage {:style styles/details-image
                        :source {:uri image_url}}]
    [react/view {:flex 1}
     [react/text {:style styles/details-name}
      (or name (i18n/label :t/cryptokitty-name {:id id}))]
     [react/text {:number-of-lines 3
                  :ellipsize-mode :tail}
      bio]]]
   [action-button/action-button {:label               (i18n/label :t/view-cryptokitties)
                                 :icon                :icons/address
                                 :icon-opts           {:color colors/blue}
                                 :accessibility-label :open-collectible-button
                                 :on-press            #(re-frame/dispatch [:open-browser {:url (str base-url id)}])}]])

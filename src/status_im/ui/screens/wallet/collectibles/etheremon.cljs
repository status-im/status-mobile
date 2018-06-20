(ns status-im.ui.screens.wallet.collectibles.etheremon
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.collectibles.styles :as styles]
            [status-im.ui.screens.wallet.collectibles.views :as collectibles]
            [status-im.utils.http :as http]))

(def emona :EMONA)

(defmethod collectibles/load-collectible-fx emona [_ id]
  {:http-get {:url (str "https://www.etheremon.com/api/monster/get_data?monster_ids=" id)
              :success-event-creator (fn [o]
                                       [:load-collectible-success emona (:data (http/parse-payload o))])
              :failure-event-creator (fn [o]
                                       [:load-collectible-failure emona {id (http/parse-payload o)}])}})

(def base-url "https://www.etheremon.com/#/mons/")

(defmethod collectibles/render-collectible emona [_ {:keys [monster_id user_defined_name image]}]
  [react/view {:style styles/details}
   [react/view {:style styles/details-text}
    [react/image {:style  styles/details-image
                  :source {:uri image}}]
    [react/view {:justify-content :center}
     [react/text {:style styles/details-name}
      user_defined_name]]]
   [action-button/action-button {:label               (i18n/label :t/view-etheremon)
                                 :icon                :icons/address
                                 :icon-opts           {:color colors/blue}
                                 :accessibility-label :open-collectible-button
                                 :on-press            #(re-frame/dispatch [:open-browser {:url (str base-url monster_id)}])}]])

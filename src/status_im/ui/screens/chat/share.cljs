(ns status-im.ui.screens.chat.share
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.utils.handlers :refer [<sub >evt-once]]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.chat.models.message :as message]
            [status-im.multiaccounts.core :as multiaccounts]
            [clojure.string :as str]))

(defn blank-page [text]
  [rn/view {:style {:padding 16 :flex 1 :flex-direction :row :align-items :center :justify-content :center}}
   [quo/text {:align :center
              :color :secondary}
    text]])

(defn contacts-list-item [{:keys [public-key active] :as contact} _ _ {:keys [selected]}]
  (let [[first-name second-name] (multiaccounts/contact-two-names contact true)]
    [quo/list-item
     {:title     first-name
      :subtitle  second-name
      :icon      [chat-icon.screen/contact-icon-contacts-tab
                  (multiaccounts/displayed-photo contact)]
      :accessory :checkbox
      :active    active
      :on-press  (fn []
                   (if active
                     (swap! selected disj public-key)
                     (swap! selected conj public-key)))}]))

(defn share []
  (let [user-pk           (reagent/atom "")
        contacts-selected (reagent/atom #{})
        {:keys [message-id]} (<sub [:get-screen-params])]
    (fn []
      (let [contacts-data               (<sub [:contacts/active])
            selected                    @contacts-selected
            contacts                    (map (fn [{:keys [public-key] :as contact}]
                                               (assoc contact :active (contains? selected public-key)))
                                             contacts-data)]
        [:<>
         (if (empty? contacts)
           [blank-page (i18n/label :t/share-images-with-your-contacts)]
           [rn/flat-list {:style                   {:flex 1}
                          :content-container-style {:padding-vertical 16}
                          :render-data             {:selected contacts-selected}
                          :render-fn               contacts-list-item
                          :key-fn                  (fn [{:keys [active public-key]}]
                                                     (str public-key active))
                          :data                    contacts}])
         [toolbar/toolbar
          {:show-border? true
           :center
           [quo/button {:disabled (and (str/blank? @user-pk)
                                       (zero? (count selected)))
                        :type     :secondary
                        :on-press #(>evt-once [::message/share-image-to-contacts-pressed
                                               @user-pk selected message-id])}
            (i18n/label :t/share)]}]]))))

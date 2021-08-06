(ns status-im.ui.components.swipeable.views
  (:require [status-im.ui.components.swipeable.style :as styles]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.utils :as utils]
            [reagent.core :as reagent]
            [quo.core :as quo]
            [quo.gesture-handler :as gesture-handler]
            [quo.react-native :as rn]))

(defn- show-delete-confirmation [on-accept on-cancel]
  (utils/show-confirmation {:title               (i18n/label :t/delete-chat)
                            :content             (i18n/label :t/delete-chat-confirmation)
                            :confirm-button-text (i18n/label :t/delete)
                            :cancel-button-text  (i18n/label :t/cancel)
                            :on-accept           on-accept
                            :on-cancel           on-cancel}))

(defn- on-swipe-to-delete [on-delete swipeable-ref]
  (let [close (goog.object/getValueByKeys swipeable-ref #js ["close"])
        dismiss-fn #(close)
        accept-fn on-delete]
    #(show-delete-confirmation accept-fn dismiss-fn)))

(defn- delete-action
  [{:keys [on-press]}]
  (reagent/as-element [rn/touchable-opacity {:style (styles/delete-action-container)
                                             :on-press on-press}
                       [quo/text {:weight :bold
                                  :size :large
                                  :color :inverse} (i18n/label :t/delete)]]))

(defn swipe-to-delete
  [{:keys [on-delete]}]
  (let [this (reagent/current-component) children (reagent/children this) swipe-item (atom nil)]
    (fn [{:keys []}]
      (into [gesture-handler/swipeable {:ref swipe-item
                                        :right-actions #(delete-action {:on-press (on-swipe-to-delete on-delete @swipe-item)})}]
            children))))

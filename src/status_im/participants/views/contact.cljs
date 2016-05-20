(ns status-im.participants.views.contact
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view]]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [status-im.components.item-checkbox :refer [item-checkbox]]
            [reagent.core :as r]
            [status-im.participants.styles :as st]))

(defn participant-contact [{:keys [whisper-identity] :as contact}]
  ;; todo must be moved to handlers
  (let [checked (r/atom false)]
    (fn [{:keys [whisper-identity] :as contact}]
      [view st/participant-container
       [item-checkbox {:onToggle (fn [checked?]
                                   (reset! checked checked?)
                                   (dispatch [:select-new-participant whisper-identity checked?]))
                       :checked  @checked
                       :size     30}]
       [contact-inner-view contact]])))

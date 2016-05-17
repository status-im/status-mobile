(ns syng-im.new-group.views.contact
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view]]
            [syng-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [syng-im.components.item-checkbox :refer [item-checkbox]]
            [reagent.core :as r]
            [syng-im.new-group.styles :as st]))

(defn new-group-contact [{:keys [whisper-identity] :as contact}]
  (let [checked (r/atom false)]
    (fn []
      [view st/contact-container
       [item-checkbox
        {:onToggle (fn [checked?]
                     (reset! checked checked?)
                     (dispatch [:select-for-new-group whisper-identity checked?]))
         :checked  @checked
         :size     30}]
       [contact-inner-view contact]])))

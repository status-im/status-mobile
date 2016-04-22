(ns syng-im.components.chats.new-group-contact
  (:require [syng-im.resources :as res]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view]]
            [syng-im.components.contact-list.contact-inner :refer [contact-inner-view]]
            [syng-im.components.item-checkbox :refer [item-checkbox]]
            [syng-im.utils.logging :as log]
            [reagent.core :as r]))

(defn new-group-contact [{:keys [whisper-identity] :as contact} navigator]
  (let [checked (r/atom false)]
    (fn []
      [view {:style {:flexDirection "row"
                     :height        56}}
       [item-checkbox {:onToggle (fn [checked?]
                                   (reset! checked checked?)
                                   (dispatch [:select-for-new-group whisper-identity checked?]))
                       :checked  @checked
                       :size     30}]
       [contact-inner-view contact]])))

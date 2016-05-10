(ns syng-im.components.contact-list.contact
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view text image touchable-highlight]]
            [syng-im.resources :as res]
            [syng-im.components.contact-list.contact-inner :refer [contact-inner-view]]))

(defn show-chat [navigator whisper-identity]
  (dispatch [:show-chat whisper-identity navigator :push]))

(defn contact-view [{:keys [navigator contact]}]
  (let [{:keys [whisper-identity]} contact]
    [touchable-highlight {:onPress #(show-chat navigator whisper-identity)}
     [view {} [contact-inner-view contact]]]))

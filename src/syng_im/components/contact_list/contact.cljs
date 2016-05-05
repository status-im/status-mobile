(ns syng-im.components.contact-list.contact
  (:require [syng-im.components.react :refer [view text image touchable-highlight]]
            [syng-im.resources :as res]
            [syng-im.navigation :as nav]
            [syng-im.components.contact-list.contact-inner :refer [contact-inner-view]]))

(defn show-chat [navigator whisper-identity]
  (nav/nav-push navigator {:view-id :chat}))

(defn contact-view [{:keys [navigator contact]}]
  (let [{:keys [whisper-identity]} contact]
    [touchable-highlight {:onPress (fn []
                                     (show-chat navigator whisper-identity))
                          :underlay-color :transparent}
     [view {} [contact-inner-view contact]]]))

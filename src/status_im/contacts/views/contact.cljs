(ns status-im.contacts.views.contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :refer [view touchable-highlight]]
            [re-frame.core :refer [dispatch subscribe]]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]))

(defn on-press [chat whisper-identity]
  (if chat
    #(dispatch [:navigate-to :chat whisper-identity])
    #(dispatch [:start-chat whisper-identity])))

(defview contact-view [{:keys [whisper-identity] :as contact}]
  [chat [:get-chat whisper-identity]]
  [touchable-highlight
   {:onPress (on-press chat whisper-identity)}
   [view {} [contact-inner-view contact]]])

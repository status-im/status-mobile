(ns syng-im.contacts.views.contact
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [syng-im.components.react :refer [view touchable-highlight]]
            [re-frame.core :refer [dispatch subscribe]]
            [syng-im.contacts.views.contact-inner :refer [contact-inner-view]]))

(defn on-press [chat whisper-identity]
  (if chat
    #(dispatch [:navigate-to :chat whisper-identity])
    #(dispatch [:start-chat whisper-identity])))

(defview contact-view [{:keys [whisper-identity] :as contact}]
  [chat [:get-chat whisper-identity]]
  [touchable-highlight
   {:onPress (on-press chat whisper-identity)}
   [view {} [contact-inner-view contact]]])

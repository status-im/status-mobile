(ns status-im.contacts.views.contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :refer [view text icon touchable-highlight]]
            [re-frame.core :refer [dispatch]]
            [status-im.contacts.styles :as st]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]))

(defn letter-view [letter]
  [view st/letter-container
   (when letter
     [text {:style st/letter-text} letter])])

(defn on-press [whisper-identity]
  #(dispatch [:start-chat whisper-identity]))

(defview contact-with-letter-view [{:keys [whisper-identity letter] :as contact}]
  [chat [:get-chat whisper-identity]]
  [touchable-highlight
   {:onPress (on-press whisper-identity)}
   [view st/contact-container
    [letter-view letter]
    [contact-inner-view contact]]])

(defview contact-extended-view [{:keys [whisper-identity] :as contact} info click-handler more-click-handler]
  [chat [:get-chat whisper-identity]]
  [touchable-highlight
   {:onPress click-handler}
   [view st/contact-container
    [contact-inner-view contact info]
    [touchable-highlight
     {:on-press more-click-handler}
     [view st/more-btn
      [icon :more_vertical st/more-btn-icon]]]]])

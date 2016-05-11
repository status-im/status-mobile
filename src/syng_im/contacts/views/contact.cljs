(ns syng-im.contacts.views.contact
  (:require [syng-im.components.react :refer [view touchable-highlight]]
            [re-frame.core :refer [dispatch]]
            [syng-im.contacts.views.contact-inner :refer [contact-inner-view]]))

(defn contact-view [contact]
  [touchable-highlight {:onPress #(dispatch [:navigate-to :chat])}
   [view {} [contact-inner-view contact]]])

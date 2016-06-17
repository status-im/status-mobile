(ns status-im.contacts.views.contact-inner
  (:require [clojure.string :as s]
            [status-im.components.react :refer [view image text]]
            [status-im.components.chat-icon.screen :refer [contact-icon-contacts-tab]]
            [status-im.contacts.styles :as st]
            [status-im.i18n :refer [label]]))

(defn contact-photo [{:keys [whisper-identity]}]
  [view st/contact-photo-container
   [contact-icon-contacts-tab whisper-identity]])

(defn contact-inner-view [{:keys [name] :as contact}]
  [view st/contact-container
   [contact-photo contact]
   [view st/name-container
    [text {:style st/name-text}
     (if (pos? (count name))
       name
       ;; todo is this correct behaviour?
       (label :t/no-name))]]])

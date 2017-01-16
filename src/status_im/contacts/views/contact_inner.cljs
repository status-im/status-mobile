(ns status-im.contacts.views.contact-inner
  (:require [status-im.components.react :refer [view image text]]
            [status-im.components.chat-icon.screen :refer [contact-icon-contacts-tab]]
            [status-im.contacts.styles :as st]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            [status-im.i18n :refer [get-contact-translated label]]))

(defn contact-photo [contact]
  [view st/contact-photo-container
   [contact-icon-contacts-tab contact]])

(defn contact-inner-view
  ([contact]
   (contact-inner-view contact nil))
  ([{:keys [whisper-identity name] :as contact} info]
   [view st/contact-inner-container
    [contact-photo contact]
    [view st/info-container
     [text {:style           st/name-text
            :number-of-lines 1}
      (if (pos? (count (:name contact)))
        (get-contact-translated whisper-identity :name name)
        ;; todo is this correct behaviour?
        (generate-gfy))]
     (when info
       [text {:style st/info-text}
        info])]]))

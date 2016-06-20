(ns status-im.contacts.views.contact-inner
  (:require [clojure.string :as s]
            [status-im.components.react :refer [view image text touchable-highlight icon]]
            [status-im.components.chat-icon.screen :refer [contact-icon-contacts-tab]]
            [status-im.contacts.styles :as st]
            [status-im.i18n :refer [label]]))

(defn letter-view [letter]
  [view st/letter-container
   (when letter
     [text {:style st/letter-text}
      letter])])

(defn contact-photo [contact]
  [view st/contact-photo-container
   [contact-icon-contacts-tab contact]])

(defn contact-inner-view [{:keys [name letter] :as contact}]
  [view st/contact-container
   [letter-view letter]
   [contact-photo contact]
   [view st/name-container
    [text {:style st/name-text}
     (if (pos? (count name))
       name
       ;; todo is this correct behaviour?
       (label :t/no-name))]]])

(defn contact-inner-view-2 [{:keys [name] :as contact}]
  [view st/contact-container
   [contact-photo contact]
   [view st/name-container
    [text {:style st/name-text}
     (if (pos? (count name))
       name
       ;; todo is this correct behaviour?
       (label :t/no-name))]]
   [touchable-highlight
    ;; TODO not imlemented: contact more button
    {:on-press nil}
    [view st/more-btn
     [icon :more-vertical st/more-btn-icon]]]])

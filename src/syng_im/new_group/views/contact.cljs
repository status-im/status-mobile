(ns syng-im.new-group.views.contact
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view]]
            [syng-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [syng-im.components.item-checkbox :refer [item-checkbox]]
            [syng-im.new-group.styles :as st]))

(defn on-toggle [whisper-identity]
  (fn [checked?]
    (println checked?)
    (let [action (if checked? :select-contact :deselect-contact)]
      (dispatch [action whisper-identity]))))

(defview new-group-contact [{:keys [whisper-identity] :as contact}]
  [checked [:is-contact-selected? whisper-identity]]
  [view st/contact-container
   [item-checkbox
    {:onToggle (on-toggle whisper-identity)
     :checked  checked
     :size     30}]
   [contact-inner-view contact]])

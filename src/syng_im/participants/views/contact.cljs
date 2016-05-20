(ns syng-im.participants.views.contact
  (:require-macros [syng-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.components.react :refer [view]]
            [syng-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [syng-im.components.item-checkbox :refer [item-checkbox]]
            [reagent.core :as r]
            [syng-im.participants.styles :as st]))

;; todo duplication
(defn on-toggle [whisper-identity]
  (fn [checked?]
    (let [action (if checked? :select-participant :deselect-participant)]
      (dispatch [action whisper-identity]))))

(defview participant-contact
  [{:keys [whisper-identity] :as contact}]
  [checked [:is-participant-selected? whisper-identity]]
  [view st/participant-container
   [item-checkbox {:onToggle (on-toggle whisper-identity)
                   :checked  checked
                   :size     30}]
   [contact-inner-view contact]])

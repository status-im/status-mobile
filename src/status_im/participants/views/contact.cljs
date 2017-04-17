(ns status-im.participants.views.contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view]]
            [status-im.components.contact.contact :refer [contact-inner-view]]
            [status-im.components.item-checkbox :refer [item-checkbox]]
            [status-im.participants.styles :as st]))

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
   [contact-inner-view {:contact contact}]])

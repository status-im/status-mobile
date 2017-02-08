(ns status-im.new-group.views.contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view icon touchable-highlight]]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [status-im.new-group.styles :as st]))

(defn on-toggle [checked? whisper-identity]
  (let [action (if checked? :deselect-contact :select-contact)]
    (dispatch [action whisper-identity])))

(defview new-group-contact [{:keys [whisper-identity] :as contact}]
  [checked [:is-contact-selected? whisper-identity]]
  [touchable-highlight {:on-press #(on-toggle checked whisper-identity)}
   [view st/contact-container
     [contact-inner-view (merge {:contact contact}
                                (when checked {:style {:background-color "#eff3fc"}}))]
     [view st/toggle-container
      [icon :options_gray]]]])

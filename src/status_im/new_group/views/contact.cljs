(ns status-im.new-group.views.contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view icon touchable-highlight]]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [status-im.new-group.styles :as st]
            [status-im.contacts.styles :as cst]
            [status-im.components.styles :refer [color-light-blue color-gray5]]
            [status-im.utils.platform :refer [platform-specific]]))

(defn on-toggle [checked? whisper-identity]
  (let [action (if checked? :deselect-contact :select-contact)]
    (dispatch [action whisper-identity])))

;;TODO: maybe it's better to have only one global component contact-view (with the types: default, extended and toggle)
(defview new-group-contact [{:keys [whisper-identity] :as contact}]
  [checked [:is-contact-selected? whisper-identity]]
  [touchable-highlight {:on-press #(on-toggle checked whisper-identity)}
   [view
    [view (merge st/contact-container (when checked {:style st/selected-contact}))
      [contact-inner-view (merge {:contact contact}
                                 (when checked {:style st/selected-contact}))]
      [view st/toggle-container
       [view (merge st/icon-check-container
                    {:background-color (if checked color-light-blue color-gray5)})
        (when checked
          [icon :check_on st/check-icon])]]]]])

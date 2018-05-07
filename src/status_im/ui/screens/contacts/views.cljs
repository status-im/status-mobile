(ns status-im.ui.screens.contacts.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.ui.components.react :refer [view text touchable-highlight scroll-view]]
            [status-im.ui.components.contact.contact :refer [contact-view]]
            [status-im.utils.platform :refer [platform-specific ios? android?]]
            [status-im.utils.utils :as u]
            [status-im.i18n :refer [label]]
            [status-im.ui.components.styles :refer [color-blue]]))

(defn contact-options [{:keys [unremovable?] :as contact} group]
  (let [delete-contact-opt {:action       #(u/show-confirmation
                                            (str (label :t/delete-contact) "?") (label :t/delete-contact-confirmation)
                                            (label :t/delete)
                                            (fn [] (dispatch [:hide-contact contact])))
                            :label        (label :t/delete-contact)
                            :destructive? true}
        options            (if unremovable? [] [delete-contact-opt])]
    (if group
      (conj options
            {:action #(dispatch [:remove-contact-from-group
                                 (:whisper-identity contact)
                                 (:group-id group)])
             :label  (label :t/remove-from-group)})
      options)))


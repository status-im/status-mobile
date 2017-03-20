(ns status-im.contacts.views.contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :refer [view text icon touchable-highlight]]
            [re-frame.core :refer [dispatch]]
            [status-im.contacts.styles :as st]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.i18n :refer [label]]
            [status-im.utils.platform :refer [platform-specific]]))

(defn- on-press [{:keys [whisper-identity] :as contact}]
  (dispatch [:send-contact-request! contact])
  (dispatch [:start-chat whisper-identity {} :navigation-replace]))

(defn letter-view [letter]
  [view st/letter-container
   (when letter
     [text {:style st/letter-text} letter])])

(defn options-btn [contact more-options]
  (let [options [{:value        #(dispatch [:hide-contact contact])
                  :text         (label :t/delete-contact)
                  :destructive? true}]]
    [view st/more-btn
     [context-menu
      [icon :options_gray]
      (or more-options options)]]))

;;TODO: maybe it's better to have only one global component contact-view with the types: default, extended and toggle
;;TODO: at the moment toggle in the other component new-group-contact
(defview contact-view [{{:keys [whisper-identity letter] :as contact} :contact
                        :keys [extended? letter? on-click extend-options info]}]
  [chat [:get-chat whisper-identity]]
  [touchable-highlight
   (when-not extended?
     {:on-press #((or on-click on-press) contact)})
   [view
    [view st/contact-container
     (when letter?
       [letter-view letter])
     [contact-inner-view {:contact contact :info info}]
     (when extended?
       [options-btn contact extend-options])]]])

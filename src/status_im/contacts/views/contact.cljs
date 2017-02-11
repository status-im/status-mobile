(ns status-im.contacts.views.contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :refer [view text icon touchable-highlight]]
            [re-frame.core :refer [dispatch]]
            [status-im.contacts.styles :as st]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [status-im.utils.platform :refer [platform-specific]]))

(defn- on-press [{:keys [whisper-identity] :as contact}]
  (dispatch [:send-contact-request! contact])
  (dispatch [:start-chat whisper-identity {} :navigation-replace]))

(defn- more-on-press [contact]
  (dispatch [:open-contact-menu (:list-selection-fn platform-specific) contact]))

(defn letter-view [letter]
  [view st/letter-container
   (when letter
     [text {:style st/letter-text} letter])])

;;TODO: maybe it's better to have only one global component contact-view with the types: default, extended and toggle
;;TODO: at the moment toggle in the other component new-group-contact
(defview contact-view [{{:keys [whisper-identity letter dapp?] :as contact} :contact
                        :keys [extended? letter? on-click more-on-click info]}]
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
       [touchable-highlight
        {:on-press #((or more-on-click more-on-press) contact)}
        [view st/more-btn
         [icon :options_gray st/options-icon]]])]
    [view st/contact-separator-container
     [view (get-in platform-specific [:component-styles :contacts :separator])]]]])

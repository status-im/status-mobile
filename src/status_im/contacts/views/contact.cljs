(ns status-im.contacts.views.contact
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [status-im.components.react :refer [view text icon touchable-highlight]]
            [re-frame.core :refer [dispatch]]
            [status-im.contacts.styles :as st]
            [status-im.contacts.views.contact-inner :refer [contact-inner-view]]
            [status-im.utils.platform :refer [platform-specific]]))

(defn- on-press [{:keys [whisper-identity]}]
  (dispatch [:start-chat whisper-identity {} :navigation-replace]))

(defn- more-on-press [contact]
  (dispatch [:open-contact-menu (:list-selection-fn platform-specific) contact]))

(defn letter-view [letter]
  [view st/letter-container
   (when letter
     [text {:style st/letter-text} letter])])

(defview contact-view [{{:keys [whisper-identity letter dapp?] :as contact} :contact
                        :keys [extended? letter? on-click more-on-click info]}]
  [chat [:get-chat whisper-identity]]
  [touchable-highlight
   {:on-press #((or on-click on-press) contact)}
   [view st/contact-container
    (when letter?
      [letter-view letter])
    [contact-inner-view contact info]
    (when (and extended? (not dapp?))
      [touchable-highlight
       {:on-press #((or more-on-click more-on-press) contact)}
       [view st/more-btn
        [icon :more_vertical st/more-btn-icon]]])]])

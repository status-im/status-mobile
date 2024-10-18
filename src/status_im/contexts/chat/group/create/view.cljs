(ns status-im.contexts.chat.group.create.view
  (:require
    [clojure.string :as string]
    [quo.foundations.colors :as colors]
    [status-im.constants :as constants]
    [status-im.contexts.chat.group.common.group-edit :as group-edit]
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [close]}]
  (let [contacts           (rf/sub [:selected-group-contacts])
        contact-string     (string/join ", " (map profile.utils/displayed-name contacts))
        default-group-name (subs contact-string
                                 0
                                 (min (count contact-string) constants/max-group-chat-name-length))
        profile            (rf/sub [:profile/profile-with-image])
        contacts           (conj contacts profile)]
    [group-edit/view
     {:default-group-name  default-group-name
      :default-group-color (rand-nth colors/account-colors)
      :contacts            contacts
      :submit-button-label (i18n/label :t/create-group-chat)
      :submit-event        :group-chat/create
      :back-button-icon    :i/arrow-left
      :close               close}]))

(ns status-im.contexts.profile.contact.unblock-contact.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.profile.contact.block-contact.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-close [] (rf/dispatch [:hide-bottom-sheet]))

(defn view
  [contact]
  (let [{:keys [customization-color
                public-key]} contact
        full-name            (profile.utils/displayed-name contact)
        profile-picture      (profile.utils/photo contact)
        on-unblock-press     (rn/use-callback
                              (fn []
                                (rf/dispatch [:toasts/upsert
                                              {:id    :user-unblocked
                                               :type  :positive
                                               :theme :dark
                                               :text  (i18n/label :t/user-unblocked
                                                                  {:username
                                                                   full-name})}])
                                (rf/dispatch [:contact/unblock-contact
                                              public-key])
                                (on-close))
                              [public-key full-name])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :title               (i18n/label :t/unblock)
       :full-name           full-name
       :profile-picture     profile-picture
       :customization-color customization-color}]
     [rn/view {:style style/content-wrapper}
      [quo/text
       {:weight :medium
        :size   :paragraph-1}
       (i18n/label :t/unblocking-a-user-message {:username full-name})]]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/unblock)
       :button-one-props {:type                :danger
                          :accessibility-label :block-contact
                          :on-press            on-unblock-press}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type                :grey
                          :accessibility-label :cancel
                          :on-press            on-close}}]]))

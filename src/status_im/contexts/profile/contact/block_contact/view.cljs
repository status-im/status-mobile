(ns status-im.contexts.profile.contact.block-contact.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.block-contact.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-close [] (rf/dispatch [:hide-bottom-sheet]))

(defn view
  []
  (let [{:keys [customization-color contact-request-state public-key]
         :as   contact}     (rf/sub [:contacts/current-contact])
        customization-color customization-color
        full-name           (profile.utils/displayed-name contact)
        profile-picture     (profile.utils/photo contact)
        on-block-press      (rn/use-callback
                             (fn []
                               (rf/dispatch [:toasts/upsert
                                             {:id   :user-blocked
                                              :type :positive
                                              :text (i18n/label :t/user-blocked
                                                                {:username
                                                                 full-name})}])
                               (rf/dispatch [:contact/block-contact
                                             public-key])
                               (rf/dispatch [:contact.ui/remove-contact-pressed
                                             {:public-key public-key}])
                               (on-close))
                             [public-key full-name])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :title               (i18n/label :t/block)
       :full-name           full-name
       :profile-picture     profile-picture
       :customization-color customization-color}]
     [rn/view {:style style/content-wrapper}
      [quo/text
       {:weight :medium
        :size   :paragraph-1}
       (i18n/label :t/block-user-title-message {:username full-name})]
      [quo/information-box
       {:icon :i/info
        :type :default}
       (i18n/label :t/blocking-a-user-message {:username full-name})]
      (when (= constants/contact-request-state-mutual contact-request-state)
        [rn/view
         {:style style/checkbox-wrapper}
         [quo/selectors
          {:type                :checkbox
           :customization-color customization-color
           :checked?            true
           :disabled?           true}]
         [quo/text (i18n/label :t/remove-contact)]])]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/block)
       :button-one-props {:type                :danger
                          :accessibility-label :block-contact
                          :on-press            on-block-press}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type                :grey
                          :accessibility-label :cancel
                          :on-press            on-close}}]]))

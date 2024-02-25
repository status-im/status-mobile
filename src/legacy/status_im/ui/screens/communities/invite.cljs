(ns legacy.status-im.ui.screens.communities.invite
  (:require
    [legacy.status-im.communities.core :as communities]
    [legacy.status-im.ui.screens.communities.style :as style]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [status-im.common.contact-list-item.view :as contact-list-item]
    [status-im.common.contact-list.view :as contact-list]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- contact-item-render
  [_]
  (fn [{:keys [public-key] :as item}]
    (let [user-selected? (rf/sub [:is-contact-selected? public-key])
          on-toggle      #(if user-selected?
                            (rf/dispatch [:deselect-contact public-key])
                            (rf/dispatch [:select-contact public-key]))]
      [contact-list-item/contact-list-item
       {:on-press                on-toggle
        :allow-multiple-presses? true
        :accessory               {:type     :checkbox
                                  :checked? user-selected?
                                  :on-check on-toggle}}
       item])))

(defn view-internal
  [{:keys [theme]}]
  (let [{:keys [id]}          (rf/sub [:get-screen-params])
        {:keys [name images]} (rf/sub [:communities/community id])]
    (fn []
      (rn/use-unmount #(rf/dispatch [:group-chat/clear-contacts]))
      (let [selected                (rf/sub [:group/selected-contacts])
            selected-contacts-count (count selected)
            on-press                (fn []
                                      (rf/dispatch [::communities/share-community-confirmation-pressed
                                                    selected id])
                                      (rf/dispatch [:navigate-back])
                                      (rf/dispatch [:toasts/upsert
                                                    {:type  :negative
                                                     :theme :dark
                                                     :text  (if (= 1 selected-contacts-count)
                                                              (i18n/label :t/one-user-was-invited)
                                                              (i18n/label
                                                               :t/n-users-were-invited
                                                               {:count selected-contacts-count}))}]))]
        [rn/view
         {:flex 1}
         [blur/view
          {:style         {:padding-horizontal 20}
           :blur-amount   0
           :blur-type     :transparent
           :overlay-color (colors/theme-colors colors/white-70-blur colors/neutral-95-opa-70-blur)
           :blur-radius   (if platform/ios? 20 10)}
          [quo/button
           {:type       :grey
            :size       32
            :icon-only? true
            :on-press   #(rf/dispatch [:navigate-back])} :i/close]
          [rn/view {:style style/contact-selection-heading}
           [quo/text
            {:weight :semi-bold
             :size   :heading-1
             :style  {:color (colors/theme-colors colors/neutral-100 colors/white theme)}}
            (i18n/label :t/invite-to-community)]]
          [quo/context-tag
           {:type            :community
            :size            24
            :community-logo  (:thumbnail images)
            :community-name  name
            :container-style {:align-self    :flex-start
                              :margin-top    -8
                              :margin-bottom 8}}]]
         [gesture/section-list
          {:key-fn                         :public-key
           :sticky-section-headers-enabled false
           :sections                       (rf/sub [:contacts/filtered-active-sections])
           :render-section-header-fn       contact-list/contacts-section-header
           :content-container-style        {:padding-bottom 70}
           :render-fn                      contact-item-render}]
         (when (pos? selected-contacts-count)
           [rn/view
            [quo/button
             {:type                :primary
              :accessibility-label :next-button
              :container-style     style/chat-button
              :on-press            on-press}
             (i18n/label :t/invite-n-users {:count selected-contacts-count})]])]))))

(def legacy-invite (quo.theme/with-theme view-internal))

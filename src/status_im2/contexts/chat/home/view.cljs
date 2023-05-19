(ns status-im2.contexts.chat.home.view
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [quo2.core :as quo]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.platform :as platform]
            [status-im2.contexts.chat.home.style :as style]
            [status-im2.common.contact-list.view :as contact-list]
            [status-im2.common.home.view :as common.home]
            [status-im2.contexts.chat.home.chat-list-item.view :as chat-list-item]
            [status-im2.contexts.chat.home.contact-request.view :as contact-request]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [status-im2.common.home.actions.view :as actions]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.chat.actions.view :as home.sheet]))

(defn get-item-layout
  [_ index]
  #js {:length 56 :offset (* 56 index) :index index})

(defn filter-items-by-tab
  [tab items]
  (if (= tab :tab/groups)
    (filter :group-chat items)
    (filter :chat-id items)))

(defn welcome-blank-chats
  []
  [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [quo/icon :i/placeholder]
   [quo/text {:weight :semi-bold} (i18n/label :t/no-messages)]
   [quo/text (i18n/label :t/blank-messages-text)]])

(defn chats
  [selected-tab top]
  (let [unfiltered-items (rf/sub [:chats-stack-items])
        items            (filter-items-by-tab selected-tab unfiltered-items)]
    (if (empty? items)
      [welcome-blank-chats]
      [rn/flat-list
       {:key-fn                            #(or (:chat-id %) (:public-key %) (:id %))
        :content-inset-adjustment-behavior :never
        :header                            [rn/view {:height (+ 245 top)}]
        :get-item-layout                   get-item-layout
        :on-end-reached                    #(re-frame/dispatch [:chat/show-more-chats])
        :keyboard-should-persist-taps      :always
        :data                              items
        :render-fn                         chat-list-item/chat-list-item}])))

(defn welcome-blank-contacts
  []
  [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [quo/icon :i/placeholder]
   [quo/text {:weight :semi-bold} (i18n/label :t/no-contacts)]
   [quo/text (i18n/label :t/blank-contacts-text)]])

(defn contact-item-render
  [{:keys [public-key] :as item}]
  (let [current-pk           (rf/sub [:multiaccount/public-key])
        show-profile-actions #(rf/dispatch [:show-bottom-sheet
                                            {:content (fn [] [actions/contact-actions item])}])]
    [contact-list-item/contact-list-item
     (when (not= public-key current-pk)
       {:on-press      #(rf/dispatch [:chat.ui/show-profile public-key])
        :on-long-press show-profile-actions
        :accessory     {:type     :options
                        :on-press show-profile-actions}})
     item]))

(defn contacts
  [pending-contact-requests top]
  (let [items (rf/sub [:contacts/active-sections])]
    (if (and (empty? items) (empty? pending-contact-requests))
      [welcome-blank-contacts]
      [rn/section-list
       {:key-fn                            :public-key
        :get-item-layout                   get-item-layout
        :content-inset-adjustment-behavior :never
        :header                            [:<>
                                            [rn/view {:height (+ 245 top)}]
                                            (when (seq pending-contact-requests)
                                              [contact-request/contact-requests
                                               pending-contact-requests])]
        :sections                          items
        :sticky-section-headers-enabled    false
        :render-section-header-fn          contact-list/contacts-section-header
        :render-fn                         contact-item-render}])))

(defn get-tabs-data
  [dot?]
  [{:id :tab/recent :label (i18n/label :t/recent) :accessibility-label :tab-recent}
   {:id :tab/groups :label (i18n/label :t/groups) :accessibility-label :tab-groups}
   {:id                  :tab/contacts
    :label               (i18n/label :t/contacts)
    :accessibility-label :tab-contacts
    :notification-dot?   dot?}])

(defn home
  []
  (fn []
    (let [pending-contact-requests (rf/sub [:activity-center/pending-contact-requests])
          selected-tab             (or (rf/sub [:messages-home/selected-tab]) :tab/recent)
          {:keys [key-uid]}        (rf/sub [:multiaccount])
          profile-color            (:color (rf/sub [:onboarding-2/profile]))
          customization-color      (if profile-color
                                     profile-color
                                     (rf/sub [:profile/customization-color key-uid]))
          top                      (safe-area/get-top)]
      [:<>
       (if (= selected-tab :tab/contacts)
         [contacts pending-contact-requests top]
         [chats selected-tab top])
       [rn/view
        {:style (style/blur-container top)}
        [blur/view
         {:blur-amount (if platform/ios? 20 10)
          :blur-type   (if (colors/dark?) :dark (if platform/ios? :light :xlight))
          :style       style/blur}]
        [common.home/top-nav
         {:type   :grey
          :avatar {:customization-color customization-color}}]
        [common.home/title-column
         {:label               (i18n/label :t/messages)
          :handler             #(rf/dispatch [:show-bottom-sheet {:content home.sheet/new-chat}])
          :accessibility-label :new-chat-button
          :customization-color customization-color}]
        [quo/discover-card
         {:banner      (resources/get-image :invite-friends)
          :title       (i18n/label :t/invite-friends-to-status)
          :description (i18n/label :t/share-invite-link)}]
        ^{:key (str "tabs-" selected-tab)}
        [quo/tabs
         {:style          style/tabs
          :size           32
          :on-change      (fn [tab]
                            (rf/dispatch [:messages-home/select-tab tab]))
          :default-active selected-tab
          :data           (get-tabs-data (pos? (count pending-contact-requests)))}]]])))

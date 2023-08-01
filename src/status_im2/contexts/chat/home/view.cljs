(ns status-im2.contexts.chat.home.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [re-frame.core :as re-frame]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.safe-area :as safe-area]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [status-im2.common.contact-list.view :as contact-list]
            [status-im2.common.home.actions.view :as actions]
            [status-im2.common.home.view :as common.home]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.chat.actions.view :as home.sheet]
            [status-im2.contexts.chat.home.chat-list-item.view :as chat-list-item]
            [status-im2.contexts.chat.home.contact-request.view :as contact-request]
            [status-im2.contexts.chat.home.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn get-item-layout
  [_ index]
  #js {:length 56 :offset (* 56 index) :index index})

(defn filter-and-sort-items-by-tab
  [tab items]
  (let [k (if (= tab :tab/groups) :group-chat :chat-id)]
    (->> items
         (filter k)
         (sort-by :timestamp >))))

(def empty-state-content
  #:tab{:contacts
        {:title       (i18n/label :t/no-contacts)
         :description (i18n/label :t/no-contacts-description)
         :image       (resources/get-image
                       (theme/theme-value :no-contacts-light :no-contacts-dark))}
        :groups
        {:title       (i18n/label :t/no-group-chats)
         :description (i18n/label :t/no-group-chats-description)
         :image       (resources/get-image
                       (theme/theme-value :no-group-chats-light :no-group-chats-dark))}
        :recent
        {:title       (i18n/label :t/no-messages)
         :description (i18n/label :t/no-messages-description)
         :image       (resources/get-image
                       (theme/theme-value :no-messages-light :no-messages-dark))}})

(defn chats
  [selected-tab top]
  (let [unfiltered-items (rf/sub [:chats-stack-items])
        items            (filter-and-sort-items-by-tab selected-tab unfiltered-items)]
    (if (empty? items)
      [empty-state {:top top :selected-tab selected-tab}]
      [rn/flat-list
       {:key-fn                            #(or (:chat-id %) (:public-key %) (:id %))
        :content-inset-adjustment-behavior :never
        :header                            [rn/view {:style (style/header-space top)}]
        :get-item-layout                   get-item-layout
        :on-end-reached                    #(re-frame/dispatch [:chat/show-more-chats])
        :keyboard-should-persist-taps      :always
        :data                              items
        :render-fn                         chat-list-item/chat-list-item}])))

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
      [common.home/empty-state-image
       {:selected-tab :tab/contacts
        :tab->content empty-state-content}]
      [rn/section-list
       {:key-fn                            :public-key
        :get-item-layout                   get-item-layout
        :content-inset-adjustment-behavior :never
        :header                            [:<>
                                            [common.home/header-spacing]
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
  (let [pending-contact-requests (rf/sub [:activity-center/pending-contact-requests])
        selected-tab             (or (rf/sub [:messages-home/selected-tab]) :tab/recent)
        customization-color      (rf/sub [:profile/customization-color])
        top                      (safe-area/get-top)]
    [:<>
     (if (= selected-tab :tab/contacts)
       [contacts pending-contact-requests top]
       [chats selected-tab top])
     [rn/view {:style (style/blur-container top)}
      (let [{:keys [sheets]} (rf/sub [:bottom-sheet])]
        [blur/view
         {:blur-amount   (if platform/ios? 20 10)
          :blur-type     (if (colors/dark?) :dark (if platform/ios? :light :xlight))
          :style         style/blur
          :overlay-color (if (seq sheets)
                           (theme/theme-value colors/white colors/neutral-95-opa-70)
                           (when (colors/dark?)
                             colors/neutral-95-opa-70))}])
      [common.home/top-nav {:type :grey}]
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
        :data           (get-tabs-data (pos? (count pending-contact-requests)))}]]]))

(ns status-im2.contexts.chat.home.view
  (:require [oops.core :as oops]
            [quo2.theme :as quo.theme]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [status-im2.common.contact-list.view :as contact-list]
            [status-im2.common.home.actions.view :as actions]
            [status-im2.common.home.banner.view :as common.banner]
            [status-im2.common.home.empty-state.view :as common.empty-state]
            [status-im2.common.home.header-spacing.view :as common.header-spacing]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.chat.actions.view :as chat.actions.view]
            [status-im2.contexts.chat.home.chat-list-item.view :as chat-list-item]
            [status-im2.contexts.chat.home.contact-request.view :as contact-request]
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

(defn empty-state-content
  [theme]
  #:tab{:contacts
        {:title       (i18n/label :t/no-contacts)
         :description (i18n/label :t/no-contacts-description)
         :image       (resources/get-image
                       (quo.theme/theme-value :no-contacts-light :no-contacts-dark theme))}
        :groups
        {:title       (i18n/label :t/no-group-chats)
         :description (i18n/label :t/no-group-chats-description)
         :image       (resources/get-image
                       (quo.theme/theme-value :no-group-chats-light :no-group-chats-dark theme))}
        :recent
        {:title       (i18n/label :t/no-messages)
         :description (i18n/label :t/no-messages-description)
         :image       (resources/get-image
                       (quo.theme/theme-value :no-messages-light :no-messages-dark theme))}})

(defn chats
  [{:keys [theme selected-tab set-scroll-ref scroll-shared-value]}]
  (let [unfiltered-items (rf/sub [:chats-stack-items])
        items            (filter-and-sort-items-by-tab selected-tab unfiltered-items)]
    (if (empty? items)
      [common.empty-state/view
       {:selected-tab selected-tab
        :tab->content (empty-state-content theme)}]
      [reanimated/flat-list
       {:ref                               set-scroll-ref
        :key-fn                            #(or (:chat-id %) (:public-key %) (:id %))
        :content-inset-adjustment-behavior :never
        :header                            [common.header-spacing/view]
        :get-item-layout                   get-item-layout
        :on-end-reached                    #(re-frame/dispatch [:chat/show-more-chats])
        :keyboard-should-persist-taps      :always
        :data                              items
        :render-fn                         chat-list-item/chat-list-item
        :scroll-event-throttle             8
        :on-scroll                         #(common.banner/set-scroll-shared-value
                                             {:scroll-input (oops/oget % "nativeEvent.contentOffset.y")
                                              :shared-value scroll-shared-value})}])))

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
  [{:keys [theme pending-contact-requests set-scroll-ref scroll-shared-value]}]
  (let [items (rf/sub [:contacts/active-sections])]
    (if (and (empty? items) (empty? pending-contact-requests))
      [common.empty-state/view
       {:selected-tab :tab/contacts
        :tab->content (empty-state-content theme)}]
      [rn/section-list
       {:ref                               set-scroll-ref
        :key-fn                            :public-key
        :get-item-layout                   get-item-layout
        :content-inset-adjustment-behavior :never
        :header                            [:<>
                                            [common.header-spacing/view]
                                            (when (seq pending-contact-requests)
                                              [contact-request/contact-requests
                                               pending-contact-requests])]
        :sections                          items
        :sticky-section-headers-enabled    false
        :render-section-header-fn          contact-list/contacts-section-header
        :render-fn                         contact-item-render
        :scroll-event-throttle             8
        :on-scroll                         #(common.banner/set-scroll-shared-value
                                             {:scroll-input (oops/oget % "nativeEvent.contentOffset.y")
                                              :shared-value scroll-shared-value})}])))

(def ^:private banner-data
  {:title-props
   {:label               (i18n/label :t/messages)
    :handler             #(rf/dispatch
                           [:show-bottom-sheet {:content chat.actions.view/new-chat}])
    :accessibility-label :new-chat-button}
   :card-props
   {:banner      (resources/get-image :invite-friends)
    :title       (i18n/label :t/invite-friends-to-status)
    :description (i18n/label :t/share-invite-link)}})

(defn- f-view-internal
  [{:keys [theme]}]
  (let [scroll-ref     (atom nil)
        set-scroll-ref #(reset! scroll-ref %)]
    (fn []
      (let [customization-color      (rf/sub [:profile/customization-color])
            pending-contact-requests (rf/sub [:activity-center/pending-contact-requests])
            selected-tab             (or (rf/sub [:messages-home/selected-tab]) :tab/recent)
            scroll-shared-value      (reanimated/use-shared-value 0)]
        [:<>
         (if (= selected-tab :tab/contacts)
           [contacts
            {:pending-contact-requests pending-contact-requests
             :set-scroll-ref           set-scroll-ref
             :scroll-shared-value      scroll-shared-value
             :theme                    theme}]
           [chats
            {:selected-tab        selected-tab
             :set-scroll-ref      set-scroll-ref
             :scroll-shared-value scroll-shared-value
             :theme               theme}])
         [:f> common.banner/animated-banner
          {:content             banner-data
           :customization-color customization-color
           :scroll-ref          scroll-ref
           :tabs                [{:id                  :tab/recent
                                  :label               (i18n/label :t/recent)
                                  :accessibility-label :tab-recent}
                                 {:id                  :tab/groups
                                  :label               (i18n/label :t/groups)
                                  :accessibility-label :tab-groups}
                                 {:id                  :tab/contacts
                                  :label               (i18n/label :t/contacts)
                                  :accessibility-label :tab-contacts
                                  :notification-dot?   (pos? (count pending-contact-requests))}]
           :selected-tab        selected-tab
           :on-tab-change       (fn [tab] (rf/dispatch [:messages-home/select-tab tab]))
           :scroll-shared-value scroll-shared-value}]]))))

(defn- internal-chats-home-view
  [params]
  [:f> f-view-internal params])

(def view (quo.theme/with-theme internal-chats-home-view))

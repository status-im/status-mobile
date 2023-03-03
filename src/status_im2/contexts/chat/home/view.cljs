(ns status-im2.contexts.chat.home.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.contact-list.view :as contact-list]
            [status-im2.common.home.view :as common.home]
            [status-im2.contexts.chat.home.chat-list-item.view :as chat-list-item]
            [status-im2.contexts.chat.home.contact-request.view :as contact-request]
            [utils.re-frame :as rf]
            [status-im2.common.contact-list-item.view :as contact-list-item]
            [status-im2.common.home.actions.view :as actions]
            [react-native.reanimated :as reanimated]
            [status-im2.common.sticky-scroll-view.view :as sticky-scroll-view]
            [react-native.safe-area :as safe-area]))

(defn get-item-layout
  [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn filter-items-by-tab
  [tab items]
  (if (= tab :groups)
    (filter :group-chat items)
    (filter :chat-id items)))

(defn welcome-blank-chats
  []
  [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [quo/icon :i/placeholder]
   [quo/text {:weight :semi-bold} (i18n/label :t/no-messages)]
   [quo/text (i18n/label :t/blank-messages-text)]])

(defn chats
  [selected-tab]
  (let [{:keys [items search-filter]} (rf/sub [:home-items])
        items                         (filter-items-by-tab selected-tab items)]
    (if (and (empty? items)
             (empty? search-filter))
      [welcome-blank-chats]
      [rn/flat-list
       {:key-fn                       #(or (:chat-id %) (:public-key %) (:id %))
        :get-item-layout              get-item-layout
        :on-end-reached               #(re-frame/dispatch [:chat/show-more-chats])
        :keyboard-should-persist-taps :always
        :data                         items
        :render-fn                    chat-list-item/chat-list-item
        :content-container-style      {:padding-bottom 30}}])))

(defn welcome-blank-contacts
  []
  [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [quo/icon :i/placeholder]
   [quo/text {:weight :semi-bold} (i18n/label :t/no-contacts)]
   [quo/text (i18n/label :t/blank-contacts-text)]])

(defn contact-item-render
  [{:keys [public-key] :as item}]
  (let [current-pk           (rf/sub [:multiaccount/public-key])
        show-profile-actions #(rf/dispatch [:bottom-sheet/show-sheet
                                            {:content (fn [] [actions/contact-actions item])}])]
    [contact-list-item/contact-list-item
     (when (not= public-key current-pk)
       {:on-press      #(rf/dispatch [:chat.ui/show-profile public-key])
        :on-long-press show-profile-actions
        :accessory     {:type     :options
                        :on-press show-profile-actions}})
     item]))

(defn contacts-section-list
  [sections]
  [rn/section-list
   {:key-fn                         :title
    :sticky-section-headers-enabled false
    :sections                       sections
    :render-section-header-fn       contact-list/contacts-section-header
    :content-container-style        {:padding-bottom 20}
    :render-fn                      contact-item-render}])

(defn contacts
  [pending-contact-requests]
  (let [items (rf/sub [:contacts/active-sections])]
    (if (and (empty? items) (empty? pending-contact-requests))
      [welcome-blank-contacts]
      [:<>
       (when (seq pending-contact-requests)
         [contact-request/contact-requests pending-contact-requests])
       (when (seq items)
         [contacts-section-list items])])))

(defn home
  []
  (let [selected-tab (reagent/atom :recent)]
    (fn []
      [:f>
       (fn []
         (let [header-height            112
               sticky-item-height       60
               card-height              72
               scroll-y                 (reanimated/use-shared-value 0)
               tabs-translation-y       (reanimated/interpolate scroll-y
                                                                [card-height
                                                                 (+ card-height sticky-item-height)]
                                                                [0 sticky-item-height]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "extend"})
               pending-contact-requests (rf/sub [:activity-center/pending-contact-requests])]
           [safe-area/consumer
            (fn [{:keys [top]}]
              [:<>
               [rn/view {:position :absolute :top top :left 0 :right 0 :height header-height :z-index 1}
                [common.home/top-nav]
                [common.home/title-column
                 {:label               (i18n/label :t/messages)
                  :handler             #(rf/dispatch [:bottom-sheet/show-sheet :new-chat-bottom-sheet
                                                      {}])
                  :accessibility-label :new-chat-button}]]
               [sticky-scroll-view/scroll-view
                {:scroll-y scroll-y
                 :blur     {:height (+ top header-height sticky-item-height)
                            :delta  sticky-item-height}}
                [rn/view {:height (+ header-height top)}]
                [quo/discover-card
                 {:title       (i18n/label :t/invite-friends-to-status)
                  :description (i18n/label :t/share-invite-link)}]
                [sticky-scroll-view/sticky-item
                 {:translation-y tabs-translation-y
                  :height        sticky-item-height}
                 [quo/tabs
                  {:style          {:padding-horizontal 20
                                    :padding-top        16
                                    :padding-bottom     12}
                   :size           32
                   :on-change      #(reset! selected-tab %)
                   :default-active @selected-tab
                   :data           [{:id                  :recent
                                     :label               (i18n/label :t/recent)
                                     :accessibility-label :tab-recent}
                                    {:id                  :groups
                                     :label               (i18n/label :t/groups)
                                     :accessibility-label :tab-groups}
                                    {:id                  :contacts
                                     :label               (i18n/label :t/contacts)
                                     :accessibility-label :tab-contacts
                                     :notification-dot?   (pos? (count pending-contact-requests))}]}]]
                (if (= @selected-tab :contacts)
                  [contacts pending-contact-requests]
                  [chats @selected-tab])]])]))])))

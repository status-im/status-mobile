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
            [utils.re-frame :as rf]))

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
        :on-end-reached               #(re-frame/dispatch [:chat.ui/show-more-chats])
        :keyboard-should-persist-taps :always
        :data                         items
        :render-fn                    chat-list-item/chat-list-item}])))

(defn welcome-blank-contacts
  []
  [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
   [quo/icon :i/placeholder]
   [quo/text {:weight :semi-bold} (i18n/label :t/no-contacts)]
   [quo/text (i18n/label :t/blank-contacts-text)]])

(defn contacts
  [contact-requests]
  (let [items (rf/sub [:contacts/active-sections])]
    (if (and (empty? items) (empty? contact-requests))
      [welcome-blank-contacts]
      [:<>
       (when (seq contact-requests)
         [contact-request/contact-requests contact-requests])
       (when (seq items)
         [contact-list/contact-list {:icon :options}])])))

(defn tabs
  []
  (let [selected-tab (reagent/atom :recent)]
    (fn []
      (let [contact-requests (rf/sub [:activity-center/pending-contact-requests])]
        [:<>
         [quo/discover-card
          {:title       (i18n/label :t/invite-friends-to-status)
           :description (i18n/label :t/share-invite-link)}]
         [quo/tabs
          {:style          {:margin-left   20
                            :margin-bottom 20
                            :margin-top    24}
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
                             :notification-dot?   (pos? (count contact-requests))}]}]
         (if (= @selected-tab :contacts)
           [contacts contact-requests]
           [chats @selected-tab])]))))

(defn home
  []
  [:<>
   [common.home/top-nav {:type :default}]
   [common.home/title-column
    {:label               (i18n/label :t/messages)
     :handler             #(rf/dispatch [:bottom-sheet/show-sheet :new-chat-bottom-sheet {}])
     :accessibility-label :new-chat-button}]
   [tabs]])

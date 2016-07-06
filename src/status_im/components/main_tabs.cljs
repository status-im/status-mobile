(ns status-im.components.main-tabs
  (:require-macros [reagent.ratom :refer [reaction]]
                   [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [status-im.components.react :refer [view
                                                animated-view
                                                text-input
                                                text
                                                image
                                                touchable-highlight
                                                get-dimensions]]
            [status-im.components.drawer.view :refer [drawer-view]]
            [status-im.components.animation :as anim]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.discovery.screen :refer [discovery]]
            [status-im.contacts.screen :refer [contact-list]]
            [status-im.components.tabs.tabs :refer [tabs]]
            [status-im.components.tabs.styles :as st]
            [status-im.components.styles :as common-st]
            [status-im.i18n :refer [label]]))

(def window-width (:width (get-dimensions "window")))

(def tab-list
  [{:view-id :chat-list
    :title   (label :t/chats)
    :screen  chats-list
    :icon    :icon_tab_chats
    :index   0}
   {:view-id :discovery
    :title   (label :t/discovery)
    :screen  discovery
    :icon    :icon_tab_discovery
    :index   1}
   {:view-id :contact-list
    :title   (label :t/contacts)
    :screen  contact-list
    :icon    :icon_tab_contacts
    :index   2}])

(defn animation-logic [{:keys [offsets val tab-id to-tab-id]}]
  (fn [_]
    (when-let [offsets @offsets]
      (let [from-value (:from offsets)
            to-value (:to offsets)
            to-tab-id @to-tab-id]
        (anim/set-value val from-value)
        (when to-value
          (anim/start
            (anim/timing val {:toValue  to-value
                              :duration 300})
            (when (= tab-id to-tab-id)
              (fn [arg]
                (when (.-finished arg)
                  (dispatch [:on-navigated-to-tab]))))))))))

(defn get-tab-index-by-id [id]
  (:index (first (filter #(= id (:view-id %)) tab-list))))

(defn get-offsets [tab-id from-id to-id]
  (let [tab (get-tab-index-by-id tab-id)
        from (get-tab-index-by-id from-id)
        to (get-tab-index-by-id to-id)]
    (if (or (= tab from) (= tab to))
      (cond
        (or (nil? from) (= from to)) {:from 0}
        (< from to) (if (= tab to)
                      {:from window-width, :to 0}
                      {:from 0, :to (- window-width)})
        (< to from) (if (= tab to)
                      {:from (- window-width), :to 0}
                      {:from 0, :to window-width}))
      {:from (- window-width)})))

(defn tab-view-container [tab-id content]
  (let [cur-tab-id (subscribe [:get :view-id])
        prev-tab-id (subscribe [:get :prev-tab-view-id])
        offsets (reaction (get-offsets tab-id @prev-tab-id @cur-tab-id))
        anim-value (anim/create-value (- window-width))
        context {:offsets   offsets
                 :val       anim-value
                 :tab-id    tab-id
                 :to-tab-id cur-tab-id}
        on-update (animation-logic context)]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [tab-id content]
         @offsets
         [animated-view {:style (st/tab-view-container anim-value)}
          content])})))

(defn tab-view [{:keys [view-id screen]}]
  ^{:key view-id}
  [tab-view-container view-id
   [screen]])

(defview main-tabs []
  [view-id [:get :view-id]
   tab-animation? [:get :prev-tab-view-id]]
  [drawer-view
   [view {:style         common-st/flex
          :pointerEvents (if tab-animation? :none :auto)}
    (doall (map #(tab-view %) tab-list))
    [tabs {:selected-view-id view-id
           :tab-list         tab-list}]]])

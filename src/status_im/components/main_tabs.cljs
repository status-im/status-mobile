(ns status-im.components.main-tabs
  (:require-macros [status-im.utils.views :refer [defview]]
                   [cljs.core.async.macros :as am])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [status-im.components.react :refer [view swiper]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.drawer.view :refer [drawer-view]]
            [status-im.components.tabs.bottom-shadow :refer [bottom-shadow-view]]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.discover.screen :refer [discover]]
            [status-im.contacts.views :refer [contact-groups-list]]
            [status-im.components.tabs.tabs :refer [tabs]]
            [status-im.components.tabs.styles :as st]
            [status-im.components.styles :as common-st]
            [status-im.i18n :refer [label]]
            [cljs.core.async :as a]))

(def tab-list
  [{:view-id       :chat-list
    :title         (label :t/chats)
    :screen        chats-list
    :icon-inactive :icon_chats
    :icon-active   :icon_chats_active
    :index         0}
   {:view-id       :discover
    :title         (label :t/discover)
    :screen        discover
    :icon-inactive :icon_discover
    :icon-active   :icon_discover_active
    :index         1}
   {:view-id       :contact-list
    :title         (label :t/contacts)
    :screen        contact-groups-list
    :icon-inactive :icon_contacts
    :icon-active   :icon_contacts_active
    :index         2}])

(def tab->index {:chat-list    0
                 :discover     1
                 :contact-list 2})

(def index->tab (clojure.set/map-invert tab->index))

(defn get-tab-index [view-id]
  (get tab->index view-id 0))

(defn scroll-to [prev-view-id view-id]
  (let [p (get-tab-index prev-view-id)
        n (get-tab-index view-id)]
    (- n p)))

(defonce scrolling? (atom false))

(defn on-scroll-end [swiped? scroll-ended view-id]
  (fn [_ state]
    (when @scrolling?
      (a/put! scroll-ended true))
    (let [{:strs [index]} (js->clj state)
          new-view-id (index->tab index)]
      (when-not (= view-id new-view-id)
        (reset! swiped? true)
        (dispatch [:navigate-to-tab new-view-id])))))

(defn start-scrolling-loop
  "Loop that synchronizes tabs scrolling to avoid an inconsistent state."
  [scroll-start scroll-ended]
  (am/go-loop [[swiper to] (a/<! scroll-start)]
    ;; start scrolling
    (reset! scrolling? true)
    (.scrollBy swiper to)
    ;; lock loop until scroll ends
    (a/alts! [scroll-ended (a/timeout 2000)])
    (reset! scrolling? false)
    (recur (a/<! scroll-start))))

(defn main-tabs []
  (let [view-id           (subscribe [:get :view-id])
        prev-view-id      (subscribe [:get :prev-view-id])
        tabs-hidden?      (subscribe [:tabs-hidden?])
        main-swiper       (r/atom nil)
        swiped?           (r/atom false)
        scroll-start      (a/chan 10)
        scroll-ended      (a/chan 10)
        tabs-were-hidden? (atom @tabs-hidden?)]
    (r/create-class
      {:component-did-mount
       #(start-scrolling-loop scroll-start scroll-ended)
       :component-will-update
       (fn []
         (if @swiped?
           (reset! swiped? false)
           (when (and (= @tabs-were-hidden? @tabs-hidden?) @main-swiper)
             (let [to (scroll-to @prev-view-id @view-id)]
               (a/put! scroll-start [@main-swiper to]))))
         (reset! tabs-were-hidden? @tabs-hidden?))
       :reagent-render
       (fn []
         [view common-st/flex
          [status-bar {:type :main}]
          [view common-st/flex
           [drawer-view
            [view {:style common-st/flex}
             [swiper (merge
                      (st/main-swiper @tabs-hidden?)
                      {:index                  (get-tab-index @view-id)
                       :loop                   false
                       :ref                    #(reset! main-swiper %)
                       :on-momentum-scroll-end (on-scroll-end swiped? scroll-ended @view-id)})
              [chats-list]
              [discover (= @view-id :discover)]
              [contact-groups-list (= @view-id :contact-list)]]
             [tabs {:selected-view-id @view-id
                    :prev-view-id     @prev-view-id
                    :tab-list         tab-list}]
             (when-not @tabs-hidden?
               [bottom-shadow-view])]]]])})))

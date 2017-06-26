(ns status-im.components.main-tabs
  (:require-macros [reagent.ratom :refer [reaction]]
                   [status-im.utils.views :refer [defview]]
                   [cljs.core.async.macros :as am])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [reagent.core :as r]
            [status-im.components.react :refer [view swiper get-dimensions]]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.drawer.view :refer [drawer-view]]
            [status-im.components.tabs.bottom-shadow :refer [bottom-shadow-view]]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.discover.screen :refer [discover]]
            [status-im.contacts.screen :refer [contact-list]]
            [status-im.components.tabs.tabs :refer [tabs]]
            [status-im.components.tabs.styles :as st]
            [status-im.components.styles :as common-st]
            [status-im.i18n :refer [label]]
            [cljs.core.async :as a]
            [status-im.utils.platform :refer [ios?]]))

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
    :screen        contact-list
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
;;Resized state
(defonce resized? (atom true))

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
    ;;When the android activity is restarted we get an extra 'on-scroll-end' event.
    ;;This causes the swiper to enter an inconsistent state if it is not prevented,
    ;;and this is the most robust way I've found to deal with it.
    (if
      ios?
      (.scrollBy swiper to)
      (when-not @resized? (.scrollBy swiper to)))
    (reset! resized? false)
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
        dims              (r/atom (get-dimensions "window"))
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
            [view {:style common-st/flex
                   ;;Detecting when the layout changes size, perhaps this should be an event
                   ;;that hooks into re-frame?
                   :onLayout (fn [e]
                               (when-not ios?
                                 (let [{new-width :width, new-height :height} (js->clj
                                                                                (-> e .-nativeEvent .-layout)
                                                                                :keywordize-keys true)
                                       {:keys [width height]} @dims]
                                   (when-not (or (= new-width width) (= new-height height))
                                     (reset! resized? true)
                                     (reset! dims {:width new-width :height new-height})))))}
             [swiper (merge
                       (st/main-swiper @tabs-hidden?)
                       {:index                  (get-tab-index @view-id)
                        :loop                   false
                        :ref                    #(reset! main-swiper %)
                        :on-momentum-scroll-end (on-scroll-end swiped? scroll-ended @view-id)}
                       ;;Adjust the dimensions dynamically, Android only
                       (when-not ios? (update @dims :height #(- % 30))))
              [chats-list]
              [discover (= @view-id :discover)]
              [contact-list (= @view-id :contact-list)]]
             [tabs {:selected-view-id @view-id
                    :prev-view-id     @prev-view-id
                    :tab-list         tab-list}]
             (when-not @tabs-hidden?
               [bottom-shadow-view])]]]])})))

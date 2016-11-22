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
                                                get-dimensions
                                                swiper]]
            [status-im.components.status-bar :refer [status-bar]]
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
            to-value   (:to offsets)
            to-tab-id  @to-tab-id]
        (anim/set-value val from-value)
        (when to-value
          (anim/start
            (anim/timing val {:toValue  to-value
                              :duration 300})
            (when (= tab-id to-tab-id)
              (fn [arg]
                (when (.-finished arg)
                  (dispatch [:on-navigated-to-tab]))))))))))

(def tab->index {:chat-list    0
                 :discovery    1
                 :contact-list 2})

(def index->tab (clojure.set/map-invert tab->index))

(defn get-tab-index [view-id]
  (get tab->index view-id 0))

(defn scroll-to [prev-view-id view-id]
  (let [p (get-tab-index prev-view-id)
        n (get-tab-index view-id)]
    (- n p)))

(defn on-scroll-end [swiped?]
  (fn [_ state]
    (let [{:strs [index]} (js->clj state)]
      (reset! swiped? true)
      (dispatch [:navigate-to-tab (index->tab index)]))))

(defn main-tabs []
  (let [view-id      (subscribe [:get :view-id])
        prev-view-id (subscribe [:get :prev-view-id])
        main-swiper  (atom nil)
        swiped?      (atom false)]
    (r/create-class
      {:component-will-update
       (fn []
         (if @swiped?
           (reset! swiped? false)
           (when @main-swiper
             (let [to (scroll-to @prev-view-id @view-id)]
               (.scrollBy @main-swiper to)))))
       :reagent-render
       (fn []
         [view common-st/flex
          [status-bar {:type :main}]
          [view common-st/flex
           [drawer-view
            [view {:style common-st/flex}
             [swiper (merge
                       st/main-swiper
                       {:index                  (get-tab-index @view-id)
                        :loop                   false
                        :ref                    #(reset! main-swiper %)
                        :on-momentum-scroll-end (on-scroll-end swiped?)})
              [chats-list]
              [discovery (= @view-id :discovery)]
              [contact-list]]
             [tabs {:selected-view-id @view-id
                    :prev-view-id     @prev-view-id
                    :tab-list         tab-list}]]]]])})))

(ns status-im.ui.screens.main-tabs.views
  (:require-macros [status-im.utils.views :as views]
                   [cljs.core.async.macros :as async-macros])
  (:require [re-frame.core :as re-frame]
            [status-im.components.react :as react]
            [status-im.components.tabs.styles :as styles]
            [status-im.components.styles :as common-styles]
            [status-im.i18n :as i18n]
            [cljs.core.async :as async]
            [status-im.components.status-bar :refer [status-bar]]
            [status-im.components.drawer.view :refer [drawer-view]]
            [status-im.ui.screens.chats-list.views :refer [chats-list]]
            [status-im.ui.screens.discover.views :refer [discover]]
            [status-im.ui.screens.contacts.views :refer [contact-groups-list]]
            [status-im.ui.screens.wallet.main.views :refer [wallet]]
            [status-im.components.tabs.views :refer [tabs]]))

(def tab-list
  [{:view-id       :wallet
    :title         (i18n/label :t/wallet)
    :screen        wallet
    :icon-inactive :icons/wallet
    :icon-active   :icons/wallet-active}
   {:view-id       :chat-list
    :title         (i18n/label :t/chats)
    :screen        chats-list
    :icon-inactive :icons/chats
    :icon-active   :icons/chats-active}
   {:view-id       :discover
    :title         (i18n/label :t/discover)
    :screen        discover
    :icon-inactive :icons/discover
    :icon-active   :icons/discover-active}
   {:view-id       :contact-list
    :title         (i18n/label :t/contacts)
    :screen        contact-groups-list
    :icon-inactive :icons/contacts
    :icon-active   :icons/contacts-active}])

(def tab->index (reduce #(assoc %1 (:view-id %2) (count %1)) {} tab-list))

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
      (async/put! scroll-ended true))
    (let [{:strs [index]} (js->clj state)
          new-view-id (index->tab index)]
      (when-not (= view-id new-view-id)
        (reset! swiped? true)
        (re-frame/dispatch [:navigate-to-tab new-view-id])))))

(defn start-scrolling-loop
  "Loop that synchronizes tabs scrolling to avoid an inconsistent state."
  [scroll-start scroll-ended]
  (async-macros/go-loop [[swiper to] (async/<! scroll-start)]
    ;; start scrolling
    (reset! scrolling? true)
    (.scrollBy swiper to)
    ;; lock loop until scroll ends
    (async/alts! [scroll-ended (async/timeout 2000)])
    (reset! scrolling? false)
    (recur (async/<! scroll-start))))

(views/defview main-tabs []
  (views/letsubs [view-id           [:get :view-id]
                  prev-view-id      [:get :prev-view-id]
                  tabs-hidden?      [:tabs-hidden?]
                  main-swiper       (atom nil)
                  swiped?           (atom false)
                  scroll-start      (async/chan 10)
                  scroll-ended      (async/chan 10)
                  tabs-were-hidden? (atom @tabs-hidden?)]
    {:component-did-mount
     (fn []
       (start-scrolling-loop scroll-start scroll-ended))
     :component-will-update
     (fn []
       (if @swiped?
         (reset! swiped? false)
         (when (and (= @tabs-were-hidden? tabs-hidden?) @main-swiper)
           (let [to (scroll-to prev-view-id view-id)]
             (async/put! scroll-start [@main-swiper to]))))
       (reset! tabs-were-hidden? tabs-hidden?))}
    [react/view common-styles/flex
     [status-bar {:type (if (= view-id :wallet) :wallet :main)}]
     [react/view common-styles/flex
      [drawer-view
       [react/view {:style common-styles/flex}
        [react/swiper
         (merge
           (styles/main-swiper tabs-hidden?)
           {:index                  (get-tab-index view-id)
            :loop                   false
            :ref                    #(reset! main-swiper %)
            :on-momentum-scroll-end (on-scroll-end swiped? scroll-ended view-id)})
         (doall
           (map-indexed (fn [index {vid :view-id screen :screen}]
                          ^{:key index} [screen (= view-id vid)]) tab-list))]
        [tabs {:style (styles/tabs-container tabs-hidden?)
               :selected-view-id view-id
               :tab-list         tab-list}]]]]]))
(ns status-im.components.tabs.views
  (:require-macros [status-im.utils.views :refer [defview]]
                   [cljs.core.async.macros :as am])
  (:require [cljs.core.async :as async]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.tabs.styles :as tabs.styles]
            [status-im.utils.platform :as platform]))

(defn- tab [{:keys [view-id title title-fn icon-active icon-inactive style-active selected-view-id on-press]
             :or {title-fn react/text}}]
  (let [active? (= view-id selected-view-id)
        text-only? (nil? icon-active)]
    [react/touchable-highlight {:style    (merge tabs.styles/tab
                                                 (when (and active? style-active)
                                                   style-active))
                                :disabled active?
                                :on-press #(if on-press
                                             (on-press view-id)
                                             (dispatch [:navigate-to-tab view-id]))}
     [react/view {:style tabs.styles/tab-container}
      (when-let [icon (if active? icon-active icon-inactive)]
        [react/view
         [vi/icon icon (tabs.styles/tab-icon active?)]])
      [react/view {:style tabs.styles/tab-title-wrapper}
       (title-fn {:uppercase? (and text-only? (get-in platform/platform-specific [:uppercase?]))
                  :style      (tabs.styles/tab-title active? text-only?)}
                 title)]]]))

(defn- create-tab [index data selected-view-id on-press style-active]
  (let [data (merge data {:key              index
                          :index            index
                          :style-active     style-active
                          :selected-view-id selected-view-id
                          :on-press         on-press})]
    [tab data]))

(defview tabs-container [style children]
  (letsubs [tabs-hidden? [:tabs-hidden?]]
    [react/animated-view {:style          style
                          :pointer-events (if tabs-hidden? :none :auto)}
     children]))

(defn tabs [{:keys [style style-tab-active tab-list selected-view-id on-press]}]
  [tabs-container style
   (into
     [react/view tabs.styles/tabs-inner-container]
     (map-indexed #(create-tab %1 %2 selected-view-id on-press style-tab-active) tab-list))])

;; Swipable tabs

(defn- tab->index [tabs] (reduce #(assoc %1 (:view-id %2) (count %1)) {} tabs))

(defn get-tab-index [tabs view-id]
  (get (tab->index tabs) view-id 0))

(defn index->tab [tabs] (clojure.set/map-invert (tab->index tabs)))

(defn scroll-to [tab-list prev-view-id view-id]
  (let [p (get-tab-index tab-list prev-view-id)
        n (get-tab-index tab-list view-id)]
    (- n p)))

(defonce scrolling? (atom false))

(defn on-scroll-end [on-view-change tabs swiped? scroll-ended view-id]
  (fn [_ state]
    (when @scrolling?
      (async/put! scroll-ended true))
    (let [{:strs [index]} (js->clj state)
          new-view-id ((index->tab tabs) index)]
      (if new-view-id
        (when-not (= @view-id new-view-id)
          (reset! swiped? true)
          (on-view-change new-view-id))))))

(defn start-scrolling-loop
  "Loop that synchronizes tabs scrolling to avoid an inconsistent state."
  [scroll-start scroll-ended]
  (am/go-loop [[swiper to] (async/<! scroll-start)]
              ;; start scrolling
              (reset! scrolling? true)
              (.scrollBy swiper to)
              ;; lock loop until scroll ends
              (async/alts! [scroll-ended (async/timeout 2000)])
              (reset! scrolling? false)
              (recur (async/<! scroll-start))))

(defn swipable-tabs [{:keys [style style-tabs style-tab-active on-view-change]} tab-list prev-view-id view-id]
  (let [swiped?           (r/atom false)
        main-swiper       (r/atom nil)
        scroll-start      (async/chan 10)
        scroll-ended      (async/chan 10)]
    (r/create-class
      {:component-did-mount
       #(start-scrolling-loop scroll-start scroll-ended)
       :component-will-update
       (fn []
         (if @swiped?
           (reset! swiped? false)
           (when @main-swiper
             (let [to (scroll-to tab-list @prev-view-id @view-id)]
               (async/put! scroll-start [@main-swiper to])))))
       :display-name "swipable-tabs"
       :reagent-render
       (fn []
         [react/view {:style style}
          [tabs {:selected-view-id @view-id
                 :tab-list         tab-list
                 :style            style-tabs
                 :style-tab-active style-tab-active
                 :on-press         on-view-change}]
          [react/swiper (merge tabs.styles/swiper
                               {:index                  (get-tab-index tab-list @view-id)
                                :ref                    #(reset! main-swiper %)
                                :loop                   false
                                :on-momentum-scroll-end (on-scroll-end on-view-change tab-list swiped? scroll-ended view-id)})
           (doall
            (map-indexed (fn [index {screen :screen}]
                           (with-meta screen {:key index}))
                         tab-list))]])})))

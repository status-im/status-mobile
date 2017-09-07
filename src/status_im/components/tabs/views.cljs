(ns status-im.components.tabs.views
  (:require-macros [status-im.utils.views :refer [defview]]
                   [cljs.core.async.macros :as am])
  (:require [cljs.core.async :as async]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.tabs.styles :as styles]
            [status-im.utils.platform :as p]))

(defn- tab [{:keys [view-id title icon-active icon-inactive selected-view-id on-press]}]
  (let [active? (= view-id selected-view-id)]
    [react/touchable-highlight {:style    styles/tab
                                :disabled active?
                                :on-press  #(if on-press (on-press view-id) (dispatch [:navigate-to-tab view-id]))}
     [react/view {:style styles/tab-container}
      (when-let [icon (if active? icon-active icon-inactive)]
        [react/view
         [vi/icon icon (styles/tab-icon active?)]])
      [react/view
       [react/text (merge (if-not icon-active {:uppercase? (get-in p/platform-specific [:uppercase?])})
                          {:style (styles/tab-title active?)})
        title]]]]))

(defn- create-tab [index data selected-view-id on-press]
  (let [data (merge data {:key              index
                          :index            index
                          :selected-view-id selected-view-id
                          :on-press         on-press})]
    [tab data]))

(defview tabs-container [style children]
  (letsubs [tabs-hidden? [:tabs-hidden?]]
    [react/animated-view {:style         (merge style
                                                styles/tabs-container-line)
                          :pointer-events (if tabs-hidden? :none :auto)}
     children]))

(defn tabs [{:keys [style tab-list selected-view-id on-press]}]
  [tabs-container style
   (into
     [react/view styles/tabs-inner-container]
     (map-indexed #(create-tab %1 %2 selected-view-id on-press) tab-list))])

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

(defn swipable-tabs [{:keys [style on-view-change]} tab-list prev-view-id view-id]
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
                               :on-press         on-view-change}]
                        [react/swiper (merge styles/swiper
                                             {:index                  (get-tab-index tab-list @view-id)
                                              :ref                    #(reset! main-swiper %)
                                              :loop                   false
                                              :on-momentum-scroll-end (on-scroll-end on-view-change tab-list swiped? scroll-ended view-id)})
                         (doall
                           (map-indexed (fn [index {screen :screen}]
                                          (with-meta screen {:key index})) tab-list))]])})))
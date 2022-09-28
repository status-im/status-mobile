(ns quo2.components.tabs.tabs
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo2.components.tabs.tab :as tab]))

(def default-tab-size 32)

(defn tabs [{:keys [default-active on-change]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size] :or {size default-tab-size}}]
      (let [active-id @active-tab-id]
        [rn/view {:flex-direction :row}
         (for [{:keys [label id]} data]
           ^{:key id}
           [rn/view {:margin-right (if (= size default-tab-size) 12 8)}
            [tab/tab
             {:id       id
              :size     size
              :active   (= id active-id)
              :on-press (fn [^js press-event id]
                          (reset! active-tab-id id)
                          (when on-change
                            (on-change press-event id)))}
             label]])]))))

(defn scrollable-tabs
  [{:keys [default-active]}]
  (let [active-tab-id (reagent/atom default-active)
        flat-list-ref (atom nil)]
    (fn [{:keys [data
                 on-change
                 scroll-event-throttle
                 scroll-on-press?
                 size]
          :or   {scroll-event-throttle 64
                 scroll-on-press?      false
                 size                  default-tab-size}
          :as   props}]
      [rn/flat-list
       (merge (dissoc props
                      :on-change
                      :scroll-on-press?
                      :size)
              {:ref                               (partial reset! flat-list-ref)
               :horizontal                        true
               :scroll-event-throttle             scroll-event-throttle
               :shows-horizontal-scroll-indicator false
               :data                              data
               :key-fn                            :id
               :render-fn                         (fn [{:keys [id label]} index]
                                                    [rn/view {:margin-right  (if (= size default-tab-size) 12 8)
                                                              :padding-right (when (= index (dec (count data)))
                                                                               (get-in props [:style :padding-left]))}
                                                     [tab/tab {:id       id
                                                               :size     size
                                                               :active   (= id @active-tab-id)
                                                               :on-press (fn [id]
                                                                           (reset! active-tab-id id)
                                                                           (when scroll-on-press?
                                                                             (.scrollToIndex @flat-list-ref
                                                                                             #js {:animated     true
                                                                                                  :index        index
                                                                                                  :viewPosition 0.5}))
                                                                           (when on-change
                                                                             (on-change id)))}
                                                      label]])})])))

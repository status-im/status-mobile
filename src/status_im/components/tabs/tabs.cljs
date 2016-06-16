(ns status-im.components.tabs.tabs
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                animated-view
                                                text-input
                                                text
                                                image
                                                touchable-highlight
                                                linear-gradient]]
            [reagent.core :as r]
            [status-im.components.tabs.styles :as st]
            [status-im.components.tabs.tab :refer [tab]]
            [status-im.components.animation :as anim]))

(defn create-tab [index data selected-view-id]
  (let [data (merge data {:key              index
                          :index            index
                          :selected-view-id selected-view-id})]
    [tab data]))

(defn animation-logic [{:keys [hidden? val]}]
  (let [was-hidden? (atom (not @hidden?))]
    (fn [_]
      (when (not= @was-hidden? @hidden?)
        (let [to-value (if @hidden? 0 (- st/tabs-height))]
          (swap! was-hidden? not)
          (anim/start
            (anim/timing val {:toValue  to-value
                              :duration 300})
            (fn [e]
              ;; if to-value was changed, then new animation has started
              (when (= to-value (if @hidden? 0 (- st/tabs-height)))
                (dispatch [:set-animation :tabs-bar-animation? false])))))))))

(defn tabs-container [& children]
  (let [chats-scrolled? (subscribe [:get :chats-scrolled?])
        animation? (subscribe [:animations :tabs-bar-animation?])
        tabs-bar-value (subscribe [:animations :tabs-bar-value])
        context {:hidden?    chats-scrolled?
                 :val        @tabs-bar-value}
        on-update (animation-logic context)]
    (anim/set-value @tabs-bar-value 0)
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [& children]
         @chats-scrolled?
         (into [animated-view {:style         (st/tabs-container @chats-scrolled? @animation? @tabs-bar-value)
                               :pointerEvents (if @chats-scrolled? :none :auto)}]
               children))})))

(defn tabs [{:keys [tab-list selected-view-id]}]
  [tabs-container
   [linear-gradient {:colors ["rgba(24, 52, 76, 0.01)" "rgba(24, 52, 76, 0.085)" "rgba(24, 52, 76, 0.165)"]
                     :style  st/top-gradient}]
   [view st/tabs-inner-container
    (doall (map-indexed #(create-tab %1 %2 selected-view-id) tab-list))]])

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
            [status-im.components.animation :as anim]
            [status-im.utils.platform :refer [platform-specific]]))

(defn create-tab [index data selected-view-id prev-view-id]
  (let [data (merge data {:key              index
                          :index            index
                          :selected-view-id selected-view-id
                          :prev-view-id     prev-view-id})]
    [tab data]))

(defn animation-logic [{:keys [hidden? val]}]
  (let [was-hidden? (atom (not @hidden?))]
    (fn [_]
      (when (not= @was-hidden? @hidden?)
        (let [to-value (if @hidden? 0 (- st/tabs-height))]
          (swap! was-hidden? not)
          (anim/start
            (anim/timing val {:toValue  to-value
                              :duration 300})))))))

(defn tabs-container [& _]
  (let [chats-scrolled? (subscribe [:get :chats-scrolled?])
        tabs-bar-value  (subscribe [:animations :tabs-bar-value])
        shadows?        (get-in platform-specific [:tabs :tab-shadows?])
        context         {:hidden? chats-scrolled?
                         :val     @tabs-bar-value}
        on-update       (animation-logic context)]
    (anim/set-value @tabs-bar-value 0)
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :reagent-render
       (fn [& children]
         @chats-scrolled?
         (into [animated-view {:style         (merge (st/tabs-container @chats-scrolled?)
                                                     (if-not shadows? st/tabs-container-line))
                               :pointerEvents (if @chats-scrolled? :none :auto)}]
               children))})))

(defn tabs [{:keys [tab-list selected-view-id prev-view-id]}]
  [tabs-container
   (into
     [view st/tabs-inner-container]
     (let [tabs (into [] tab-list)]
       [[create-tab 0 (nth tabs 0) selected-view-id prev-view-id]
        [create-tab 1 (nth tabs 1) selected-view-id prev-view-id]
        [create-tab 2 (nth tabs 2) selected-view-id prev-view-id]])
     ;; todo: figure why it doesn't work on iOS release build
     #_(map-indexed #(create-tab %1 %2 selected-view-id prev-view-id) tab-list))])

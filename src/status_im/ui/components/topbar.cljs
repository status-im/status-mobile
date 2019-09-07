(ns status-im.ui.components.topbar
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.colors :as colors]
            [reagent.core :as reagent]
            [status-im.utils.label :as utils.label]))

(defn default-navigation [modal?]
  {:icon                (if modal? :main-icons/close :main-icons/back)
   :accessibility-label :back-button
   :handler             #(re-frame/dispatch [:navigate-back])})

(defn container [style title-padding & children]
  (into []
        (concat
         [react/view {:style     style
                      :on-layout #(reset! title-padding (max (-> ^js % .-nativeEvent .-layout .-width)
                                                             @title-padding))}]
         children)))

(defn button [value nav?]
  (let [{:keys [handler icon label accessibility-label]} value]
    [react/touchable-highlight {:on-press #(when handler (handler))}
     [react/view (cond-> {:padding-horizontal (if nav? 16 10) :height 56
                          :align-items        :center :justify-content :center}
                   accessibility-label
                   (assoc :accessibility-label accessibility-label))
      (cond
        icon
        [icons/icon icon]
        label
        [react/text {:style {:color colors/blue}}
         (utils.label/stringify label)])]]))

;; TODO(Ferossgp): Tobbar should handle safe area
(defn topbar [_]
  (let [title-padding (reagent/atom 16)]
    (fn [& [{:keys [title navigation accessories show-border? modal? content]}]]
      (let [navigation (or navigation (default-navigation modal?))]
        [react/view (cond-> {:height 56 :align-items :center :flex-direction :row}
                      show-border?
                      (assoc :border-bottom-width 1 :border-bottom-color colors/gray-lighter))
         (when-not (= navigation :none)
           [container {} title-padding
            [button navigation true]])
         [react/view {:flex 1}]
         (when accessories
           [container {:flex-direction :row :padding-right 6} title-padding
            (for [value accessories]
              ^{:key value}
              [button value false])])
         (when content
           [react/view {:position :absolute :left @title-padding :right @title-padding
                        :top 0 :bottom 0}
            content])
         (when title
           [react/view {:position :absolute :left @title-padding :right @title-padding
                        :top 0 :bottom 0 :align-items :center :justify-content :center}
            [react/text {:style {:typography :title-bold :text-align :center} :number-of-lines 2}
             (utils.label/stringify title)]])]))))

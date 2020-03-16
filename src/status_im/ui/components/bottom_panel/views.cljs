(ns status-im.ui.components.bottom-panel.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.animation :as anim]
            [reagent.core :as reagent]
            [status-im.ui.components.colors :as colors]))

(defn hide-panel-anim
  [bottom-anim-value alpha-value window-height]
  (react/dismiss-keyboard!)
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         (- window-height)
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         0
                               :duration        500
                               :useNativeDriver true})])))

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         40
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         0.4
                               :duration        500
                               :useNativeDriver true})])))

(defn bottom-panel [obj render window-height]
  (let [bottom-anim-value (anim/create-value window-height)
        alpha-value       (anim/create-value 0)
        clear-timeout     (atom nil)
        update?           (atom nil)
        current-obj       (reagent/atom nil)]
    (reagent/create-class
     {:component-will-update (fn [_ [_ obj _ _]]
                               (when @clear-timeout (js/clearTimeout @clear-timeout))
                               (when (or (not= obj @current-obj) @update?)
                                 (cond
                                   @update?
                                   (do (reset! update? false)
                                       (show-panel-anim bottom-anim-value alpha-value))

                                   (and @current-obj obj)
                                   (do (reset! update? true)
                                       (js/setTimeout #(reset! current-obj obj) 600)
                                       (hide-panel-anim bottom-anim-value alpha-value (- window-height)))

                                   obj
                                   (do (reset! current-obj obj)
                                       (show-panel-anim bottom-anim-value alpha-value))

                                   :else
                                   (do (reset! clear-timeout (js/setTimeout #(reset! current-obj nil) 600))
                                       (hide-panel-anim bottom-anim-value alpha-value (- window-height))))))
      :reagent-render        (fn []
                               (when @current-obj
                                 [react/keyboard-avoiding-view {:style {:position :absolute :top 0 :bottom 0 :left 0 :right 0}}
                                  [react/view {:flex 1}
                                   [react/animated-view {:flex 1 :background-color colors/black-persist :opacity alpha-value}]
                                   [react/animated-view {:style {:position  :absolute
                                                                 :transform [{:translateY bottom-anim-value}]
                                                                 :bottom    0 :left 0 :right 0}}
                                    [react/view {:flex 1}
                                     [render @current-obj]]]]]))})))

(views/defview animated-bottom-panel [val signing-view]
  (views/letsubs [{window-height :height} [:dimensions/window]]
    [bottom-panel (when val (select-keys val [:from :contact :amount :token :approve? :message])) signing-view window-height]))
